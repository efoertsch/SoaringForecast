package org.soaringforecast.rasp.utils;

import android.content.Context;
import android.support.annotation.StringRes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;

import timber.log.Timber;

public class StringUtils {

    public  String readFromAssetsFolder(Context context, String assetsFileName, String searchString, String replacementString) {
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
//                if (BuildConfig.DEBUG) {
//                    Timber.d("%1$s: %2$s", assetsFileName, str);
//                }
                buf.append(str.trim());
            }
            in.close();
            return buf.toString();
        } catch (IOException e) {
            Timber.e("Error opening asset %1$s", assetsFileName);
        } 
        return null;

    }

    public String stripOldIfNeeded(String examineString) {
        if (examineString.startsWith("old ") && examineString.length() > 4) {
            return examineString.substring(4);
        } else {
            return examineString;
        }
    }

    public static HashMap<String, String> getHashMapFromStringRes(Context context, @StringRes int stringRes) {
        return getHashMapFromString(context.getString(stringRes));
    }

    public static String convertHashMapToJsonString(HashMap<String, String> hashMap){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create();
        Type type = new TypeToken<HashMap<String,String>>(){}.getType();
        return gson.toJson(hashMap, type);
    }

    public static HashMap<String, String> getHashMapFromString(String string) {
        return new Gson().fromJson(string, new TypeToken<HashMap<String, String>>(){}.getType());

    }

    public static LinkedHashMap<String, String> getLinkedHashMapFromStringRes(Context context, @StringRes int stringRes) {
        return new Gson().fromJson(context.getString(stringRes), new TypeToken<LinkedHashMap<String, String>>(){}.getType());

        }


}
