package com.fisincorporated.soaringforecast.satellite.geos;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class GeosCacheKey implements Key {
    private String currentVersion;

    public GeosCacheKey(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GeosCacheKey) {
            GeosCacheKey other = (GeosCacheKey) o;
            return currentVersion.equals(other.currentVersion);
        }
        return false;
    }

    @Override
    public  int hashCode() {
        return currentVersion.hashCode();
    }
    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ByteBuffer.allocate(Integer.SIZE).putInt(hashCode()).array());
    }
}
