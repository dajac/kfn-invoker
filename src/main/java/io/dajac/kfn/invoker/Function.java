package io.dajac.kfn.invoker;

import java.util.Properties;

/**
 * The {@code Function} interface is for stateless mapping of an input record to zero or one new output record. This
 * is a stateless record-by-record operation, {@link #apply(Object, Object))} is invoked individually for each
 * record of a topic.
 * 
 * The {@link #configure(Properties))} method will receive all the key-value pairs set in the <a href="https://github.com/dajac/kfn/blob/master/pkg/apis/kfn/v1alpha1/types.go#L62">definition</a> of the Function
 * before starting the processing of the records.
 * 
 * The types of the respective input key, input value, output key and output value must match the serialisers set in the definition of the Function.
 * 
 * @param <K1> the type of the input key
 * @param <V1> the type of the input value
 * @param <K2> the type of the output key
 * @param <V2> the type of the output value
 */
public interface Function<K1, V1, K2, V2> {

    /**
     * Configure the Function.
     * This is called once when the Function is instanciated.
     * 
     * @param props the key-values pairs set in the definition of the Funtion
     */
    default void configure(Properties props) { }

    /**
     * Transform the record with the given key and value.
     * 
     * @param key the key of the record
     * @param value the valye of the record
     * @return new {@link KeyValue} pair or {@code null}
     */
    KeyValue<K2, V2> apply(final K1 key, final V1 value);

    /**
     * Close this Function and clean up any resources.
     */
    default void close() { }
}
