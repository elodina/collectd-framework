package net.elodina.collectd;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.collectd.api.*;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Kafka implements CollectdWriteInterface, CollectdConfigInterface {
    private String brokerList = "localhost:9292";
    private Integer batchSize = 1000;
    private String topic = "collectd";
    private String hostname;
    private Producer<String, byte[]> kafkaProducer;
    private static final SpecificDatumWriter<Metric> avroEventWriter = new SpecificDatumWriter<>(Metric.SCHEMA$);
    private static final EncoderFactory avroEncoderFactory = EncoderFactory.get();

    public Kafka() {
        Collectd.registerConfig("Kafka", this);
        Collectd.registerWrite("Kafka", this);
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public int config(OConfigItem ci) {
        for (OConfigItem conf : ci.getChildren()){
            if(Objects.equals(conf.getKey(), "BrokerList")) {
                this.brokerList = conf.getValues().get(0).getString();
            }
            if(Objects.equals(conf.getKey(), "BatchSize")) {
                this.batchSize = conf.getValues().get(0).getNumber().intValue();
            }
            if(Objects.equals(conf.getKey(), "Topic")) {
                this.topic = conf.getValues().get(0).getString();
            }
        }
        this.kafkaProducer = createProducer();
        return 0;
    }

    public int write(ValueList vl) {
        List<Double> values = new ArrayList<>();
        for (Number num : vl.getValues()) {
            values.add(num.doubleValue());
        }
        //add all the data
        Metric metric = new Metric(vl.getType(), vl.getPlugin(), values,
                vl.getTime(), vl.getPluginInstance(), vl.getTypeInstance());
        produce(metric);
        return 0;
    }

    private Producer<String, byte[]> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerList);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, this.batchSize);
        props.put(ProducerConfig.RETRIES_CONFIG, "3");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
        props.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, true);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        return new KafkaProducer<>(props);
    }

    private void produce(Metric metric) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryEncoder binaryEncoder = avroEncoderFactory.binaryEncoder(stream, null);
        try {
            avroEventWriter.write(metric, binaryEncoder);
            binaryEncoder.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(this.topic, this.hostname, stream.toByteArray());
        try {
            this.kafkaProducer.send(producerRecord).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
