package com.ruoyi.common.core.constant;

import com.ruoyi.common.core.enums.SystemCode;

/**
 * 任务调度通用常量
 * 
 * @author ruoyi
 */
public class ScheduleConstants
{
    public static final String TASK_CLASS_NAME = "TASK_CLASS_NAME";

    /** 执行目标key */
    public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

    /** 默认 */
    public static final Integer MISFIRE_DEFAULT = SystemCode.JobMisfirePolicy.DEFAULT.getCode();

    /** 立即触发执行 */
    public static final Integer MISFIRE_IGNORE_MISFIRES = SystemCode.JobMisfirePolicy.IGNORE_MISFIRES.getCode();

    /** 触发一次执行 */
    public static final Integer MISFIRE_FIRE_AND_PROCEED = SystemCode.JobMisfirePolicy.FIRE_AND_PROCEED.getCode();

    /** 不触发立即执行 */
    public static final Integer MISFIRE_DO_NOTHING = SystemCode.JobMisfirePolicy.DO_NOTHING.getCode();

    public enum Status
    {
        /**
         * 正常
         */
        NORMAL(SystemCode.JobStatus.NORMAL.getCode()),
        /**
         * 暂停
         */
        PAUSE(SystemCode.JobStatus.PAUSE.getCode());

        private final Integer value;

        Status(Integer value)
        {
            this.value = value;
        }

        public Integer getValue()
        {
            return value;
        }
    }
}
