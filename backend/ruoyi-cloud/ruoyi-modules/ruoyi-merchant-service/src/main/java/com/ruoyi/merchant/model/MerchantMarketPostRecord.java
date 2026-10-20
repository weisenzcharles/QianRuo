package com.ruoyi.merchant.model;

/** 周转市场帖子内部记录，仅供商户服务持久层和状态流转使用。 */
public class MerchantMarketPostRecord
{
    private Long postId;
    private Long memberId;
    private String postNo;
    private String publisherName;
    private Integer boardType;
    private String category;
    private String brand;
    private String modelName;
    private String memorySpec;
    private String conditionLevel;
    private Integer quantity;
    private String priceRangeCode;
    private String city;
    private String serviceTags;
    private Integer recent30dTurnover;
    private Integer idleQty;
    private Integer rentedQty;
    private Integer longIdleQty;
    private Integer postStatus;
    private Integer downRankFlag;
    private String createTime;
    private String updateTime;
    private String remark;

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPostNo() { return postNo; }
    public void setPostNo(String postNo) { this.postNo = postNo; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public Integer getBoardType() { return boardType; }
    public void setBoardType(Integer boardType) { this.boardType = boardType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getMemorySpec() { return memorySpec; }
    public void setMemorySpec(String memorySpec) { this.memorySpec = memorySpec; }
    public String getConditionLevel() { return conditionLevel; }
    public void setConditionLevel(String conditionLevel) { this.conditionLevel = conditionLevel; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getPriceRangeCode() { return priceRangeCode; }
    public void setPriceRangeCode(String priceRangeCode) { this.priceRangeCode = priceRangeCode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getServiceTags() { return serviceTags; }
    public void setServiceTags(String serviceTags) { this.serviceTags = serviceTags; }
    public Integer getRecent30dTurnover() { return recent30dTurnover; }
    public void setRecent30dTurnover(Integer recent30dTurnover) { this.recent30dTurnover = recent30dTurnover; }
    public Integer getIdleQty() { return idleQty; }
    public void setIdleQty(Integer idleQty) { this.idleQty = idleQty; }
    public Integer getRentedQty() { return rentedQty; }
    public void setRentedQty(Integer rentedQty) { this.rentedQty = rentedQty; }
    public Integer getLongIdleQty() { return longIdleQty; }
    public void setLongIdleQty(Integer longIdleQty) { this.longIdleQty = longIdleQty; }
    public Integer getPostStatus() { return postStatus; }
    public void setPostStatus(Integer postStatus) { this.postStatus = postStatus; }
    public Integer getDownRankFlag() { return downRankFlag; }
    public void setDownRankFlag(Integer downRankFlag) { this.downRankFlag = downRankFlag; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
