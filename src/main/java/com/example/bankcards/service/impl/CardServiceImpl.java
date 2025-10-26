package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardDTO2;
import com.example.bankcards.entity.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserMessageRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class CardServiceImpl implements CardService {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private UserMessageRepository messageRepository;

    public int saveCard(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            Card card = new Card();
            // Generation of number
            Random random = new Random();
            long min = 1_000_000_000_000_000L; // 10^15
            long max = 9_999_999_999_999_999L; // 10^16 - 1
            long randomNumber = min + (long)(random.nextDouble() * (max - min + 1));
            card.setNumber(String.valueOf(randomNumber));
            card.setUser(user.get());
            card.setStatus(Status.ACTIVE);
            card.setBalance(0.0);
            LocalDate now = LocalDate.now();
            Date date = new Date();
            date.setYear(now.getYear());
            date.setMonth(now.getMonthValue());
            card.setPeriod(date);
            cardRepository.save(card);
            return 0;
        }
        return 1;
    }

    @Override
    public int activateCard(String cardNumber) {
        Optional<Card> card = cardRepository.findByNumber(cardNumber);
        if (card.isPresent()) {
            Card newCard = card.get();
            newCard.setStatus(Status.ACTIVE);
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteCard(String cardNumber) {
        Optional<Card> card = cardRepository.findByNumber(cardNumber);
        if (card.isPresent()) {
            cardRepository.delete(card.get());
            return 0;
        }
        return 1;
    }

    @Override
    public List<Card> getCards() {
        return cardRepository.findAll();
    }

    @Override
    public Page<CardDTO> getUserCards(String username, int offset, int limit) {
        Pageable page = PageRequest.of(offset, limit);
        Optional<User> user = userRepository.findByUsername(username);
        long userId = user.get().getId();
        Page<Card> cardPage = cardRepository.findByUser_Id(userId, page);
        List<CardDTO> cardDTOList = new ArrayList<>();
        for (Card card : cardPage.getContent()) {
            CardDTO cardDTO = new CardDTO(
                    card.getNumber(),
                    card.getPeriod(),
                    card.getStatus(),
                    card.getBalance()
            );
            cardDTOList.add(cardDTO);
        }
        return new PageImpl<>(cardDTOList, page, cardPage.getTotalElements());
    }

    @Override
    public int blockCardRequest(String cardNumber, String username) {
        Optional<Card> card = cardRepository.findByNumber(cardNumber);
        if (card.isEmpty()) {
            return 1;
        }
        Optional<User> user = userRepository.findByUsername(username);
        long userId = user.get().getId();
        if (userId != card.get().getUser().getId()) {
            return 2;
        }
        UserMessage userMessage = new UserMessage();
        userMessage.setAction(Action.BLOCK);
        userMessage.setCardNumber(cardNumber);
        messageRepository.save(userMessage);
        return 0;
    }

    @Override
    public void makeResponses() {
        List<UserMessage> userMessageList = messageRepository.findAll();
        for (UserMessage userMessage : userMessageList) {
            if (userMessage.getAction() == Action.BLOCK) {
                String cardNumber = userMessage.getCardNumber();
                Optional<Card> card = cardRepository.findByNumber(cardNumber);
                card.ifPresent(newCard -> newCard.setStatus(Status.BLOCKED));
            }
        }
        messageRepository.deleteAll();
    }

    @Override
    public int makeTransfer(String username, String fromCardNumber,
                            String toCardNumber, int amount) {
        Optional<Card> card1 = cardRepository.findByNumber(fromCardNumber);
        Optional<Card> card2 = cardRepository.findByNumber(toCardNumber);
        if (card1.isEmpty() || card2.isEmpty()) {
            return 1;
        }
        Card fromCard = card1.get();
        Card toCard = card2.get();
        Optional<User> user = userRepository.findByUsername(username);
        long userId = user.get().getId();
        long card1UserId = fromCard.getUser().getId();
        long card2UserId = toCard.getUser().getId();
        if ((userId != card1UserId) || (userId != card2UserId)) {
            return 2;
        }
        if ((fromCard.getStatus() != Status.ACTIVE)
                || (toCard.getStatus() != Status.ACTIVE)) {
            return 3;
        }
        double balance = fromCard.getBalance();
        if (balance < amount) {
            return 4;
        }
        fromCard.setBalance(fromCard.getBalance() - amount);
        toCard.setBalance(toCard.getBalance() + amount);
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        return 0;
    }

    @Override
    public CardDTO2 viewBalance(String username, String cardNumber) {
        Optional<Card> card = cardRepository.findByNumber(cardNumber);
        if (card.isEmpty()) {
            return null;
        }
        Optional<User> user = userRepository.findByUsername(username);
        long userId = user.get().getId();
        if (userId != card.get().getUser().getId()) {
            return null;
        }
        return new CardDTO2(cardNumber, card.get().getBalance());
    }

}
