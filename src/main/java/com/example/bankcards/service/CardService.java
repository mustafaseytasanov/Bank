package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardDTO2;
import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CardService {
    //int saveCard(String username);
    int activateCard(String cardNumber);
    int deleteCard(String cardNumber);
    List<Card> getCards();
    Page<CardDTO> getUserCards(String username, int offset, int limit,
                               boolean mask);
    int blockCardRequest(String cardNumber, String username);

    void openCardRequest(String username);

    void makeResponses();
    int makeTransfer(String username, String fromCardNumber,
                     String toCardNumber, int amount);
    CardDTO2 viewBalance(String username, String cardNumber);
}
