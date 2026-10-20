package com.ruoyi.rentalbusiness.service;

import java.net.URI;
import java.net.InetAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.rentalbusiness.enums.MerchantExternalCallbackEventStatus;
import com.ruoyi.rentalbusiness.enums.MerchantExternalCallbackEventType;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryGovernanceMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;

/** 外部订单回调出站服务：事务内写事件，任务线程异步发送并按状态重试。 */
@Service
public class MerchantExternalOrderCallbackService
{
    private static final Logger log = LoggerFactory.getLogger(MerchantExternalOrderCallbackService.class);
    private static final int MAX_ATTEMPTS = 8;
    private static final int BATCH_SIZE = 100;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final MerchantInventoryGovernanceMapper mapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MerchantExternalOrderCallbackService(MerchantInventoryGovernanceMapper mapper, ObjectMapper objectMapper)
    {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
    }

    /** 保存订单创建回调事件；事件与订单绑定在同一事务内提交。 */
    @Transactional(rollbackFor = Exception.class)
    public String enqueueOrderCreated(Long bindingId, Long tenantId, Long memberId, Long orderId,
            String callbackUrl, String callbackSecretHash, MerchantOrderCreateCommand order, String reservationNo)
    {
        if (bindingId == null || tenantId == null || memberId == null || orderId == null
                || blank(callbackUrl) || blank(callbackSecretHash) || order == null)
        {
            throw new ServiceException("外部订单回调参数不完整");
        }
        String eventNo = "EOC-" + UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventNo", eventNo);
        payload.put("eventType", MerchantExternalCallbackEventType.ORDER_CREATED.getCode());
        payload.put("eventTypeLabel", MerchantExternalCallbackEventType.ORDER_CREATED.getLabel());
        payload.put("externalOrderNo", order.externalOrderNo);
        payload.put("orderNo", order.orderNo);
        payload.put("reservationNo", reservationNo);
        payload.put("orderStatus", order.orderStatus);
        payload.put("orderStatusLabel", "待发货");
        payload.put("occurredAt", LocalDateTime.now().toString());
        final String payloadJson;
        try
        {
            payloadJson = objectMapper.writeValueAsString(payload);
        }
        catch (JsonProcessingException exception)
        {
            throw new ServiceException("外部订单回调内容生成失败");
        }
        if (mapper.insertExternalCallbackEvent(eventNo, bindingId, tenantId, memberId, orderId, callbackUrl,
                callbackSecretHash, MerchantExternalCallbackEventType.ORDER_CREATED.getCode(), payloadJson) != 1)
        {
            throw new ServiceException("外部订单回调事件保存失败");
        }
        return eventNo;
    }

    /** 保存外部订单状态变更回调；没有外部订单绑定时不产生无效出站事件。 */
    @Transactional(rollbackFor = Exception.class)
    public String enqueueOrderStatusChanged(Long tenantId, Long memberId, Long orderId,
            Integer previousOrderStatus, Integer orderStatus, String changeReason)
    {
        if (tenantId == null || memberId == null || orderId == null || orderStatus == null)
        {
            throw new ServiceException("外部订单状态回调参数不完整");
        }
        Map<String, Object> binding = mapper.selectExternalOrderBindingByOrder(tenantId, memberId, orderId);
        if (binding == null)
        {
            return null;
        }
        String callbackUrl = string(binding, "callbackUrl");
        String callbackSecretHash = string(binding, "callbackSecretHash");
        Long bindingId = stringLong(binding, "bindingId");
        if (bindingId == null || blank(callbackUrl) || blank(callbackSecretHash))
        {
            throw new ServiceException("外部订单回调配置不完整");
        }
        String eventNo = "EOC-" + UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventNo", eventNo);
        payload.put("eventType", MerchantExternalCallbackEventType.ORDER_STATUS_CHANGED.getCode());
        payload.put("eventTypeLabel", MerchantExternalCallbackEventType.ORDER_STATUS_CHANGED.getLabel());
        payload.put("externalOrderNo", string(binding, "externalOrderNo"));
        payload.put("orderNo", string(binding, "orderNo"));
        payload.put("reservationNo", string(binding, "reservationNo"));
        payload.put("previousOrderStatus", previousOrderStatus);
        payload.put("previousOrderStatusLabel", orderStatusLabel(previousOrderStatus));
        payload.put("orderStatus", orderStatus);
        payload.put("orderStatusLabel", orderStatusLabel(orderStatus));
        payload.put("changeReason", blank(changeReason) ? "订单状态变更" : changeReason.trim());
        payload.put("occurredAt", LocalDateTime.now().toString());
        final String payloadJson;
        try
        {
            payloadJson = objectMapper.writeValueAsString(payload);
        }
        catch (JsonProcessingException exception)
        {
            throw new ServiceException("外部订单状态回调内容生成失败");
        }
        if (mapper.insertExternalCallbackEvent(eventNo, bindingId, tenantId, memberId, orderId, callbackUrl,
                callbackSecretHash, MerchantExternalCallbackEventType.ORDER_STATUS_CHANGED.getCode(), payloadJson) != 1)
        {
            throw new ServiceException("外部订单状态回调事件保存失败");
        }
        return eventNo;
    }

