package com.zhangyuan.modules.payment.domain.service;

import com.zhangyuan.modules.payment.domain.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 支付领域服务，包含支付校验和模拟支付的核心业务规则。
 */
@Service
public class PaymentDomainService {

    private static final Logger log = LoggerFactory.getLogger(PaymentDomainService.class);

    /**
     * 验证支付交易是否处于待支付状态。
     *
     * @param payment 支付领域对象
     * @throws IllegalStateException 状态不正确时抛出
     */
    public void validateCheckout(Payment payment) {
        if (!payment.isPending()) {
            log.warn("Payment is not pending: {}", payment.getPaymentNo());
            throw new IllegalStateException("Payment is not pending: " + payment.getPaymentNo());
        }
    }

}
