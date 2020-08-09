package org.soaringforecast.rasp.test;

import org.junit.Before;
import org.junit.Test;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefing;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefingRequest;
import org.soaringforecast.rasp.retrofit.One800WxBriefApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class OneEightHundredWxBriefJsonText {

    SimpleDateFormat departureDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String endpointUrl = "https://www.elabs.testafss.net/Website2/rest/";
    Retrofit retrofit;
    One800WxBriefApi client;


    @Before
    public void createRetrofitWithHeader() {
        createRetrofit();
        client = retrofit.create(One800WxBriefApi.class);
    }

    @Test
    public void shouldGetMETAR() throws Exception {
        Call<ResponseBody> call = client.getMETAR("KORH");
        ResponseBody responseBody = call.execute().body();
        System.out.println( responseBody.string());
        assertNotNull(responseBody);
    }


    @Test
    public void shouldStartEmailBriefing() throws Exception {
        ArrayList<String> turnpointIds = new ArrayList<String>(Arrays.asList("3B3", "KAFN", "KEEN", "3B3"));
        RouteBriefingRequest routeBriefingRequest =   RouteBriefingRequest.newInstance(turnpointIds);
        routeBriefingRequest.setWebUserName("flightservice@soaringforecast.org");
        routeBriefingRequest.setDepartureInstant("");
        String departureTIme = departureDateFormat.format(new Date(System.currentTimeMillis() + 4 * 60  * 60  * 1000) );
        routeBriefingRequest.setDepartureInstant(departureTIme);
        routeBriefingRequest.setFlightDuration("PT04H");
        routeBriefingRequest.setAircraftIdentifier("N68RM");
        routeBriefingRequest.setBriefingType("EMAIL");
        routeBriefingRequest.setEmailAddress("flightservice@soaringforecast.org");
        Call<RouteBriefing>  call = client.getRouteBriefing(routeBriefingRequest.getRestParmString());
        RouteBriefing routeBriefing = call.execute().body();
        assertNotNull(routeBriefing);
        assertNotNull(routeBriefing.returnStatus);
        assertNull(routeBriefing.returnMessages);

    }



    public void createRetrofit() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(40, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .header("Authorization", "Basic " + getBase64UidPwd())
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(endpointUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    private  String getBase64UidPwd() {
        String encoded = Base64.getEncoder().encodeToString(("flightservice@soaringforecast.org" + ":" + "SoaringForecast2020!!!").getBytes());
        System.out.println("Encoded :" + encoded);
        return encoded;
        // Following should give   Sm9lc0ZsaWdodFNlcnZpY2VzOlNlY3JldFBX
       // return Base64.getEncoder().encodeToString(("JoesFlightServices:SecretPW").getBytes());
    }
}