    /** 发送当前到期的回调事件；每次发送前先抢占事件，支持多实例并行调度。 */
    public int dispatchDue()
    {
        List<Map<String, Object>> events = mapper.selectDueExternalCallbackEvents(BATCH_SIZE);
        if (events == null || events.isEmpty())
        {
            return 0;
        }
        int delivered = 0;
        for (Map<String, Object> event : events)
        {
            Long eventId = number(event, "eventId");
            if (eventId == null || mapper.claimExternalCallbackEvent(eventId,
                    MerchantExternalCallbackEventStatus.PENDING.getCode(),
                    MerchantExternalCallbackEventStatus.RETRYING.getCode(),
                    MerchantExternalCallbackEventStatus.SENDING.getCode()) != 1)
            {
                continue;
            }
            int attempt = intValue(event.get("attemptCount")) + 1;
            int responseStatus = 0;
            try
            {
                HttpResponse<String> response = send(event);
                responseStatus = response.statusCode();
                if (responseStatus < 200 || responseStatus >= 300)
                {
                    throw new IllegalStateException("回调响应状态码=" + responseStatus);
                }
                mapper.markExternalCallbackDelivered(eventId, MerchantExternalCallbackEventStatus.SENDING.getCode(),
                        MerchantExternalCallbackEventStatus.DELIVERED.getCode(), responseStatus);
                delivered++;
            }
            catch (InterruptedException exception)
            {
                Thread.currentThread().interrupt();
                markFailed(eventId, attempt, responseStatus, "回调发送线程被中断");
            }
            catch (Exception exception)
            {
                markFailed(eventId, attempt, responseStatus, limitError(exception.getMessage()));
                log.warn("外部订单回调发送失败，事件编号={}，尝试次数={}", event.get("eventNo"), attempt);
            }
        }
        return delivered;
    }

    private HttpResponse<String> send(Map<String, Object> event) throws Exception
    {
        String callbackUrl = string(event, "callbackUrl");
        URI uri = URI.create(callbackUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || unsafeHost(uri.getHost()))
        {
            throw new IllegalArgumentException("回调地址必须使用HTTPS且不能指向本机或内网地址");
        }
        String eventNo = string(event, "eventNo");
        String payloadJson = string(event, "payloadJson");
        String signature = hmacSha256(string(event, "callbackSecretHash"), eventNo + "\n" + payloadJson);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("X-External-Callback-Id", eventNo)
                .header("X-External-Callback-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** 回调是用户配置的出站地址，拒绝环回、链路本地和私网解析结果，降低 SSRF 风险。 */
    private boolean unsafeHost(String host)
    {
        if (blank(host) || "localhost".equalsIgnoreCase(host)
                || host.endsWith(".localhost") || host.endsWith(".local"))
        {
            return true;
        }
        try
        {
            for (InetAddress address : InetAddress.getAllByName(host))
            {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress() || address.isMulticastAddress())
                {
                    return true;
                }
            }
            return false;
        }
        catch (java.net.UnknownHostException exception)
        {
            return true;
        }
    }

    private void markFailed(Long eventId, int attempt, int responseStatus, String errorMessage)
    {
        int nextStatus = attempt >= MAX_ATTEMPTS ? MerchantExternalCallbackEventStatus.EXHAUSTED.getCode()
                : MerchantExternalCallbackEventStatus.RETRYING.getCode();
        LocalDateTime nextRetryTime = LocalDateTime.now().plusSeconds(Math.min(3600L, 1L << Math.min(attempt, 10)));
        mapper.markExternalCallbackFailed(eventId, MerchantExternalCallbackEventStatus.SENDING.getCode(), nextStatus,
                attempt, nextRetryTime, responseStatus == 0 ? null : responseStatus, errorMessage);
    }

    private String hmacSha256(String secret, String content)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal(content.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }
        catch (java.security.GeneralSecurityException exception)
        {
            throw new IllegalStateException("外部订单回调签名失败", exception);
        }
    }

    private String limitError(String value)
    {
        String message = blank(value) ? "未知回调错误" : value.trim();
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    private String orderStatusLabel(Integer status)
    {
        if (status != null)
        {
            for (RentalCode.OrderStatus value : RentalCode.OrderStatus.values())
            {
                if (value.getCode().equals(status))
                {
                    return value.getLabel();
                }
            }
        }
        return "未知状态";
    }

    private Long number(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    private Long stringLong(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        if (value == null || String.valueOf(value).trim().isEmpty())
        {
            return null;
        }
        try
        {
            return Long.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }

    private int intValue(Object value)
    {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private String string(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private boolean blank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
