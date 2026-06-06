package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.client.FulfillmentClient;
import com.zhangyuan.payment.domain.model.CompensationEvent;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.repository.CompensationEventRepository;
import com.zhangyuan.payment.domain.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompensationService {

    private static final Logger log = LoggerFactory.getLogger(CompensationService.class);
    private static final int MAX_RETRIES = 5;

    private final CompensationEventRepository compensationEventRepository;
    private final FulfillmentClient fulfillmentClient;
    private final PaymentRepository paymentRepository;

    public CompensationService(CompensationEventRepository compensationEventRepository,
                               FulfillmentClient fulfillmentClient,
                               PaymentRepository paymentRepository) {
        this.compensationEventRepository = compensationEventRepository;
        this.fulfillmentClient = fulfillmentClient;
        this.paymentRepository = paymentRepository;
    }

    public CompensationEvent createFulfillEvent(String paymentNo, String orderNo) {
        CompensationEvent event = new CompensationEvent(paymentNo, "FULFILL_ORDER",
                "{\"orderNo\":\"" + orderNo + "\"}");
        return compensationEventRepository.save(event);
    }

    @Scheduled(fixedRate = 30_000)
    @Transactional
    public void compensatePendingEvents() {
        log.debug("CompensationService: scanning pending events");
        var events = compensationEventRepository.findPendingEvents(MAX_RETRIES, 50);

        for (CompensationEvent event : events) {
            try {
                var paymentOpt = paymentRepository.findByPaymentNo(event.getPaymentNo());
                if (paymentOpt.isEmpty() || !"SUCCESS".equals(paymentOpt.get().getStatus())) {
                    event.markFailed("Payment not found or not SUCCESS");
                    compensationEventRepository.save(event);
                    continue;
                }
                if ("FULFILL_ORDER".equals(event.getEventType())) {
                    var payload = event.getPayloadJson();
                    String orderNo = extractOrderNo(payload);
                    if (orderNo == null) {
                        event.markFailed("Invalid payload");
                        compensationEventRepository.save(event);
                        continue;
                    }
                    try {
                        fulfillmentClient.fulfillOrder(orderNo);
                        event.markProcessed();
                        log.info("Compensation succeed: paymentNo={}, orderNo={}", event.getPaymentNo(), orderNo);
                    } catch (Exception e) {
                        event.incrementRetry(e.getMessage());
                        log.warn("Compensation retry: paymentNo={}, retry={}/{}",
                                event.getPaymentNo(), event.getRetryCount(), MAX_RETRIES);
                    }
                }
                compensationEventRepository.save(event);
            } catch (Exception e) {
                log.error("Compensation failed for event id={}", event.getId(), e);
            }
        }
    }

    private String extractOrderNo(String payload) {
        if (payload == null) return null;
        String key = "\"orderNo\":\"";
        int start = payload.indexOf(key);
        if (start < 0) return null;
        start += key.length();
        int end = payload.indexOf("\"", start);
        return end > start ? payload.substring(start, end) : null;
    }
}
