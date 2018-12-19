package com.fisincorporated.soaringforecast.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import timber.log.Timber;

public class StringUtils {

    public static String readFromAssetsFolder(Context context, String assetsFileName, String searchString, String replacementString) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(assetsFileName);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                if (searchString != null && str.contains(searchString)) {
                    str = str.replace(searchString, replacementString);
                }
                Timber.d("%1$s: %2$s",assetsFileName,str);
                buf.append(str.trim());
            }
            in.close();
            return buf.toString();
        } catch (IOException e) {
            Timber.e("Error opening asset " + assetsFileName);
        } 
        return null;

    }
}
