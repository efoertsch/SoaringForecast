package org.soaringforecast.rasp.app;

public interface CacheTimeListener {

    // provide new time limit for clearing cache
    void cacheTimeLimit(int minutes);
}
