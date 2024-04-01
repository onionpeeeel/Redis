import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TimedCache<K, V> {

    private final ConcurrentHashMap<K, Long> expirationTimes;

    private final ConcurrentHashMap<K, V> cache;

    public TimedCache() {
        this.expirationTimes = new ConcurrentHashMap<>();
        this.cache = new ConcurrentHashMap<>();
    }

    public void put(K key, V value, long timeout) {
        long expirationTime = timeout;
        if (timeout != -1) {
            expirationTime = System.currentTimeMillis() + timeout;
        }
        expirationTimes.put(key, expirationTime);
        cache.put(key, value);
    }

    public V get(K key) {
        Long expirationTime = expirationTimes.get(key);
        if (Objects.nonNull(expirationTime) && expirationTime != -1 &&
            expirationTime < System.currentTimeMillis()) {
            expirationTimes.remove(key);
            cache.remove(key);
            return null;
        }
        return cache.get(key);
    }

    public void remove(K key) {
        expirationTimes.remove(key);
        cache.remove(key);
    }

    public void cleanUp() {
        long currentTime = System.currentTimeMillis();
        expirationTimes.forEach((key, expirationTime) -> {
            if (expirationTime < currentTime) {
                expirationTimes.remove(key);
                cache.remove(key);
            }
        });
    }
}
