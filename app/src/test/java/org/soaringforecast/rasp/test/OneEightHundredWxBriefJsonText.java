package org.soaringforecast.rasp.test;

import org.junit.Before;
import org.junit.Test;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefing;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefingRequest;
import org.soaringforecast.rasp.retrofit.One800WxBriefApi;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        System.out.println(responseBody.string());
        assertNotNull(responseBody);
    }


    @Test
    public void shouldStartEmailBriefing() throws Exception {

        RouteBriefingRequest routeBriefingRequest = RouteBriefingRequest.newInstance();
        routeBriefingRequest.setDeparture("3B3");
        routeBriefingRequest.setRoute("KAFN KEEN");
        routeBriefingRequest.setDestination("3B3");
        routeBriefingRequest.setWebUserName("flightservice@soaringforecast.org");

        String departureTIme = departureDateFormat.format(new Date(System.currentTimeMillis() + 4 * 60 * 60 * 1000));
        routeBriefingRequest.setDepartureInstant(departureTIme);
        routeBriefingRequest.setFlightDuration("PT04H");
        routeBriefingRequest.setAircraftIdentifier("N68RM");
        routeBriefingRequest.setSelectedBriefingType(RouteBriefingRequest.BriefingType.EMAIL);
        routeBriefingRequest.setBriefingResultFormat("PDF");
        routeBriefingRequest.setEmailAddress("flightservice@soaringforecast.org");
        // Call<RouteBriefing>  call = client.getRouteBriefing(routeBriefingRequest.getRestParmString());
        //RouteBriefing routeBriefing = call.execute().body();
        System.out.println("Parms: \n" + routeBriefingRequest.getRestParmString());
        RequestBody body = RequestBody.create(MediaType.parse("\"text/plain\""), routeBriefingRequest.getRestParmString());
        Call<RouteBriefing> call = client.getRouteBriefing(body);
        RouteBriefing routeBriefing = call.execute().body();
        assertNotNull(routeBriefing);
        if (routeBriefing != null && routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
            System.out.println("Code: \n" + routeBriefing.returnCodedMessage.get(0).code);
            System.out.println("Message: \n" + routeBriefing.returnCodedMessage.get(0).message);
        }
        assertNotNull(routeBriefing.returnStatus);
        assertTrue(routeBriefing.returnStatus);

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

    private String getBase64UidPwd() {
        String encoded = Base64.getEncoder().encodeToString(("flightservice@soaringforecast.org" + ":" + "SoaringForecast2020!!!").getBytes());
        System.out.println("Encoded :" + encoded);
        return encoded;
        // Following should give   Sm9lc0ZsaWdodFNlcnZpY2VzOlNlY3JldFBX
        // return Base64.getEncoder().encodeToString(("JoesFlightServices:SecretPW").getBytes());
    }


    @Test
    public void shouldCreateSimpleBriefForNotABriefingWithProductAndTailoringOptions() throws Exception {
        // Not a NGBV2
        String parmString =
                "notABriefing=true&includeCodedMessages=true&type=ICAO&routeCorridorWidth=25&briefingPreferences={\"items\":[\"DD_NTM\",\"TFR\",\"FA\"],\"tailoring\":[]}&outlookBriefing=false&flightRules=VFR&departure=3B3&departureInstant=2020-08-15T18:00:00.000&destination=3B3&route=3B3 AFN EEN 3B3 &flightDuration=PT05H&speedKnots=50&versionRequested=99999999&briefingType=NGB&plainTextTimeZone=EST";

        RequestBody body = RequestBody.create(MediaType.parse("\"text/plain\""), parmString);
        Call<RouteBriefing> call = client.getRouteBriefing(body);
        RouteBriefing routeBriefing = call.execute().body();
        assertNotNull(routeBriefing);
        if (routeBriefing != null && routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
            System.out.println("Code: \n" + routeBriefing.returnCodedMessage.get(0).code);
            System.out.println("Message: \n" + routeBriefing.returnCodedMessage.get(0).message);

        }
    }

    @Test
    public void shouldCreateNGBV2BriefWithProductAndTailoringOptions() throws Exception {
        // Not a NGBV2
        String parmString =
                "includeCodedMessages=true&type=ICAO&aircraftIdentifier=N68RM&routeCorridorWidth=25&briefingPreferences={\"items\":[\"DD_NTM\",\"SEV_WX\",\"METAR\"],\"tailoring\":[\"EXCLUDE_GRAPHICS\",\"EXCLUDE_HISTORICAL_METARS\"]}&outlookBriefing=false&flightRules=VFR&departure=3B3&departureInstant=2020-08-18T19:36:06&destination=3B3&route=KAFN,KEEN&flightDuration=PT04H&webUserName=flightservice@soaringforecast.org&speedKnots=50&versionRequested=99999999&notABriefing=false&briefingType=NGBV2&briefingResultFormat=PDF&emailAddress=flightservice@soaringforecast.org";

        RequestBody body = RequestBody.create(MediaType.parse("\"text/plain\""), parmString);
        Call<RouteBriefing> call = client.getRouteBriefing(body);
        RouteBriefing routeBriefing = call.execute().body();
        assertNotNull(routeBriefing);
        if (routeBriefing != null && routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
            System.out.println("Code: \n" + routeBriefing.returnCodedMessage.get(0).code);
            System.out.println("Message: \n" + routeBriefing.returnCodedMessage.get(0).message);

        }
    }

    @Test
    public void shouldReturnEDT() {
        String timeZone = RouteBriefingRequest.getLocalTimeZoneAbbrev();
        System.out.println("Current abbreviation for either standard or summer time: "
                + timeZone);
    }

    @Test
    public void shouldReturnTimeInUTC() {
        String localTime = "2020-08-15T11:25:00.000";
        System.out.println("Local time: " + localTime);
        String zuluDate  = RouteBriefingRequest.convertLocalTimeToZulu(localTime);
        System.out.println("Zulu Time: " + zuluDate);
    }

}
