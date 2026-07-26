package com.ruoyi.merchant.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;

/** 商户操作日志返回视图，不暴露租户和商户归属字段。 */
public class MerchantOperLogVO
{
    @Excel(name = "日志编号", cellType = ColumnType.NUMERIC)
    private Long operId;

    @Excel(name = "用户ID", cellType = ColumnType.NUMERIC)
    private Long operatorUserId;

    @Excel(name = "操作模块")
    private String title;

    @Excel(name = "操作类型", readConverterExp = "0=其他,1=新增,2=修改,3=删除,4=授权,5=导出,6=导入,7=强退,8=生成代码,9=清空数据")
    private Integer businessType;

    private String method;

    @Excel(name = "请求方式")
    private String requestMethod;

    @Excel(name = "操作人员")
    private String operName;

    private String operUrl;

    @Excel(name = "操作地址")
    private String operIp;

    private String operParam;
    private String jsonResult;

    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    private Integer status;

    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operTime;

    @Excel(name = "消耗时间", suffix = "毫秒")
    private Long costTime;

    public Long getOperId() { return operId; }
    public void setOperId(Long operId) { this.operId = operId; }
    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getBusinessType() { return businessType; }
    public void setBusinessType(Integer businessType) { this.businessType = businessType; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public String getOperName() { return operName; }
    public void setOperName(String operName) { this.operName = operName; }
    public String getOperUrl() { return operUrl; }
    public void setOperUrl(String operUrl) { this.operUrl = operUrl; }
    public String getOperIp() { return operIp; }
    public void setOperIp(String operIp) { this.operIp = operIp; }
    public String getOperParam() { return operParam; }
    public void setOperParam(String operParam) { this.operParam = operParam; }
    public String getJsonResult() { return jsonResult; }
    public void setJsonResult(String jsonResult) { this.jsonResult = jsonResult; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public Date getOperTime() { return operTime; }
    public void setOperTime(Date operTime) { this.operTime = operTime; }
    public Long getCostTime() { return costTime; }
    public void setCostTime(Long costTime) { this.costTime = costTime; }
}
