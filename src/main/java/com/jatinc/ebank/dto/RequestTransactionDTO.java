package com.jatinc.ebank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestTransactionDTO {
    @JsonFormat(shape=JsonFormat.Shape.STRING,
            pattern = "yyyy-MM")
    private YearMonth month;

    private Integer page;

    private Integer size;

    private String currency;
}
