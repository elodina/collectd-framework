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
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Kafka implements CollectdWriteInterface, CollectdConfigInterface {
    private String brokerList = "localhost:9292";
    private Integer batchSize = 1000;
    private Producer<String, byte[]> kafkaProducer;
    private static final SpecificDatumWriter<Metric> avroEventWriter = new SpecificDatumWriter<>(Metric.SCHEMA$);
    private static final EncoderFactory avroEncoderFactory = EncoderFactory.get();

    public Kafka() throws Exception {
        Collectd.registerConfig("Kafka", this);
        Collectd.registerWrite("Kafka", this);
    }

    public int config(OConfigItem ci) {
        for (OConfigItem conf : ci.getChildren()){
            if(Objects.equals(conf.getKey(), "BrokerList")) {
                this.brokerList = conf.getValues().get(0).getString();
            }
            if(Objects.equals(conf.getKey(), "BatchSize")) {
                this.batchSize = conf.getValues().get(0).getNumber().intValue();
            }
        }
        kafkaProducer = createProducer();
        return 0;
    }

    public int write(ValueList vl) {
        DataSet dataset = vl.getDataSet();
        List<Float> values = vl.getValues().stream().map(Number::floatValue).collect(Collectors.toList());
        Metric metric = new Metric(dataset.getType(), values);
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
        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>("collectd", metric.getType(), stream.toByteArray());
        try {
            kafkaProducer.send(producerRecord).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
