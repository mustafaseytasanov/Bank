package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardDTO2;
import com.example.bankcards.service.CardService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final CardService cardService;

    @GetMapping("/my-cards")
    @PreAuthorize("hasRole('USER')")
    public Page<CardDTO> getMyCards(
            @RequestParam("offset") Integer offset,
            @RequestParam("limit") Integer limit,
            @RequestParam(value = "hiding", required = false) String hiding
    ) {
        boolean mask = hiding != null;
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return cardService.getUserCards(username, offset, limit, mask);
    }

    @PostMapping("/block-card/{cardNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> blockCardRequest(
            @PathVariable String cardNumber) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        int returnedValue = cardService.checkCardAndUser(cardNumber, username);
        if (returnedValue == 1) {
            return new ResponseEntity<>("Введенной карты у Вас нет",
                    HttpStatus.NOT_FOUND);
        } else {
            cardService.blockCardRequest(cardNumber, username);
            return new ResponseEntity<>("Запрос по блокировке карты принят",
                    HttpStatus.ACCEPTED);
        }
    }

    @PostMapping("/open-card")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> openCardRequest() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        cardService.openCardRequest(username);
        return new ResponseEntity<>("Запрос об открытии новой карты принят",
                HttpStatus.ACCEPTED);
    }

    @PostMapping("/activate-card/{cardNumber}")
    public ResponseEntity<String> activateCardRequest(
            @PathVariable String cardNumber
    ) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        int returnedValue = cardService.checkCardAndUser(
                cardNumber, username);
        if (returnedValue == 1) {
            return new ResponseEntity<>("Введенной карты у Вас нет",
                    HttpStatus.NOT_FOUND);
        } else {
            cardService.activateCardRequest(cardNumber, username);
            return new ResponseEntity<>("Запрос по активированию карты принят",
                    HttpStatus.ACCEPTED);
        }
    }

    @PostMapping("/make-transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> makeTransfer(
            @RequestParam("from") String fromCardNumber,
            @RequestParam("to") String toCardNumber,
            @RequestParam("amount") Integer amount
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication()
                .getName();
        int returnedValue = cardService.makeTransfer(username, fromCardNumber,
                                                     toCardNumber, amount);
        return switch (returnedValue) {
            case 1 -> new ResponseEntity<>("Карты не существует",
                    HttpStatus.NOT_FOUND);
            case 2 -> new ResponseEntity<>("Переводы только между своими счетами",
                    HttpStatus.FORBIDDEN);
            case 3 -> new ResponseEntity<>("Карты должны быть активными",
                    HttpStatus.FORBIDDEN);
            case 4 -> new ResponseEntity<>("Недостаточно средств",
                    HttpStatus.FORBIDDEN);
            case 0 -> new ResponseEntity<>("Средства успешно переведены",
                    HttpStatus.OK);
            default -> new ResponseEntity<>("",
                    HttpStatus.OK);
        };
    }

    @GetMapping("/view-balance/{cardNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardDTO2> viewBalance(@PathVariable String cardNumber) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        CardDTO2 card = cardService.viewBalance(username, cardNumber);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(card, HttpStatus.OK);
    }

}
