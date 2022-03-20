package org.soaringforecast.rasp.retrofit;

import android.annotation.SuppressLint;

import org.soaringforecast.rasp.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import timber.log.Timber;


//http://stackoverflow.com/questions/32965790/retrofit-2-0-how-to-print-the-full-json-response
public class LoggingInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {

        if (BuildConfig.DEBUG) {
            Timber.d("inside intercept callback");
        }
        Request request = chain.request();
        long t1 = System.nanoTime();
        String requestLog = String.format("Sending request %s on %s %s",
                request.url(), chain.connection(), request.headers());
        if (request.method().compareToIgnoreCase("post") == 0) {
            requestLog = "\n" + requestLog + "\n" + bodyToString(request);
        }
        if (BuildConfig.DEBUG) {
            Timber.d("request" + "\n" + requestLog);
        }
        Response response = chain.proceed(request);
        long t2 = System.nanoTime();

        @SuppressLint("DefaultLocale")
        String responseLog = String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers());

        String contentType = response.header("Content-Type");
        if (contentType != null && !contentType.startsWith("image")) {
            String bodyString = response.body().string();
            if (BuildConfig.DEBUG) {
            //    Timber.d("response only" + "\n" + bodyString);
                Timber.d("response \n %s \n %s", responseLog , bodyString);
            }
            return response.newBuilder()
                    .body(ResponseBody.create(response.body().contentType(), bodyString))
                    .build();
        } else {
            return chain.proceed(request);
        }

    }


    public static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}