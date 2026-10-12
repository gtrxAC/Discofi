package fi.gtrxac.discord;

public class CacheEntry {
    private static long usageCounter;

    private Object object;
    private long lastUsed;

    public CacheEntry(Object o) {
        object = o;
    }

    public CacheEntry(Object o, long lastUsed) {
        object = o;
        this.lastUsed = lastUsed;
    }

    public Object getObject() {
        usageCounter++;
        lastUsed = usageCounter;
        return object;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public static long getUsageCounter() {
        return usageCounter;
    }
}