package com.example.pomelo.model;

import com.example.pomelo.exceptions.CustomException;
import com.example.pomelo.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentsManager extends EventsManager{
    public void payOff(String username, String cardNumber, int amount) throws CustomException {
        int txnId = addEvent(username, cardNumber, amount, "PAYMENT");
        Thread backgroundThread = new Thread(() -> {
            try {
                Thread.sleep(5000*60); // To simulate processing payment by bank for 5 minutes
                settleEvent(username, cardNumber, txnId);
            } catch (InterruptedException | CustomException e) {
                log.error("Error: ", e);
            }
        });
        backgroundThread.start();
    }

    public void cancelPayment(String username, int txnId) throws CustomException {
        modifyEvent(username, -1, txnId, Constants.CANCEL_PAYMENT_ACTION);
    }
}
