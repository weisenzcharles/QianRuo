package com.ruoyi.rentalbusiness.integration.express;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

/** 快递客户端共用的 JDK HTTP 与签名辅助方法。 */
public final class ExpressHttpSupport
{
    static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);

    private ExpressHttpSupport()
    {
    }

    public static HttpClient client()
    {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    public static HttpResponse<String> postForm(HttpClient client, String endpoint, Map<String, String> form)
            throws Exception
    {
        String body = form.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public static String encode(String value)
    {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    public static String md5(String value)
    {
        try
        {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        }
        catch (Exception exception)
        {
            throw new IllegalStateException("无法生成快递接口签名", exception);
        }
    }

    public static boolean success(HttpResponse<String> response)
    {
        return response != null && response.statusCode() >= 200 && response.statusCode() < 300;
    }
}
