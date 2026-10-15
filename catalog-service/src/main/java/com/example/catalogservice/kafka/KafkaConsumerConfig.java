package com.example.catalogservice.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

// Kafka Consumer의 Configuration 정보를 구성하는 클래스
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    // ConsumerFactory: Kafka 브로커 접속 정보와 컨슈머 설정(그룹 ID, 역직렬화 방식 등)을 담아
    // KafkaConsumer 객체를 생성하는 팩토리 빈 (토픽은 @KafkaListener에서 지정)
    @Bean
    public ConsumerFactory<String, String> consumerFactory() { // <String, String>은 키, 밸뷰 타입
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // kafka 서버의 주소
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "product-service-group");
        // GROUP ID: kafka에서 토픽에 쌓여있는 메시지를 가져가는 컨슈머들을 그룹핑할 수 있음
        // 지금은 하나밖에 존재하지 않기 때문에 큰 의미는 없지만,
        // 나중에 여러 개의 컨슈머가 데이터를 가져갈 때 우리가 특정한 컨슈머 그룹을 만들어 놓고 전달하고자 하는 그룹을 지정할수 있음

        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // earliest: 토픽에 남아 있는 가장 오래된 메시지부터 읽음
        // latest (기본값): 컨슈머가 접속한 시점 이후에 들어오는 새 메시지부터만 읽음

        // 메시지 키와 값을 문자열로 역직렬화
        // Kafka는 메시지를 키-값 쌍으로 다루고, 이 둘이 각각 다른 타입일 수 있어서 개별적으로 지정
        // 지금은 둘 다 문자열이라 같은 StringDeserializer를 썼지만,
        // 실무에서는 값(value) 쪽에 JSON을 객체로 바로 변환하는 JsonDeserializer 를 쓰는 경우가 많음
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    // KafkaListenerContainerFactory: 토픽에 어떤 변경사항이 있는지 계속 리스닝하고 있는,
    // 즉 이벤트가 발생했을 때 그걸 캐치할 수 있는 리스너 컨테이너를 생성하는 팩토리
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory()); // 위에서 선언한 설정 접속 정보를 가져와 등록

        return kafkaListenerContainerFactory;
    }

}
