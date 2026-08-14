package com.ruoyi.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.ruoyi.common.core.constant.CacheConstants;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.TokenConstants;
import com.ruoyi.common.core.utils.JwtUtils;
import com.ruoyi.common.core.utils.ServletUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.gateway.config.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

/**
 * 网关鉴权
 * 
 * @author ruoyi
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered
{
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    // 排除过滤的 uri 地址，nacos自行添加
    @Autowired
    private IgnoreWhiteProperties ignoreWhite;

    @Autowired
    private RedisService redisService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutate = request.mutate();

        String url = request.getURI().getPath();
        // 跳过不需要验证的路径
        if (StringUtils.matches(url, ignoreWhite.getWhites()))
        {
            return chain.filter(exchange);
        }
        String token = getToken(request);
        if (StringUtils.isEmpty(token))
        {
            return unauthorizedResponse(exchange, "令牌不能为空");
        }
        Claims claims = JwtUtils.parseToken(token);
        if (claims == null)
        {
            return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
        }
        String userkey = JwtUtils.getUserKey(claims);
        String clientType = JwtUtils.getClientType(claims);
        if (!isValidClientType(clientType))
        {
            return unauthorizedResponse(exchange, "令牌缺少有效终端身份，请重新登录");
        }
        if (!matchesNamespace(url, clientType))
        {
            return forbiddenResponse(exchange, "当前登录终端无权访问该接口");
        }
        String requestedClientType = request.getHeaders().getFirst(Constants.CLIENT_TYPE_HEADER);
        if (StringUtils.isEmpty(requestedClientType))
        {
            return forbiddenResponse(exchange, "请求缺少终端标识");
        }
        if (!clientType.equals(requestedClientType))
        {
            return forbiddenResponse(exchange, "请求终端与登录终端不一致");
        }
        boolean islogin = redisService.hasKey(getTokenKey(clientType, userkey));
        if (!islogin)
        {
            return unauthorizedResponse(exchange, "登录状态已过期");
        }
        String userid = JwtUtils.getUserId(claims);
        String username = JwtUtils.getUserName(claims);
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(username))
        {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }

        // 设置用户信息到请求
        addHeader(mutate, SecurityConstants.USER_KEY, userkey);
        addHeader(mutate, SecurityConstants.DETAILS_USER_ID, userid);
        addHeader(mutate, SecurityConstants.DETAILS_USERNAME, username);
        mutate.headers(headers -> {
            headers.remove(Constants.CLIENT_TYPE_HEADER);
            headers.add(Constants.CLIENT_TYPE_HEADER, clientType);
        });
        // 内部请求来源参数清除
        removeHeader(mutate, SecurityConstants.FROM_SOURCE);
        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value)
    {
        if (value == null)
        {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = ServletUtils.urlEncode(valueStr);
        mutate.header(name, valueEncode);
    }

    private void removeHeader(ServerHttpRequest.Builder mutate, String name)
    {
        mutate.headers(httpHeaders -> httpHeaders.remove(name)).build();
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg)
    {
        log.error("[鉴权异常处理]请求路径:{},错误信息:{}", exchange.getRequest().getPath(), msg);
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, HttpStatus.UNAUTHORIZED);
    }

    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String msg)
    {
        log.warn("[终端越权拦截]请求路径:{},错误信息:{}", exchange.getRequest().getPath(), msg);
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, HttpStatus.FORBIDDEN);
    }

    /**
     * 获取缓存key
     */
    private String getTokenKey(String clientType, String token)
    {
        return CacheConstants.loginTokenKey(clientType, token);
    }

    static boolean isValidClientType(String clientType)
    {
        return Constants.CLIENT_TYPE_PLATFORM.equals(clientType) || Constants.CLIENT_TYPE_MERCHANT.equals(clientType);
    }

    static boolean matchesNamespace(String path, String clientType)
    {
        if (path.equals("/admin") || path.startsWith("/admin/"))
        {
            return Constants.CLIENT_TYPE_PLATFORM.equals(clientType);
        }
        if (path.equals("/merchant") || path.startsWith("/merchant/"))
        {
            return Constants.CLIENT_TYPE_MERCHANT.equals(clientType);
        }
        if (path.equals("/platform") || path.startsWith("/platform/"))
        {
            return Constants.CLIENT_TYPE_PLATFORM.equals(clientType);
        }
        return true;
    }

    /**
     * 获取请求token
     */
    private String getToken(ServerHttpRequest request)
    {
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX))
        {
            token = token.replaceFirst(TokenConstants.PREFIX, StringUtils.EMPTY);
        }
        return token;
    }

    @Override
    public int getOrder()
    {
        return -200;
    }
}
