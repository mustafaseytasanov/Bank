package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CardService cardService;
    private final UserService userService;

    @PostMapping("/process-requests")
    public ResponseEntity<String> makeResponses() {
        cardService.makeResponses();
        return new ResponseEntity<>("Запросы пользователей выполнены",
                                    HttpStatus.OK);
    }

    @PostMapping("/process-expired")
    public ResponseEntity<String> processExpiredCards() {
        cardService.processExpiredCards();
        return new ResponseEntity<>("Отмечены просроченные карты",
                HttpStatus.OK);
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getUsers();
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/all-cards")
    public ResponseEntity<List<Card>> getAllCards() {
        List<Card> cards = cardService.getCards();
        return ResponseEntity.ok().body(cards);
    }

}
