package ru.x5.demo.kafka.saga.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.x5.demo.kafka.saga.domain.TaxiOrder;
import ru.x5.demo.kafka.saga.enums.TaxiStatus;
import ru.x5.demo.kafka.saga.exceptions.TaxiNotFoundException;
import ru.x5.demo.kafka.saga.repository.TaxiOrderRepository;

import java.security.SecureRandom;

@Service
public class TaxiService {

    private final TaxiOrderRepository taxiOrderRepository;

    public TaxiService(TaxiOrderRepository taxiOrderRepository) {
        this.taxiOrderRepository = taxiOrderRepository;
    }

    @Transactional
    public Integer getNewTaxi(String orderId) {
        // some synthetic errors
        int random = new SecureRandom().nextInt(20);
        if (random == 0) {
            throw new TaxiNotFoundException("Не удалось заказать такси");
        }

        TaxiOrder taxiOrder = new TaxiOrder();
        taxiOrder.setOrderId(orderId);
        taxiOrder = taxiOrderRepository.save(taxiOrder);
        return taxiOrder.getId();
    }

    public void declineTaxi(String orderId) {
        TaxiOrder taxi = taxiOrderRepository.findByOrderId(orderId);
        taxi.setStatus(TaxiStatus.ERROR);
        taxiOrderRepository.save(taxi);
    }

    public void approveTaxi(String orderId) {
        TaxiOrder taxi = taxiOrderRepository.findByOrderId(orderId);
        taxi.setStatus(TaxiStatus.APPROVED);
        taxiOrderRepository.save(taxi);
    }
}
