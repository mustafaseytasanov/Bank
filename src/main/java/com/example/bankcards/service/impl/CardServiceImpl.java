package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class CardServiceImpl implements CardService {

    private CardRepository cardRepository;
    private UserRepository userRepository;

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
    public int blockCard(String cardNumber) {
        Optional<Card> card = cardRepository.findByNumber(cardNumber);
        if (card.isPresent()) {
            Card newCard = card.get();
            newCard.setStatus(Status.BLOCKED);
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

}
