[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet(
        'all',
        'build',
        'push',
        'deploy',
        'render',
        'init-db',
        'migrate-db',
        'status',
        'local',
        'local-reset',
        'local-pull-base',
        'local-init-db',
        'local-migrate-db',
        'local-down',
        'local-status',
        'help'
    )]
    [string]$Command = 'local'
)

$ErrorActionPreference = 'Stop'

# 以当前脚本所在位置确定仓库根目录，确保用户从任意工作目录启动时都能找到部署入口。
$RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$DeployScript = Join-Path $PSScriptRoot 'deploy.sh'
$DeployLogPath = Join-Path ([System.IO.Path]::GetTempPath()) "qianruo-deploy-$Command-$([guid]::NewGuid().ToString('N')).log"
$DeployErrorLogPath = "$DeployLogPath.stderr"

if (-not (Test-Path -LiteralPath $DeployScript -PathType Leaf)) {
    throw "找不到 Linux 部署入口：$DeployScript"
}

# 优先使用 Git for Windows 自带的 Bash，这样 Docker、Maven 和 Node.js 可沿用 Windows PATH。
$GitBashCandidates = @()
$ProgramFilesPath = [Environment]::GetFolderPath('ProgramFiles')
$ProgramFilesX86Path = [Environment]::GetEnvironmentVariable('ProgramFiles(x86)')

# 补入已安装但尚未写入 Windows PATH 的 Maven；优先读取 MAVEN_HOME，再检查常见安装目录。
$MavenHomeCandidates = @()
if ($env:MAVEN_HOME) {
    $MavenHomeCandidates += $env:MAVEN_HOME
}
if ($ProgramFilesPath) {
    $MavenHomeCandidates += [System.IO.Path]::Combine($ProgramFilesPath, 'Apache Maven')
}
$MavenHomeCandidates += 'D:\Program Files\Apache Maven'
$MavenHome = $MavenHomeCandidates |
    Where-Object { $_ -and (Test-Path -LiteralPath (Join-Path $_ 'bin\mvn.cmd') -PathType Leaf) } |
    Select-Object -First 1
if ($MavenHome) {
    $MavenBin = Join-Path $MavenHome 'bin'
    $env:MAVEN_HOME = $MavenHome
    $env:PATH = "$MavenBin;$env:PATH"
    Write-Host "已将 Maven 加入本次部署环境：$(Join-Path $MavenBin 'mvn.cmd')"
}

# Maven 的 Git Bash 启动脚本要求 JAVA_HOME；若未设置有效路径，则从当前 java.exe 自动定位 JDK 根目录。
$JavaHomeIsValid = $false
if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe') -PathType Leaf)) {
    $JavaHomeIsValid = $true
}
if (-not $JavaHomeIsValid) {
    $JavaCommand = Get-Command 'java.exe' -ErrorAction SilentlyContinue
    if ($JavaCommand -and $JavaCommand.Source) {
        $JavaBin = Split-Path -Parent $JavaCommand.Source
        $DetectedJavaHome = Split-Path -Parent $JavaBin
        if (Test-Path -LiteralPath (Join-Path $DetectedJavaHome 'bin\java.exe') -PathType Leaf) {
            $env:JAVA_HOME = $DetectedJavaHome
            $env:PATH = "$($env:JAVA_HOME)\bin;$env:PATH"
            Write-Host "已将 JDK 加入本次部署环境：$env:JAVA_HOME"
        }
    }
}

if ($ProgramFilesPath) {
    $GitBashCandidates += [System.IO.Path]::Combine($ProgramFilesPath, 'Git', 'bin', 'bash.exe')
}
if ($ProgramFilesX86Path) {
    $GitBashCandidates += [System.IO.Path]::Combine($ProgramFilesX86Path, 'Git', 'bin', 'bash.exe')
}

# Git for Windows 可能安装在非系统盘；从 git.exe 所在的 cmd 目录反查安装根目录。
$GitCommand = Get-Command 'git.exe' -ErrorAction SilentlyContinue
if ($GitCommand -and $GitCommand.Source) {
    $GitCommandDirectory = Split-Path -Parent $GitCommand.Source
    $GitInstallDirectory = Split-Path -Parent $GitCommandDirectory
    $GitBashCandidates += [System.IO.Path]::Combine($GitInstallDirectory, 'bin', 'bash.exe')
    $GitBashCandidates += [System.IO.Path]::Combine($GitInstallDirectory, 'usr', 'bin', 'bash.exe')
}

$GitBashCommand = Get-Command 'bash.exe' -ErrorAction SilentlyContinue
if ($GitBashCommand -and $GitBashCommand.Source -notlike "$env:WINDIR\System32\bash.exe") {
    $GitBashCandidates += $GitBashCommand.Source
}

$GitBashPath = $GitBashCandidates |
    Where-Object { $_ -and (Test-Path -LiteralPath $_ -PathType Leaf) } |
    Select-Object -First 1

