package com.ruoyi.rentalbusiness.model;

/** 收发趋势数字对应的订单明细查询枚举。 */
public final class MerchantInboundOutboundOrderQuery
{
    private MerchantInboundOutboundOrderQuery()
    {
    }

    public enum Mode
    {
        MODEL("model"),
        PACKAGE("package");

        private final String code;

        Mode(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }

        public static Mode fromCode(String value)
        {
            for (Mode mode : values())
            {
                if (mode.code.equalsIgnoreCase(value == null ? "" : value.trim()))
                {
                    return mode;
                }
            }
            return null;
        }
    }

    public enum Direction
    {
        SHIP("ship"),
        RETURN("return");

        private final String code;

        Direction(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }

        public static Direction fromCode(String value)
        {
            for (Direction direction : values())
            {
                if (direction.code.equalsIgnoreCase(value == null ? "" : value.trim()))
                {
                    return direction;
                }
            }
            return null;
        }
    }

    public enum Source
    {
        ORDER("order"),
        ESTIMATED("estimated");

        private final String code;

        Source(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }

        public static Source fromCode(String value)
        {
            for (Source source : values())
            {
                if (source.code.equalsIgnoreCase(value == null ? "" : value.trim()))
                {
                    return source;
                }
            }
            return null;
        }
    }
}
