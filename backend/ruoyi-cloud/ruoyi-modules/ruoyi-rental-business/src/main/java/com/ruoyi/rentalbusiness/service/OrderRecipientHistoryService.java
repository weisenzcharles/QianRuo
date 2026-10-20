package com.ruoyi.rentalbusiness.service;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.rentalbusiness.domain.OrderRecipientHistoryRecord;
import com.ruoyi.rentalbusiness.mapper.OrderRecipientHistoryMapper;
import com.ruoyi.rentalbusiness.model.RegionAddressModel;

/** 订单收件人历史快照应用服务。 */
@Service
public class OrderRecipientHistoryService
{
    private final OrderRecipientHistoryMapper historyMapper;
    private final RegionAddressParser regionAddressParser;

    /**
     * 注入历史快照 Mapper 和地址解析公共服务。
     *
     * @param historyMapper 收件人历史快照 Mapper
     * @param regionAddressParser 地址解析器
     */
    public OrderRecipientHistoryService(OrderRecipientHistoryMapper historyMapper,
            RegionAddressParser regionAddressParser)
    {
        this.historyMapper = historyMapper;
        this.regionAddressParser = regionAddressParser;
    }

    /**
     * 记录只包含收件人字段的快照，供不需要客户字段的旧内部调用方复用。
     *
     * @param tenantId 租户唯一编号
     * @param memberId 商户唯一编号
     * @param orderId 订单唯一编号
     * @param orderNo 订单编号
     * @param recipientName 收件人姓名
     * @param recipientPhone 收件人电话
     * @param recipientAddress 收件人地址
     * @param operatorId 操作人用户唯一编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void record(Long tenantId, Long memberId, Long orderId, String orderNo,
            String recipientName, String recipientPhone, String recipientAddress, Long operatorId)
    {
        record(tenantId, memberId, orderId, orderNo, null, null, null,
                recipientName, recipientPhone, recipientAddress, null, operatorId);
    }

    /**
     * 记录订单完整客户和收件人保存快照。
     *
     * <p>地址解析失败只记录失败状态和原始地址，不阻断订单主流程；这样不完整的自由文本地址
     * 仍可先保存，后续补全地址后再次编辑即可形成新的成功快照。</p>
     *
     * @param tenantId 租户唯一编号
     * @param memberId 商户唯一编号
     * @param orderId 订单唯一编号
     * @param orderNo 订单编号
     * @param customerName 客户姓名快照
     * @param customerPhone 客户电话快照
     * @param customerNickname 客户昵称快照
     * @param recipientName 收件人姓名快照
     * @param recipientPhone 收件人电话快照
     * @param recipientAddress 收件人地址快照
     * @param remark 订单备注快照
     * @param operatorId 操作人用户唯一编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void record(Long tenantId, Long memberId, Long orderId, String orderNo,
            String customerName, String customerPhone, String customerNickname,
            String recipientName, String recipientPhone, String recipientAddress,
            String remark, Long operatorId)
    {
        if (tenantId == null || memberId == null || orderId == null || orderNo == null || orderNo.trim().isEmpty())
        {
            return;
        }
        String normalizedOrderNo = orderNo.trim();
        String normalizedCustomerName = normalizeText(customerName);
        String normalizedCustomerPhone = normalizeText(customerPhone);
        String normalizedCustomerNickname = normalizeText(customerNickname);
        String normalizedRecipientName = normalizeText(recipientName);
        String normalizedRecipientPhone = normalizeText(recipientPhone);
        String normalizedRecipientAddress = normalizeText(recipientAddress);
        RegionAddressModel parsed = regionAddressParser.parse(normalizedRecipientAddress);
        OrderRecipientHistoryRecord record = new OrderRecipientHistoryRecord();
        record.customerName = normalizedCustomerName;
        record.customerPhone = normalizedCustomerPhone;
        record.customerNickname = normalizedCustomerNickname;
        record.recipientName = normalizedRecipientName;
        record.recipientPhone = normalizedRecipientPhone;
        record.recipientAddress = normalizedRecipientAddress;
        record.remark = normalizeText(remark);
        record.provinceCode = parsed.provinceCode;
        record.cityCode = parsed.cityCode;
        record.districtCode = parsed.districtCode;
        record.amapCityCode = parsed.amapCityCode;
        record.provinceName = parsed.provinceName;
        record.cityName = parsed.cityName;
        record.districtName = parsed.districtName;
        record.parseStatus = parsed.parseStatus;
        record.parseMessage = parsed.parseMessage;
        OrderRecipientHistoryRecord current = historyMapper.selectLatest(tenantId, memberId, normalizedOrderNo);
        if (sameRecipientInfo(current, record))
        {
            return;
        }
        historyMapper.clearUsedSnapshots(tenantId, memberId, normalizedOrderNo, operatorId);
        historyMapper.insertSnapshot(tenantId, memberId, orderId, normalizedOrderNo, record, operatorId);
    }

    /**
     * 比较两个收件人快照的业务字段，避免订单重复保存时产生无意义的历史记录。
     *
     * <p>解析说明属于诊断信息，不属于收件人业务数据，因此不参与去重；客户信息、收件人信息、
     * 地址以及行政区划编码和名称发生变化时，才会创建新的当前快照。</p>
     *
     * @param current 当前订单正在使用的快照
     * @param candidate 本次订单保存解析出的候选快照
     * @return 收件人业务信息是否一致
     */
    private boolean sameRecipientInfo(OrderRecipientHistoryRecord current,
            OrderRecipientHistoryRecord candidate)
    {
        if (current == null || candidate == null)
        {
            return false;
        }
        return sameText(current.customerName, candidate.customerName)
                && sameText(current.customerPhone, candidate.customerPhone)
                && sameText(current.customerNickname, candidate.customerNickname)
                && sameText(current.recipientName, candidate.recipientName)
                && sameText(current.recipientPhone, candidate.recipientPhone)
                && sameText(current.recipientAddress, candidate.recipientAddress)
                && sameText(current.remark, candidate.remark)
                && sameText(current.provinceCode, candidate.provinceCode)
                && sameText(current.cityCode, candidate.cityCode)
                && sameText(current.districtCode, candidate.districtCode)
                && sameText(current.amapCityCode, candidate.amapCityCode)
                && sameText(current.provinceName, candidate.provinceName)
                && sameText(current.cityName, candidate.cityName)
                && sameText(current.districtName, candidate.districtName);
    }

    /**
     * 按业务语义比较文本字段，将 null、空字符串和首尾空白视为同一值。
     *
     * @param left 第一个文本值
     * @param right 第二个文本值
     * @return 两个文本值是否相同
     */
    private boolean sameText(String left, String right)
    {
        return Objects.equals(normalizeText(left), normalizeText(right));
    }

    /**
     * 规范化收件人文本，避免前端控件把空白值提交为不同的字符串而误生成快照。
     *
     * @param value 原始文本
     * @return 去除首尾空白后的文本，空文本返回 null
     */
    private String normalizeText(String value)
    {
        if (value == null)
        {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}
