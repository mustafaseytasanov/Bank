package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardDTO2;
import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CardService {

    int deleteCard(String cardNumber);
    List<Card> getCards();
    Page<CardDTO> getUserCards(String username, int offset, int limit,
                               boolean mask);
    void blockCardRequest(String cardNumber, String username);

    void openCardRequest(String username);
    void makeResponses();
    int makeTransfer(String username, String fromCardNumber,
                     String toCardNumber, int amount);
    CardDTO2 viewBalance(String username, String cardNumber);
    void activateCardRequest(String encryptedCardNumber, String username);
    int checkCardAndUser(String cardNumber, String username);
    String encrypt(String value);
    String decrypt(String value);

}
