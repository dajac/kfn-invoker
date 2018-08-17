package io.dajac.kfn.invoker;

import java.util.Objects;

public class KeyValue<K, V> {

    public final K key;
    public final V value;

    public KeyValue(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> KeyValue<K, V> pair(final K key, final V value) {
        return new KeyValue<>(key, value);
    }

    @Override
    public String toString() {
        return "KeyValue(" + key + ", " + value + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof KeyValue)) {
            return false;
        }

        final KeyValue other = (KeyValue) obj;
        return (key == null ? other.key == null : key.equals(other.key))
                && (value == null ? other.value == null : value.equals(other.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
