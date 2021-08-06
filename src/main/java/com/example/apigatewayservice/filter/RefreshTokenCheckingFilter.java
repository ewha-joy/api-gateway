package com.example.apigatewayservice.filter;

import com.example.apigatewayservice.redisutil.RedisUtil;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class RefreshTokenCheckingFilter extends AbstractGatewayFilterFactory<RefreshTokenCheckingFilter.Config> {

    Environment env;
    public RefreshTokenCheckingFilter(Environment env){
        super(RefreshTokenCheckingFilter.Config.class);
        this.env = env;
    }

    private static final Logger logger = LoggerFactory.getLogger("jwtValidTest");

    public static class Config{

    }

    @Autowired
    public RedisUtil redisUtil;

    @Override
    public GatewayFilter apply(RefreshTokenCheckingFilter.Config config){
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.substring(7, authorizationHeader.length());

            String result = isJwtValid(jwt);
            if(result.equals("false")){ //만료되었지만, refresh가 존재하는 jwt인지 확인하는 작업 -> 존재할 경우에만 user-service/access_token 가능
                return onError(exchange, "No jwt", HttpStatus.UNAUTHORIZED);
            }else if(result.equals("expire")){
                logger.error("Expired JWT token");
                return onError1(exchange);
            }

            return chain.filter(exchange);
        });
    }


    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error(err);
        return response.setComplete();
    }

    public Mono<Void> onError1(ServerWebExchange exchange){
        int errorCode = 999;
        String errorcode = "{\"errorCode\":" + errorCode +"}";
        byte[] bytes = errorcode.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Flux.just(buffer));
    }


    private String isJwtValid(String jwt){
        String key = env.getProperty("token.secret");
        Long userId = 0L ;
        try {
            // 만료된 jwt에서 userid 추출
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody();

        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }catch (ExpiredJwtException ex) { //expire됐을 때 예외처리로 파싱해줌

            userId = Long.parseLong(ex.getClaims().get("userId").toString());
            // redis에서 userid(key값) 검색
            String result = redisUtil.getData(userId.toString());
            try {
                if (result != null) {
                    Claims claims2 = Jwts.parser().setSigningKey(key).parseClaimsJws(result).getBody();
                    Long Id = Long.parseLong(claims2.get("userId").toString());
                    if (Id == userId) return "true";
                }
            } catch (Exception e){
                logger.error("refresh jwt invalid.");
            }
        }
        catch (Exception e){
            logger.error("JWT wrong");
        }
        return "false";
    }



}

