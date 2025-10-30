package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardDTO2;
import com.example.bankcards.entity.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserMessageRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import lombok.AllArgsConstructor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
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

    private static final String cryptoPassword = "cardPassword";

    @Override
    public String encrypt(String value) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(cryptoPassword);
        return encryptor.encrypt(value);
    }

    @Override
    public String decrypt(String value) {
        StandardPBEStringEncryptor decrypter = new StandardPBEStringEncryptor();
        decrypter.setPassword(cryptoPassword);
        return decrypter.decrypt(value);
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
    public Page<CardDTO> getUserCards(String username, int offset, int limit,
                                      boolean mask) {
        Pageable page = PageRequest.of(offset, limit);
        Optional<User> user = userRepository.findByUsername(username);
        long userId = user.get().getId();
        Page<Card> cardPage = cardRepository.findByUser_Id(userId, page);
        List<CardDTO> cardDTOList = new ArrayList<>();
        for (Card card : cardPage.getContent()) {
            CardDTO cardDTO = new CardDTO(
                    decrypt(card.getNumber()),
                    card.getPeriod(),
                    card.getStatus(),
                    card.getBalance()
            );
            if (mask) {
                cardDTO.setNumber("**** **** **** " + decrypt(card.getNumber())
                        .substring(15));
            }
            cardDTOList.add(cardDTO);
        }
        return new PageImpl<>(cardDTOList, page, cardPage.getTotalElements());
    }

    @Override
    public void blockCardRequest(String cardNumber, String username) {
        UserMessage userMessage = new UserMessage();
        userMessage.setAction(Action.BLOCK);
        userMessage.setCardNumber(cardNumber);
        Optional<User> user = userRepository.findByUsername(username);
        long userId = user.get().getId();
        userMessage.setUserId(userId);
        messageRepository.save(userMessage);
    }

    @Override
    public void openCardRequest(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        long userId = user.get().getId();
        UserMessage userMessage = new UserMessage();
        userMessage.setAction(Action.CREATE);
        userMessage.setUserId(userId);
        messageRepository.save(userMessage);
    }

    @Override
    public void activateCardRequest(String cardNumber, String username) {
        UserMessage userMessage = new UserMessage();
        userMessage.setAction(Action.ACTIVATE);
        userMessage.setCardNumber(cardNumber);
        Optional<User> user = userRepository.findByUsername(username);
        long userId = user.get().getId();
        userMessage.setUserId(userId);
        messageRepository.save(userMessage);
    }

    @Override
    public int checkCardAndUser(String cardNumber, String username) {

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            List<Card> cardList = cardRepository.findByUser_Id(user.get().getId());
            String encryptedCardNumber2;
            for (Card card : cardList) {
                encryptedCardNumber2 = decrypt(card.getNumber());
                System.out.println(encryptedCardNumber2);
                if (encryptedCardNumber2.equals(cardNumber)) {
                    return 0;
                }
            }
        }
        return 1;
    }

    @Override
    public void makeResponses() {
        List<UserMessage> userMessageList = messageRepository.findAll();
        String cardNumber;
        List<Card> cardList;
        for (UserMessage userMessage : userMessageList) {
            Action action = userMessage.getAction();
            switch (action) {
                case BLOCK:
                    cardNumber = userMessage.getCardNumber();
                    cardList = cardRepository.findByUser_Id(
                            userMessage.getUserId());
                    for (Card card: cardList) {
                        if (decrypt(card.getNumber()).equals(cardNumber)) {
                            card.setStatus(Status.BLOCKED);
                            cardRepository.save(card);
                            break;
                        }
                    }
                    break;
                case CREATE:
                    Card card2 = new Card();
                    while (true) {
                        String newCardNumber = getNewCardNumber();
                        
                        if (cardRepository.findByNumber(
                                this.encrypt(newCardNumber)).isEmpty()) {
                            Optional<User> user = userRepository.findById(userMessage.getUserId());
                            if (user.isPresent()) {
                                card2.setUser(user.get());
                                card2.setNumber(this.encrypt(newCardNumber));
                                LocalDate today = LocalDate.now();
                                LocalDate date = LocalDate.of(today.getYear() + 5,
                                        today.getMonth(), today.getDayOfMonth());
                                card2.setPeriod(date);
                                card2.setStatus(Status.ACTIVE);
                                card2.setBalance(0.0);
                                cardRepository.save(card2);
                                break;
                            }
                        }
                    }
                    break;
                case ACTIVATE:
                    cardNumber = userMessage.getCardNumber();
                    cardList = cardRepository.findByUser_Id(
                            userMessage.getUserId());
                    for (Card card: cardList) {
                        if (decrypt(card.getNumber()).equals(cardNumber)) {
                            card.setStatus(Status.ACTIVE);
                            cardRepository.save(card);
                            break;
                        }
                    }
                    break;
                default:
                    System.out.println("Invalid action");
                    break;
            }

        }
        messageRepository.deleteAll();
    }

    private static String getNewCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (true) {
            int min = 1000;
            int max = 9999;
            int randomNumber = min + random.nextInt(max - min + 1);
            sb.append(randomNumber);
            i++;
            if (i == 4) {
                break;
            }
            sb.append(" ");
        }
        return sb.toString();
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
