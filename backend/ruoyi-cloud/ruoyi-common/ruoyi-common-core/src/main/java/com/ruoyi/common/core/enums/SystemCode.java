package com.ruoyi.common.core.enums;

/**
 * 平台基础模块数字编码。
 *
 * <p>菜单、角色、字典和调度任务的有限状态统一使用本类定义的数字编码。客户端类型、菜单类型、
 * 配置键等可扩展标识不属于状态值，继续使用其既有字符串契约。</p>
 */
public final class SystemCode
{
    private SystemCode()
    {
    }

    /** 菜单外链标识。 */
    public enum MenuFrame implements NumericCode
    {
        EXTERNAL(0, "外链"),
        INTERNAL(1, "内链");

        private final Integer code;
        private final String label;

        MenuFrame(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 菜单缓存标识。 */
    public enum MenuCache implements NumericCode
    {
        CACHE(0, "缓存"),
        NO_CACHE(1, "不缓存");

        private final Integer code;
        private final String label;

        MenuCache(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 菜单显示标识。 */
    public enum MenuVisibility implements NumericCode
    {
        VISIBLE(0, "显示"),
        HIDDEN(1, "隐藏");

        private final Integer code;
        private final String label;

        MenuVisibility(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 通知公告类型。 */
    public enum NoticeType implements NumericCode
    {
        NOTIFICATION(1, "通知"),
        ANNOUNCEMENT(2, "公告");

        private final Integer code;
        private final String label;

        NoticeType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 平台角色数据范围。 */
    public enum DataScope implements NumericCode
    {
        ALL(1, "全部数据权限"),
        CUSTOM(2, "自定义数据权限"),
        DEPARTMENT(3, "本部门数据权限"),
        DEPARTMENT_AND_CHILD(4, "本部门及以下数据权限"),
        SELF(5, "仅本人数据权限");

        private final Integer code;
        private final String label;

        DataScope(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 调度任务状态。 */
    public enum JobStatus implements NumericCode
    {
        NORMAL(0, "正常"),
        PAUSE(1, "暂停");

        private final Integer code;
        private final String label;

        JobStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 调度任务执行结果。 */
    public enum JobLogStatus implements NumericCode
    {
        SUCCESS(0, "成功"),
        FAILED(1, "失败");

        private final Integer code;
        private final String label;

        JobLogStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 登录结果状态。 */
    public enum LoginStatus implements NumericCode
    {
        SUCCESS(0, "成功"),
        FAILED(1, "失败");

        private final Integer code;
        private final String label;

        LoginStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 调度任务错失执行策略。 */
    public enum JobMisfirePolicy implements NumericCode
    {
        DEFAULT(0, "默认"),
        IGNORE_MISFIRES(1, "立即执行"),
        FIRE_AND_PROCEED(2, "触发一次"),
        DO_NOTHING(3, "不触发");

        private final Integer code;
        private final String label;

        JobMisfirePolicy(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 调度任务并发执行标识。 */
    public enum JobConcurrency implements NumericCode
    {
        ALLOWED(0, "允许并发"),
        DISALLOWED(1, "禁止并发");

        private final Integer code;
        private final String label;

        JobConcurrency(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 字典项默认标识。 */
    public enum DefaultFlag implements NumericCode
    {
        NO(0, "否"),
        YES(1, "是");

        private final Integer code;
        private final String label;

        DefaultFlag(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }
}
