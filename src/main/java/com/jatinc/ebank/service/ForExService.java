package com.jatinc.ebank.service;

import com.jatinc.ebank.dto.PaymentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class ForExService {

    @Value("${currency.exchange.rate.url}")
    private String currencyApiUrl;

    @Value("${currency.exchange.rate.api.key}")
    private String currencyApiKey;

    /**
     * convert all the transactions to the provided currency at that day
     * @param transactions - the list of transactions
     * @param currency - the currency that we want to convert to
     * @return - list of transactions with the amount converted to the provided currency at the specified day.
     */
    public List<PaymentDTO> convertCurrencies(List<PaymentDTO> transactions, String currency){
        String baseCurrency = transactions.get(0).getAmount().substring(0, 3);
        HashMap historicalExchangeRates = (HashMap) getExchangeRate(baseCurrency, currency,
                YearMonth.of(2022, 2)).get("data");

        return transactions
                .stream()
                .map(transaction->{
                    String amount = transaction.getAmount().substring(4);
                    String date = transaction.getTransactionDate().toString();
                    HashMap rateMap = (HashMap) historicalExchangeRates.get(date);
                    String rate = rateMap.get(currency).toString();
                    BigDecimal convertedValue = new BigDecimal(amount)
                            .multiply(new BigDecimal(rate));


                    transaction.setAmount(currency + " " + convertedValue.toString());
                    return transaction;
                }).toList();
    }

    /**
     * get historical exchange rate data for all days in a month
     * @param baseCurrency - the base currency which we want to convert from
     * @param convertCurrency - the currency we want to convert to.
     * @param month - the month we want to get all the exchange rate data.
     * @return - response object containing the currency rate for each day.
     */
    private HashMap getExchangeRate(String baseCurrency, String convertCurrency, YearMonth month){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(getFullRequestUrl(baseCurrency, convertCurrency, month), HashMap.class);
    }

    /**
     * get the api request url
     * @param baseCurrency - the base currency which we want to convert from
     * @param convertCurrency - the currency we want to convert to.
     * @param month - the month we want to get all the exchange rate data.
     * @return string containing the api url.
     */
    private String getFullRequestUrl(String baseCurrency, String convertCurrency, YearMonth month){
        StringBuilder requestApi = new StringBuilder();
        requestApi.append(currencyApiUrl);
        requestApi.append("?");
        requestApi.append("apikey=");
        requestApi.append(currencyApiKey);
        requestApi.append("&date_from=");
        requestApi.append(month.atDay(1).toString());
        requestApi.append("&date_to=");
        requestApi.append(month.atEndOfMonth().toString());
        requestApi.append("&base_currency=");
        requestApi.append(baseCurrency);
        requestApi.append("&currencies=");
        requestApi.append(convertCurrency);

        return requestApi.toString();
    }

}
