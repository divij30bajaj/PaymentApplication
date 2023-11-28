package com.example.pomelo.utils;

import lombok.Data;

@Data
public class UserCard {
    private String username;
    private String cardNumber;
    private int availableCredit;
    private int payableBalance;

    public UserCard(String username, String cardNumber, int availableCredit, int payableBalance) {
        this.username = username;
        this.cardNumber = cardNumber;
        this.availableCredit = availableCredit;
        this.payableBalance = payableBalance;
    }
}
