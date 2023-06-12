package com.jatinc.ebank.service;

import com.jatinc.ebank.dto.BankAccountDTO;
import com.jatinc.ebank.dto.PaymentDTO;
import com.jatinc.ebank.topology.PaymentTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
public class PaymentService {

    private final KafkaStreams kafkaStreams;
    private final ForExService forExService;

    @Autowired
    public PaymentService(KafkaStreams kafkaStreams, ForExService forExService) {
        this.kafkaStreams = kafkaStreams;
        this.forExService = forExService;
    }

    /**
     * Get all bank transactions from a specic iban account
     * @param iBan - string containing the iban account
     * @return <code>BankAccountDTO</code> object that contains all the transaction activity in the iban account.
     */
    public BankAccountDTO getBankTransactionsByIBan(String iBan){
        return getStore().get(iBan);
    }


    /**
     * Get all the transactions from a iban account in a specific month
     * @param iBan - string containing the iban account
     * @param month - the month to retrieve all the transactions from
     * @param page - the page of results for pagination
     * @param size - the total number of records per page
     * @param currency - the currency that the transactions have to be converted to
     * @return - <code>HashMap</code> object that contains the total debited and credited amount, as well as
     * all the transactions in the selected page.
     */
    public Map<String, Object> getUserTransactionsByMonth(String iBan, YearMonth month, int page, int size,
                                                          String currency){
        try {
            BankAccountDTO userAccountDetails = getBankTransactionsByIBan(iBan);
            Map<String, Object> userTransactionDetails = new HashMap<>();

            if (userAccountDetails.getTransactions().isEmpty()) {
//            if there are no transactions, return an empty json response
                userTransactionDetails.put("debited", 0);
                userTransactionDetails.put("credited", 0);
                userTransactionDetails.put("transactions", 0);
                userTransactionDetails.put("total-pages", 1);
            } else {
//            populate the response object.
                List<PaymentDTO> transactionPage = getMonthTransactions(userAccountDetails, month)
                        .skip(((long) (page - 1) * size))
                        .limit(size)
                        .toList();
                transactionPage = forExService.convertCurrencies(transactionPage, currency);

                BigDecimal debitAmount = transactionPage
                        .stream()
                        .filter((payment -> BigDecimal.valueOf(Double
                                        .parseDouble(payment
                                                .getAmount()
                                                .substring(3)
                                                .trim()))
                                .compareTo(BigDecimal.ZERO) > 0))
                        .map(payment -> BigDecimal.valueOf(Double
                                .parseDouble(payment
                                        .getAmount()
                                        .substring(3)
                                        .trim())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal creditAmount = transactionPage
                        .stream()
                        .filter((payment -> BigDecimal.valueOf(Double
                                        .parseDouble(payment
                                                .getAmount()
                                                .substring(3)
                                                .trim()))
                                .compareTo(BigDecimal.ZERO) < 0))
                        .map(payment -> BigDecimal
                                .valueOf(Double
                                        .parseDouble(payment
                                                .getAmount()
                                                .substring(3)
                                                .trim())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .negate();

                int totalPages = getTotalPages(size, (int) getMonthTransactions(userAccountDetails, month).count());

                userTransactionDetails.put("debited", debitAmount);
                userTransactionDetails.put("credited", creditAmount);
                userTransactionDetails.put("transactions", transactionPage);
                userTransactionDetails.put("total-pages", totalPages);
            }

            return userTransactionDetails;
        }catch(Exception ex){
            log.error("error", ex);
            return Collections.emptyMap();
        }
    }

    /**
     * get transactions in a month
     * @param userAccountDetails - the bank account details for the user
     * @param month - the month of which we need to get all the transactions
     * @return A Stream of <code>PaymentDTO</code> transactions in the selected month.
     */
    private Stream<PaymentDTO> getMonthTransactions(BankAccountDTO userAccountDetails, YearMonth month){
        return userAccountDetails
                .getTransactions()
                .stream()
                .filter(payments -> {
                    LocalDate endOfMonth = month.atEndOfMonth();
                    LocalDate beginningOfMonth = month.atDay(1);
                    return payments.getTransactionDate().isAfter(beginningOfMonth)
                            && payments.getTransactionDate().isBefore(endOfMonth);
                });
    }

    /**
     * get the total pages required to show all the records in a month
     * @param size - integer with the size of the page
     * @param total - the total count of transactions in a month
     * @return the number of pages.
     */
    private Integer getTotalPages(Integer size, Integer total){
        double pages = Math.ceil((double) total / (double) size);
        return (int) pages;
    }

    private ReadOnlyKeyValueStore<String, BankAccountDTO> getStore(){
        return kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                        PaymentTopology.PAYMENTS_STORE,
                        QueryableStoreTypes.keyValueStore()
                )
        );
    }
}
