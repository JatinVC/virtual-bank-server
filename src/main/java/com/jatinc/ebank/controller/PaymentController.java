package com.jatinc.ebank.controller;

import com.jatinc.ebank.dto.RequestTransactionDTO;
import com.jatinc.ebank.service.JwtService;
import com.jatinc.ebank.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtService jwtService;

    @Autowired
    public PaymentController(PaymentService paymentService, JwtService jwtService){
        this.paymentService = paymentService;
        this.jwtService = jwtService;
    }

    /**
     * Get Request function to get all transactions related to a user in a given month
     * @param token - the authenticated token of the user
     * @param query - the request body in the format of <code>RequestTransactionDTO</code>
     * @return JSON containing all the transactions in the requested month, the total debited and credited amount, and
     * the total number of pages.
     */
    @GetMapping(value="/transactions", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getMonthTransactionsById(@RequestHeader(name="Authorization")
                                                                            String token,
                                                                        @RequestBody RequestTransactionDTO query){

        String iBan = jwtService.extractUserKey(token.substring(7));

        if(iBan == null){
            return (ResponseEntity<Map<String, Object>>) ResponseEntity.badRequest();
        }

        log.info("Request '/api/transactions' from user: {} for data in the month: {}", iBan, query.getMonth().toString());

        Map<String, Object> response = paymentService.getUserTransactionsByMonth(iBan, query.getMonth(),
                query.getPage(), query.getSize(), query.getCurrency());

        if(response.isEmpty()){
            return (ResponseEntity<Map<String, Object>>) ResponseEntity.notFound();
        }else{
            return ResponseEntity.ok(response);
        }
    }
}