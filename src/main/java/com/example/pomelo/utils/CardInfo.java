package com.example.pomelo.utils;

import lombok.Data;

@Data
public class CardInfo {
    private String number;
    private int expiryMonth;
    private int expiryYear;
    private String name;

    public CardInfo(String number, int expiryMonth, int expiryYear, String name) {
        this.number = number;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.name = name;
    }
}
