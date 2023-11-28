package com.example.pomelo.model;

import com.example.pomelo.exceptions.CustomException;
import com.example.pomelo.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class TransactionsManager extends EventsManager {

    public void addNewTransaction(String username, String cardNumber, int amount) throws CustomException {
        int txnId = addEvent(username, cardNumber, amount, "TXN");
        Thread backgroundThread = new Thread(() -> {
            try {
                Thread.sleep(5000*60); // To simulate processing transaction by bank for 5 minutes
                settleEvent(username, cardNumber, txnId);
            } catch (InterruptedException | CustomException e) {
                log.error("Error: ", e);
            }
        });
        backgroundThread.start();
    }

    public void modifyTransaction(String username, int newAmount, int txnId) throws CustomException {
        modifyEvent(username, newAmount, txnId, Constants.MODIFY_TXN_ACTION);
    }

    public void cancelTransaction(String username, int txnId) throws CustomException {
        modifyEvent(username, -1, txnId, Constants.CANCEL_TXN_ACTION);
    }
}
