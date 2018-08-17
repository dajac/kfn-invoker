package io.dajac.kfn.functions;

import io.dajac.kfn.invoker.Function;
import io.dajac.kfn.invoker.KeyValue;

import java.util.Properties;

public class CopyFunction implements Function<byte[], byte[], byte[], byte[]> {

    @Override
    public void configure(Properties properties) {

    }

    @Override
    public KeyValue<byte[], byte[]> apply(byte[] key, byte[] value) {
        return KeyValue.pair(key, value);
    }

    @Override
    public void close() {

    }
}
