package com.ruoyi.rentalbusiness.model;

import java.util.ArrayList;
import java.util.List;

/** 批量导入结果及逐行错误信息。 */
public class BatchImportResult
{
    public int total;
    public int success;
    public int failed;
    public List<String> errors = new ArrayList<String>();
}
