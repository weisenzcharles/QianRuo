#!/usr/bin/env python3
import os
import re
import sys
from pathlib import Path


TOKEN = re.compile(r"\$\{([A-Z0-9_]+)\}")


def render(text: str) -> str:
    def replace(match: re.Match[str]) -> str:
        key = match.group(1)
        return os.environ.get(key, match.group(0))

    return TOKEN.sub(replace, text)


def main() -> int:
    if len(sys.argv) != 3:
        print("Usage: render_template.py <template> <output>", file=sys.stderr)
        return 2

    source = Path(sys.argv[1])
    target = Path(sys.argv[2])
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(render(source.read_text(encoding="utf-8")), encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
