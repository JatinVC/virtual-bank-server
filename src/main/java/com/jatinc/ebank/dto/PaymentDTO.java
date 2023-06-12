package com.jatinc.ebank.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class PaymentDTO {
    private String paymentId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("iban")
    private String iBan;

    @JsonProperty("transaction_date")
    @JsonFormat(shape=JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @JsonProperty("description")
    private String description;
}
