-- 将市级行政区三日天气刷新注册到平台定时任务服务。
-- 调度由 ruoyi-job 统一负责，业务逻辑由 rental-business 内部接口执行。

USE `ry-cloud`;

INSERT INTO `sys_job`
    (`job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`,
     `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
SELECT '市级行政区三日天气刷新', 'SYSTEM', 'regionWeatherRefreshJob.refreshThreeDayWeather()', '0 0 1 * * ?', '3', '1', '0',
       'migration', NOW(), 'migration', NOW(),
       '平台调度服务调用 rental-business 刷新全部市级行政区未来三日天气，区县复用所属市级天气，业务时区为 Asia/Shanghai', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_job`
    WHERE `invoke_target` = 'regionWeatherRefreshJob.refreshThreeDayWeather()'
      AND `is_delete` = 0
);
