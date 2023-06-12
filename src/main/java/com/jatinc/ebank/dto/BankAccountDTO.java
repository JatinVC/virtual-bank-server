package com.jatinc.ebank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class BankAccountDTO {
    private String iBan;
    private List<PaymentDTO> transactions = new ArrayList<>();
    private LocalDate lastUpdate;

    public BankAccountDTO process(PaymentDTO transaction){
        this.iBan = transaction.getIBan();
        this.transactions.add(transaction);
        this.lastUpdate = LocalDate.now();
        return this;
    }
}
