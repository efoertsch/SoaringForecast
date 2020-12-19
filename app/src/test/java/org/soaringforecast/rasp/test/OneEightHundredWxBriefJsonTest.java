package org.soaringforecast.rasp.test;

import org.junit.Before;
import org.junit.Test;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.ReturnCodedMessage;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefing;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefingRequest;
import org.soaringforecast.rasp.retrofit.One800WxBriefApi;
import org.soaringforecast.rasp.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OneEightHundredWxBriefJsonTest {

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
        Call<ResponseBody> call = client.getMETAR(get1800WXBriefAPIAuthorization(), "KORH");
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
        routeBriefingRequest.setSelectedBriefingType("EMAIL");
        routeBriefingRequest.setBriefingResultFormat("PDF");
        routeBriefingRequest.setEmailAddress("flightservice@soaringforecast.org");
        // Call<RouteBriefing>  call = client.getRouteBriefing(routeBriefingRequest.getRestParmString());
        //RouteBriefing routeBriefing = call.execute().body();
        System.out.println("Parms: \n" + routeBriefingRequest.getRestParmString());
        RequestBody body = RequestBody.create(MediaType.parse("\"text/plain\""), routeBriefingRequest.getRestParmString());
        Call<RouteBriefing> call = client.getRouteBriefing(get1800WXBriefAPIAuthorization(), body);
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
                .addInterceptor(new JunitLoggingInterceptor())
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(endpointUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }




    @Test
    public void shouldCreateSimpleBriefForNotABriefingWithProductAndTailoringOptions() throws Exception {
        // Not a NGBV2
        String parmString =
                "notABriefing=true&includeCodedMessages=true&type=ICAO&routeCorridorWidth=25&briefingPreferences={\"items\":[\"DD_NTM\",\"TFR\",\"FA\"],\"tailoring\":[]}&outlookBriefing=false&flightRules=VFR&departure=3B3&departureInstant=2020-08-15T18:00:00.000&destination=3B3&route=3B3 AFN EEN 3B3 &flightDuration=PT05H&speedKnots=50&versionRequested=99999999&briefingType=NGB&plainTextTimeZone=EST";

        RequestBody body = RequestBody.create(MediaType.parse("\"text/plain\""), parmString);
        Call<RouteBriefing> call = client.getRouteBriefing(get1800WXBriefAPIAuthorization(), body);
        RouteBriefing routeBriefing = call.execute().body();
        assertNotNull(routeBriefing);
        if (routeBriefing != null && routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
            System.out.println("Code: \n" + routeBriefing.returnCodedMessage.get(0).code);
            System.out.println("Message: \n" + routeBriefing.returnCodedMessage.get(0).message);

        }
    }

    @Test
    public void shouldBeSuccessfulRouteBriefingRequest() throws Exception {
        String parmString =
                // Not a NGBV2
                // "includeCodedMessages=true&type=ICAO&aircraftIdentifier=N68RM&routeCorridorWidth=25&briefingPreferences={\"items\":[\"DD_NTM\",\"SEV_WX\",\"METAR\"],\"tailoring\":[\"EXCLUDE_GRAPHICS\",\"EXCLUDE_HISTORICAL_METARS\"]}&outlookBriefing=false&flightRules=VFR&departure=3B3&departureInstant=2020-08-18T19:36:06&destination=3B3&route=KAFN,KEEN&flightDuration=PT04H&webUserName=flightservice@soaringforecast.org&speedKnots=50&versionRequested=99999999&notABriefing=false&briefingType=NGBV2&briefingResultFormat=PDF&emailAddress=flightservice@soaringforecast.org";

                // Not an official brief - but
                //Code: NotABriefingEmail.invalid    Message: Email briefing is not allowed if notABriefing is true
                //Code: EmailBriefing.UnableToSendEmail    Message: Unable to send email.
                //"notABriefing=true&includeCodedMessages=true&type=ICAO&routeCorridorWidth=25&briefingPreferences={\"items\":[\"TFR\",\"DD_NTM\",\"DEP_NTM\",\"DEST_NTM\",\"ENROUTE_NTM_RWY_TWY_APRON_AD_FDC\"],\"tailoring\":[]}&outlookBriefing=false&flightRules=VFR&departure=3B3&departureInstant=2020-12-07T15:00:00.000&destination=3B3&route=3B3 CNH 3B0 3B3 &flightDuration=PT05H&speedKnots=50&versionRequested=99999999&briefingType=EMAIL&emailAddress=efoertsch@verizon.net&plainTextTimeZone=EST";

                // An official brief requires aircraftId
                "notABriefing=false&includeCodedMessages=true&type=ICAO&aircraftIdentifier=N68RM&routeCorridorWidth=25&briefingPreferences={\"items\":[\"TFR\",\"DD_NTM\",\"DEP_NTM\",\"DEST_NTM\",\"ENROUTE_NTM_RWY_TWY_APRON_AD_FDC\"],\"tailoring\":[]}&outlookBriefing=false&flightRules=VFR&departure=3B3&departureInstant=2020-12-08T12:00:00.000&destination=3B3&route=3B3 CNH 3B0 3B3 &flightDuration=PT05H&webUserName=flightservice@soaringforecast.org&speedKnots=50&versionRequested=99999999&briefingType=EMAIL&emailAddress=flightservice@soaringforecast.org&altitudeFL=060&plainTextTimeZone=EST";

        // NGBV2
        //Code: Webservice.SystemError
        //Message: We are sorry, but the system was unable to process your last request.
        //"notABriefing=true&includeCodedMessages=true&type=ICAO&routeCorridorWidth=25&briefingPreferences={\"items\":[\"TFR\",\"DD_NTM\",\"DEP_NTM\",\"DEST_NTM\",\"ENROUTE_NTM_RWY_TWY_APRON_AD_FDC\"],\"tailoring\":[]}&outlookBriefing=false&flightRules=VFR&departure=3B3&departureInstant=2020-12-07T15:00:00.000&destination=3B3&route=3B3 CNH 3B0 3B3 &flightDuration=PT05H&speedKnots=50&versionRequested=99999999&briefingType=NGBV2&plainTextTimeZone=EST";

        RequestBody body = RequestBody.create(MediaType.parse("\"text/plain\""), parmString);
        Call<RouteBriefing> call = client.getRouteBriefing(get1800WXBriefAPIAuthorization(), body);
        RouteBriefing routeBriefing = call.execute().body();
        assertNotNull(routeBriefing);
        if (routeBriefing != null && routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
            for (ReturnCodedMessage returnCodedMessage : routeBriefing.returnCodedMessage) {
                System.out.println("Code: " + returnCodedMessage.code + "    " + "Message: " + returnCodedMessage.message + "\n");
            }
        }
    }


    @Test
    public void shouldBeSuccessful_NotABrief_Simple_RouteBriefingRequest() throws Exception {
        String briefingType="SIMPLE";
        String parmString =
                "notABriefing=true"
                        + "&briefingType=" + briefingType
                        + "&outlookBriefing=true"     // true or false
                        + "&includeCodedMessages=true&type=ICAO&routeCorridorWidth=25"
                        + "&flightRules=VFR&departure=3B3&departureInstant=2020-12-17T15:00:00.000"
                        + "&destination=3B3&route=3B3 CNH 3B0 3B3 &flightDuration=PT05H&speedKnots=50"
                        + "&versionRequested=99999999"
                        + "&briefingPreferences={\"plainText\":true,\"tailoring\":[\"PLAINTEXT_ONLY\",\"ENCODED_ONLY\",\"NO_GEOMETRY\" ]}"
                        + "&emailAddress=flightservice@soaringforecast.org&altitudeFL=060&plainTextTimeZone=EST"
                //+ "&briefingResultFormat=PDF"  << valid only if NGBV2
                ;

        RequestBody body = RequestBody.create(MediaType.parse("\"text/plain\""), parmString);
        Call<RouteBriefing> call = client.getRouteBriefing(get1800WXBriefAPIAuthorization(), body);
        RouteBriefing routeBriefing = call.execute().body();
        assertNotNull(routeBriefing);
        if (routeBriefing != null && routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
            for (ReturnCodedMessage returnCodedMessage : routeBriefing.returnCodedMessage) {
                System.out.println("Code: " + returnCodedMessage.code + "    " + "Message: "
                        + returnCodedMessage.message + "\n");
            }
        }
        if (briefingType.equals("SIMPLE") && routeBriefing.simpleWeatherBriefing != null) {
            System.out.println(routeBriefing.simpleWeatherBriefing.replace("_NL_" , "\n"));
        }
    }

    @Test
    public void shouldBeSuccessful_PDF_Standard_RouteBriefingRequest() throws Exception {
        String briefingType="NGBV2";
        String parmString =
                // "notABriefing=true" +
                "&briefingType=" + briefingType
                        + "&briefingResultFormat=PDF"  //<< valid only if NGBV2
                        + "&outlookBriefing=false"     // false with NGBV2 should return standard brief
                        + "&includeCodedMessages=true&type=ICAO"
                        + "&routeCorridorWidth=25"
                        + "&flightRules=VFR"
                        + "&departure=3B3"
                        + "&departureInstant=2020-12-17T19:00:00.000"
                        + "&destination=3B3&route=3B3 CNH 3B0 3B3 &flightDuration=PT05H&speedKnots=50"
                        + "&type=ICAO"
                        + "&webUserName=flightservice@soaringforecast.org&speedKnots=50&versionRequested=99999999"
                        + "&aircraftIdentifier=N68RM&routeCorridorWidth=25"
                        + "&versionRequested=99999999"
                        + "&briefingPreferences={\"items\":[\"TFR\",\"DD_NTM\",\"DEP_NTM\",\"DEST_NTM\",\"ENROUTE_NTM_RWY_TWY_APRON_AD_FDC\"],\"tailoring\":[]}"
                        + "&emailAddress=flightservice@soaringforecast.org&altitudeFL=060&plainTextTimeZone=EST"
                //
                ;

        RequestBody body = RequestBody.create(MediaType.parse("\"text/plain\""), parmString);
        Call<RouteBriefing> call = client.getRouteBriefing(get1800WXBriefAPIAuthorization(), body);
        RouteBriefing routeBriefing = call.execute().body();
        assertNotNull(routeBriefing);
        if (routeBriefing != null && routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
            for (ReturnCodedMessage returnCodedMessage : routeBriefing.returnCodedMessage) {
                System.out.println("Code: " + returnCodedMessage.code + "    " + "Message: "
                        + returnCodedMessage.message + "\n");
            }
        }
        if (briefingType.equals("SIMPLE") && routeBriefing.simpleWeatherBriefing != null) {
            System.out.println(routeBriefing.simpleWeatherBriefing.replace("_NL_" , "\n"));
        }
    }

    @Test
    public void shouldReturnEDT() {
        String timeZone = TimeUtils.getLocalTimeZoneAbbrev();
        System.out.println("Current abbreviation for either standard or summer time: "
                + timeZone);
    }

    @Test
    public void shouldReturnTimeInUTC() {
        String localTime = "2020-08-15T11:25:00.000";
        System.out.println("Local time: " + localTime);
        String zuluDate = TimeUtils.convertLocalTimeToZulu(localTime);
        System.out.println("Zulu Time: " + zuluDate);
    }

    private String get1800WXBriefAPIAuthorization() {
        // Following should give   ZmxpZ2h0c2VydmljZUBzb2FyaW5nZm9yZWNhc3Qub3JnOlNvYXJpbmdGb3JlY2FzdDIwMjAhISE=
        String encoded = Base64.getEncoder().encodeToString(("flightservice@soaringforecast.org" + ":" + "SoaringForecast2020!!!").getBytes());
        System.out.println("Encoded :" + encoded);
        return "Basic " + encoded;
    }

}
