package com.example.bankcards.dto;

import com.example.bankcards.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
public class CardDTO {
    private String number;
    private LocalDate period;
    private Status status;
    private double balance;
}
