package com.thiagoft.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

class KafkaService<T> implements Closeable {
    private final KafkaConsumer<String, T> consumer;
    private final ConsumerFunction parse;

    KafkaService(String groupId, String topic, ConsumerFunction parse, Class<T> type) {
        this.parse = parse;
        this.consumer = new KafkaConsumer<>(properties(type,groupId));
        consumer.subscribe(Collections.singletonList(topic));
    }

    public KafkaService(String groupId, Pattern topic, ConsumerFunction parse, Class<T> type) {
        this.parse = parse;
        this.consumer = new KafkaConsumer<>(properties(type, groupId));
        consumer.subscribe(topic);
    }

    void run() {
        while(true) {
            var records = consumer.poll(Duration.ofMillis(100));
            if (!records.isEmpty()) {
                System.out.println("Found " + records.count() + " records");

                for (var record : records) {
                    parse.consume(record);
                }
            }
        }
    }

    private Properties properties(Class<T> type, String groupId) {
        var properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        properties.setProperty(GsonDeserializer.TYPE_CONFIG, type.getName());
        return properties;
    }

    @Override
    public void close() throws IOException {
        consumer.close();
    }
}
