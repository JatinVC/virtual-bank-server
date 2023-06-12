package com.jatinc.ebank.topology;

import com.jatinc.ebank.dto.BankAccountDTO;
import com.jatinc.ebank.dto.PaymentDTO;
import com.jatinc.ebank.serdes.JsonSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class PaymentTopology {

    public static final String PAYMENTS_STORE = "transaction-store";
    
    /**
     * we define a <code>JsonSerde</code> for our <code>PaymentDTO</code> and <code>BankAccountDTO</code> 
     * to allow the values from the event stream to be deserialized.
     * 
     * since the key of the event also serves as a id for each transaction, we have to also capture
     * the id and store it in the created <code>PaymentDTO</code> for future reference.
     * 
     * we aggregate all transactions into a <code>BankAccountDTO</code> by filtering the iban number used,
     * so we can store all the transactions related to an iban into one object for easier reference.
     * 
     * We also create a local state store for all of our <code>BankAccountDTO</code>s and <code>PaymentDTO</code>s
     * 
     * @return streams topology
     */
    public static Topology buildTopology(){
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        JsonSerde<PaymentDTO> paymentJsonSerde = new JsonSerde<>(PaymentDTO.class);
        JsonSerde<BankAccountDTO> bankAccountSerde = new JsonSerde<>(BankAccountDTO.class);

        streamsBuilder
                .stream("transactions", Consumed.with(Serdes.String(), paymentJsonSerde))
                .peek((transactionKey, transactionValue) -> transactionValue.setPaymentId(transactionKey))
                .groupBy((transactionKey, transactionValue) -> transactionValue.getIBan())
                .aggregate(BankAccountDTO::new,
                        (transactionKey, transactionValue, aggregate) -> aggregate.process(transactionValue),
                        Materialized.<String, BankAccountDTO, KeyValueStore<Bytes, byte[]>>as(PAYMENTS_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(bankAccountSerde))
                .toStream();

        return streamsBuilder.build();
    }
}
