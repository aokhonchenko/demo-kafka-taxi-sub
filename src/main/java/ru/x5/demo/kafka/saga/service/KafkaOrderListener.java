package ru.x5.demo.kafka.saga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.x5.demo.kafka.saga.config.TaxiProperties;
import ru.x5.demo.kafka.saga.dto.OrderUpdateDto;
import ru.x5.demo.kafka.saga.enums.TaxiStatus;
import ru.x5.demo.kafka.saga.model.Result;

@Service
public class KafkaOrderListener {

    private final Logger log = LoggerFactory.getLogger(KafkaOrderListener.class);

    private final TaxiService taxiService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final TaxiProperties taxiProperties;

    private final ObjectMapper mapper;

    public KafkaOrderListener(
            TaxiService taxiService,
            KafkaTemplate<String, String> kafkaTemplate,
            TaxiProperties taxiProperties,
            ObjectMapper mapper) {
        this.taxiService = taxiService;
        this.kafkaTemplate = kafkaTemplate;
        this.taxiProperties = taxiProperties;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "${app.topic.income-order-topic}", groupId = "transfer")
    public void processIncome(@Payload OrderUpdateDto order, Acknowledgment acknowledgment) {

        log.info("Получен новый статус [{}] для заказа {}", order.getStatus(), order.getOrderId());

        if ("pending".equalsIgnoreCase(order.getStatus())) {
            newOrder(order.getOrderId(), acknowledgment);
            return;
        }
        if ("error".equalsIgnoreCase(order.getStatus())) {
            decline(order.getOrderId(), acknowledgment);
            return;
        }
        if ("done".equalsIgnoreCase(order.getStatus())) {
            approve(order.getOrderId(), acknowledgment);
        }
    }

    private void newOrder(String orderId, Acknowledgment acknowledgment) {
        log.info("Получен запрос на такси для заказа {}", orderId);
        Result result = new Result();
        try {

            // lets get some taxi
            Integer ticketId = taxiService.getNewTaxi(orderId);
            result.setOrderId(Integer.getInteger(orderId));
            result.setTicketId(ticketId);

            kafkaTemplate.send(
                    taxiProperties.getOutcomeResultTopic(), mapper.writeValueAsString(result));
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error("Отмечаем заказ такси как неудачный. Order = {}", orderId);
            log.error(ex.getMessage(), ex);
            result.setOrderId(Integer.getInteger(orderId));
            result.setStatus(TaxiStatus.ERROR);
            try {
                kafkaTemplate.send(
                        taxiProperties.getOutcomeResultTopic(), mapper.writeValueAsString(result));
            } catch (JsonProcessingException ignore) {
                // don't do it in production. Let's mark this exception as impossible just here.
            }
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void decline(String orderId, Acknowledgment acknowledgment) {
        log.info("Отменяем заказ {}", orderId);
        taxiService.declineTaxi(orderId);
        acknowledgment.acknowledge();
    }

    private void approve(String orderId, Acknowledgment acknowledgment) {
        log.info("Отменяем заказ {}", orderId);
        taxiService.approveTaxi(orderId);
        acknowledgment.acknowledge();
    }
}
