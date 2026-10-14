package com.example.userservice.client;

import com.example.userservice.error.FeignErrorDecoder;
import com.example.userservice.vo.ResponseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//@FeignClient(name = "order-service") // 유레카에 등록된 이름
@FeignClient(name = "order-service", configuration = FeignErrorDecoder.class) // 해당 클라이언트에서 쓸 커스텀 에러디코더 지정
public interface OrderServiceClient {

    // GET http://localhost:8000/order-service/9410e078-ce73-44e1-8647-c0b4437b2c64/orders
//    @GetMapping("/order-service/{userId}/orders")
    @GetMapping("/order-service/{userId}/orders-ng") // 에러 핸들링 테스트용
    List<ResponseOrder> getOrders(@PathVariable("userId") String userId);

}
