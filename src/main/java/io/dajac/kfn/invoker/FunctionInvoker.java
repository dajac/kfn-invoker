package io.dajac.kfn.invoker;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("ALL")
public class FunctionInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionInvoker.class);

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final String inputTopic;
    private final String outputTopic;

    private final KafkaConsumer consumer;
    private final KafkaProducer producer;

    private final String functionName;
    private final String functionClass;
    private final Function function;

    public FunctionInvoker(Properties properties) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        Properties functionProps = getProperitesWithPrefix(properties, "function.", true);
        this.functionName = functionProps.getProperty("name");
        this.functionClass = functionProps.getProperty("class");
        this.inputTopic = functionProps.getProperty("input");
        this.outputTopic = functionProps.getProperty("output");

        LOG.info("Loading {} function", this.functionClass);

        // Load, instanciate and configure the function
        this.function = (Function) Class.forName(this.functionClass).newInstance();
        this.function.configure(functionProps);

        Properties consumerProps = getProperitesWithPrefix(properties, "consumer.", true);
        this.consumer = new KafkaConsumer<>(consumerProps);

        Properties producerProps = getProperitesWithPrefix(properties, "producer.", true);
        this.producer = new KafkaProducer<>(producerProps);
    }

    public void run() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        LOG.info("Starting Function {}", this.functionName);

        this.isRunning.set(true);

        this.consumer.subscribe(Arrays.asList(this.inputTopic));

        try {
            while (this.isRunning.get()) {
                LOG.debug("Polling new records");
                ConsumerRecords records = this.consumer.poll(1000);
                LOG.debug("Consumed {} records", records.count());

                // TODO(dajac): how to handle futures returned by the send method and how to commit offsets?
                for (Object obj : records) {
                    ConsumerRecord record = (ConsumerRecord) obj;
                    KeyValue result = this.function.apply(record.key(), record.value());
                    
                    if (result != null) {
                        this.producer.send(new ProducerRecord(this.outputTopic, result.key, result.value));
                    }
                }

                LOG.debug("Processed {} records", records.count());
            }
        } catch (WakeupException e) {
            // Ignore
        } catch (Throwable t) {
            LOG.error("Got following error: {}", t.getMessage(), t);
        } finally {
            LOG.info("Shutting down Function {}", this.functionName);
            this.consumer.close();
            this.producer.close();
            this.function.close();
            this.shutdownLatch.countDown();
        }

        LOG.info("Shut down of Function {} complete", this.functionName);
    }

    public void shutdown() {
        LOG.info("Request shutting down the invoker");
        this.isRunning.set(false);
        this.consumer.wakeup();

        try {
            this.shutdownLatch.await();
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    public static Properties getPropsFromFile(String propsFile) throws IOException {
        Properties props = new Properties();
        if (propsFile == null) {
            return props;
        }
        try (FileInputStream propStream = new FileInputStream(propsFile)) {
            props.load(propStream);
        } catch (IOException e) {
            throw new IOException("Couldn't load properties from " + propsFile, e);
        }
        return props;
    }

    public Properties getProperitesWithPrefix(Properties properties, String prefix, boolean strip) {
        Properties result = new Properties();
        for (String property : properties.stringPropertyNames()) {
            if (property.startsWith(prefix) && property.length() > prefix.length()) {
                if (strip)
                    result.put(property.substring(prefix.length()), properties.getProperty(property));
                else
                    result.put(property, properties.getProperty(property));
            }
        }
        return result;
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, IOException {
        if (args.length != 1) {
            LOG.error("Properties file is required to start the instance");
            System.exit(1);
        }

        Properties properties = getPropsFromFile(args[0]);

        FunctionInvoker invoker = new FunctionInvoker(properties);

        Runtime.getRuntime().addShutdownHook(new Thread(invoker::shutdown));

        invoker.run();
    }
}
