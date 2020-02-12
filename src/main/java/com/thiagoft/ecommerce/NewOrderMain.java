package com.thiagoft.ecommerce;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var producer = new KafkaDispatcher();

        var key = UUID.randomUUID().toString();

        var value = "123, 123, 76.30";
        producer.send("ECOMMERCE_NEW_ORDER", key, value);

        var email = "Thanks you for your order! We are processing!";
        producer.send("ECOMMERCE_SEND_EMAIL", key, email);

    }
}
