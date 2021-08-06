package com.example.apigatewayservice.filter;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    Environment env;
    public AuthorizationHeaderFilter(Environment env){
        super(Config.class);
        this.env = env;
    }

    private static final Logger logger = LoggerFactory.getLogger("jwtValidTest");

    public static class Config{

    }

    @Override
    public GatewayFilter apply(Config config){
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.substring(7, authorizationHeader.length());
            String result = isJwtValid(jwt);
            if(result.equals("false")){
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }
            else if(result.equals("expire")){
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
        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody();
            Long userId = Long.parseLong(claims.get("userId").toString());
            return "true";
        } catch (SignatureException ex) {
                logger.error("Invalid JWT signature");
            } catch (MalformedJwtException ex) {
                logger.error("Invalid JWT token");
            } catch (ExpiredJwtException ex) {
                return "expire";
            } catch (UnsupportedJwtException ex) {
                logger.error("Unsupported JWT token");
            } catch (IllegalArgumentException ex) {
                logger.error("JWT claims string is empty.");
            } catch (Exception e){
                logger.error("JWT wrong");
        }
        return "false";
    }



}
