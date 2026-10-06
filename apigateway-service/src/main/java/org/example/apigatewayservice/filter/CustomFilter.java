package org.example.apigatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // Custom Pre Filter
            // 인증 검사, 헤더 추가, 로깅 등을 여기서 처리 가능
            log.info("Custom Pre Filter: request id -> {}", request.getId());

            // 다음 필터로 넘기고, 최종적으로 더 이상 처리할 필터가 없으면 비즈니스 로직을 호출
            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        // Custom Post Filter
                        log.info("Custom Post Filter: response code -> {}", response.getStatusCode());
                    }));
        };
    }
    
    public static class Config {

    }
}