if ($GitBashPath) {
    # 切换到仓库根目录后使用 Bash 执行现有部署脚本，部署脚本会自行读取 deploy/.env。
    Write-Host "使用 Git Bash 执行 Windows Docker 部署命令：$Command"
    Push-Location -LiteralPath $RepositoryRoot
    $BashBootstrapPath = Join-Path ([System.IO.Path]::GetTempPath()) "qianruo-deploy-$Command-$([guid]::NewGuid().ToString('N')).sh"
    try {
        # Git Bash 会优先命中 WindowsApps 的 python3 商店占位程序；覆盖为机器上可用的真实 python.exe。
        # 未安装本机 Maven 时，改用 Maven 官方 Java 17 镜像打包，并把 Maven 本地仓库保存在 Docker 卷中。
        $BashBootstrap = @'
# 将部署脚本调用的 python3 转发到 Windows PATH 中可用的真实解释器，避免误启动 WindowsApps 商店别名。
python3() { python.exe "$@"; }
if command -v mvn.cmd >/dev/null 2>&1; then
  # 将 Maven 的 -f 文件参数从 Git Bash 路径转换为 Windows 路径，再调用已安装的 Maven 批处理启动器；其他参数原样转发。
  mvn() {
    local args=("$@")
    local index
    for ((index = 0; index < ${#args[@]}; index++)); do
      if [[ "${args[$index]}" == "-f" && $((index + 1)) -lt ${#args[@]} ]]; then
        args[$((index + 1))]="$(cygpath -w "${args[$((index + 1))]}")"
      fi
    done
    mvn.cmd "${args[@]}"
  }
elif ! command -v mvn >/dev/null 2>&1; then
  # 本机没有 Maven 时，在官方 Java 17 Maven 镜像中执行全部参数，并挂载仓库与持久化依赖缓存供后续构建复用。
  mvn() {
    local windows_root
    local maven_image
    local args=("$@")
    windows_root="$(cygpath -w "$ROOT_DIR")"
    maven_image="${QIANRUO_MAVEN_IMAGE:-maven:3.10.0-eclipse-temurin-17}"
    if [[ "${args[0]:-}" == "-f" && "${args[1]:-}" == "$ROOT_DIR/backend/ruoyi-cloud/pom.xml" ]]; then
      args[1]="/workspace/backend/ruoyi-cloud/pom.xml"
    fi
    docker run --rm \
      --mount "type=bind,source=${windows_root},target=/workspace" \
      --mount "type=volume,source=qianruo-maven-cache,target=/root/.m2" \
      --workdir /workspace/backend/ruoyi-cloud \
      "$maven_image" mvn "${args[@]}"
  }
fi
source ./scripts/deploy.sh "$1"
'@
        # 将启动代码写入无 BOM 的临时 Bash 文件，避免 PowerShell 5.1 对多行 -c 参数中的引号、括号和反斜杠再次转义。
        [System.IO.File]::WriteAllText($BashBootstrapPath, $BashBootstrap, [System.Text.UTF8Encoding]::new($false))
        # 由子进程直接分流标准输出和标准错误，避免 PowerShell 将 Docker BuildKit 进度误报为 NativeCommandError。
        # 两路输出均保存到日志，进程退出码仍取 Bash 实际退出码，不能因普通警告或进度文本误判失败。
        $BashArguments = @("`"$BashBootstrapPath`"", "`"$Command`"")
        $DeployProcess = Start-Process `
            -FilePath $GitBashPath `
            -ArgumentList $BashArguments `
            -WorkingDirectory $RepositoryRoot `
            -NoNewWindow `
            -Wait `
            -PassThru `
            -RedirectStandardOutput $DeployLogPath `
            -RedirectStandardError $DeployErrorLogPath
        $DeployExitCode = $DeployProcess.ExitCode

        if (Test-Path -LiteralPath $DeployErrorLogPath -PathType Leaf) {
            $StandardErrorLines = Get-Content -LiteralPath $DeployErrorLogPath -Encoding UTF8
            if ($StandardErrorLines) {
                Add-Content -LiteralPath $DeployLogPath -Value $StandardErrorLines -Encoding UTF8
            }
        }
        if (Test-Path -LiteralPath $DeployLogPath -PathType Leaf) {
            Get-Content -LiteralPath $DeployLogPath -Encoding UTF8 | ForEach-Object { Write-Host $_ }
        }
    }
    finally {
        if (Test-Path -LiteralPath $BashBootstrapPath -PathType Leaf) {
            Remove-Item -LiteralPath $BashBootstrapPath -Force
        }
        Pop-Location
    }
}
else {
    # 没有 Git Bash 时，通过 WSL 执行；Docker Desktop 需要为所选 WSL 发行版启用集成。
    $WslCommand = Get-Command 'wsl.exe' -ErrorAction SilentlyContinue
    if (-not $WslCommand) {
        throw '未找到 Git Bash 或 WSL。请安装 Git for Windows，或安装 WSL 并启用 Docker Desktop 集成。'
    }

    Write-Host "使用 WSL 执行 Windows Docker 部署命令：$Command"
    $WslRepositoryRoot = (& $WslCommand.Source -- wslpath -u $RepositoryRoot | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or -not $WslRepositoryRoot) {
        throw 'WSL 中没有可用的 Linux 发行版或无法访问仓库路径。请安装并配置 WSL 发行版，或安装 Git for Windows 后重试。'
    }

    # 将仓库路径按 Bash 单引号规则转义，避免目录名中的空格或特殊字符改变执行路径。
    $EscapedWslRepositoryRoot = $WslRepositoryRoot.Replace("'", "'\''")
    $BashCommand = "cd -- '$EscapedWslRepositoryRoot' && exec ./scripts/deploy.sh '$Command'"
    $PreviousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & $WslCommand.Source -- bash -lc $BashCommand 2>&1 |
            Tee-Object -FilePath $DeployLogPath
        $DeployExitCode = $LASTEXITCODE
    }
    finally {
        $ErrorActionPreference = $PreviousErrorActionPreference
    }
}

if ($DeployExitCode -ne 0) {
    $RecentOutput = ''
    if (Test-Path -LiteralPath $DeployLogPath -PathType Leaf) {
        $RecentOutput = (Get-Content -LiteralPath $DeployLogPath -Tail 40 | Out-String).Trim()
    }
    $FailureMessage = "Docker 部署命令 '$Command' 执行失败，退出代码：$DeployExitCode。完整日志：$DeployLogPath"
    if ($RecentOutput) {
        $FailureMessage += "`n最近输出：`n$RecentOutput"
    }
    throw $FailureMessage
}
