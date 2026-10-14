package com.example.userservice.error;


import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
    Environment env;

    public FeignErrorDecoder(Environment env) {
        this.env = env;
    }

    // 어떠한 오류가 발생했을 때 어떻게 처리할 것인지를 구현
    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                break;
            case 404:
                if (methodKey.contains("getOrders")) {
                    return new ResponseStatusException(HttpStatus.valueOf(response.status()),
//                            "User's orders is empty");
                            env.getProperty("order-service.exception.order-is-empty"));
                }
                break;
            default:
                return new Exception(response.reason());
        }

        return null;
    }

}
