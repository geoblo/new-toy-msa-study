package com.example.catalogservice.kafka;

import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

// KafkaConsumerConfig를 사용할 수 있는 실제 컨슈머라는 빈을 하나 등록
// 실제로 리스너를 이용해서 데이터를 가져오고, 가져온 데이터 값을 이용해서 데이터베이스에 업데이트 해보려고 함
@Service
@Slf4j
public class CatalogConsumer {
    CatalogRepository repository;

    @Autowired
    public CatalogConsumer(CatalogRepository repository) {
        this.repository = repository;
    }

    // 중요!
    // 리스너 연결: 프로듀서가 토픽에 어떤 데이터를 전달했을 때 변경을 감지
    @KafkaListener(topics = "example-catalog-topic") // 감지할 토픽의 이름 지정
    public void updateQty(String kafkaMessage) {
        log.info("Kafka Message: ->" + kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 어떠한 데이터가 메시지 안에 들어가 있는지 역직렬화해서 읽어옴
            // 예: 상품의 정보 및 주문 수량
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
            // String -> Object 형태로 바꿈
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        CatalogEntity entity = repository.findByProductId((String) map.get("productId"));
        if (entity != null) {
            entity.setStock(entity.getStock() - (Integer) map.get("qty"));
            repository.save(entity); // 해당 데이터가 존재하면 업데이트로 동작
        }
    }
}
