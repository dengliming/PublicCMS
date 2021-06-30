package com.publiccms.common.base;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.publiccms.common.api.PaymentGateway;
import com.publiccms.common.api.TradePaymentProcessor;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.trade.TradePayment;
import com.publiccms.entities.trade.TradePaymentHistory;
import com.publiccms.logic.component.trade.PaymentProcessorComponent;
import com.publiccms.logic.service.trade.TradePaymentHistoryService;
import com.publiccms.logic.service.trade.TradePaymentService;

public abstract class AbstractPaymentGateway implements PaymentGateway {
    @Autowired
    private TradePaymentService service;
    @Autowired
    private TradePaymentHistoryService historyService;
    @Autowired
    private PaymentProcessorComponent paymentProcessorComponent;

    @Override
    public boolean confirmPay(SysSite site, TradePayment payment, HttpServletResponse response) {
        TradePaymentProcessor paymentProcessor = paymentProcessorComponent.get(payment.getTradeType());
        if (null != paymentProcessor && paymentProcessor.paid(payment)) {
            service.processed(site.getId(), payment.getId(), payment.getUserId());
            return true;
        } else {
            TradePaymentHistory history = new TradePaymentHistory(payment.getSiteId(), payment.getId(), CommonUtils.getDate(),
                    TradePaymentHistoryService.OPERATE_PROCESS_ERROR);
            historyService.save(history);
        }
        return false;
    }

    protected String getNotifyUrl(String notifyUrl) {
        if (null != notifyUrl && notifyUrl.startsWith("//")) {
            notifyUrl = "https:" + notifyUrl;
        }
        return notifyUrl;
    }
}
