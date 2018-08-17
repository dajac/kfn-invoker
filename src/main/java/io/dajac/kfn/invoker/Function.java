package io.dajac.kfn.invoker;

import java.util.Properties;

public interface Function<K1, V1, K2, V2> {

    default void configure(Properties props) { }

    KeyValue<K2, V2> apply(final K1 key, final V1 value);

    default void close() { }
}
