package com.example.bankcards.service;

import com.example.bankcards.entity.Card;

import java.util.List;

public interface CardService {
    int saveCard(String username);
    int blockCard(String cardNumber);
    int activateCard(String cardNumber);
    int deleteCard(String cardNumber);
    List<Card> getCards();
}
