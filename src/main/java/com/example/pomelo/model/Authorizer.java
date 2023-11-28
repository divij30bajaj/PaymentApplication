package com.example.pomelo.model;

import com.example.pomelo.exceptions.CustomException;
import com.example.pomelo.utils.CardInfo;
import com.example.pomelo.utils.User;
import com.example.pomelo.utils.UserCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class Authorizer {

    @Autowired
    private CardManager cardManager;
    @Autowired
    private UserAccount userAccount;

    public boolean authorizeTransactionAmount(String username, String cardNumber, int amount) throws CustomException {
        UserCard userCard = userAccount.getUserCardDetails(username, cardNumber);
        return userCard.getAvailableCredit() >= amount;
    }

    public boolean authorizePaymentAmount(String username, String cardNumber, int amount) throws CustomException {
        UserCard userCard = userAccount.getUserCardDetails(username, cardNumber);
        return userCard.getPayableBalance() >= amount;
    }
}
