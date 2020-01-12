package org.soaringforecast.rasp.test;

import org.junit.Test;
import org.soaringforecast.rasp.dagger.OkHttpClientModule;
import org.soaringforecast.rasp.retrofit.LoggingInterceptor;
import org.soaringforecast.rasp.retrofit.UsgsApi;
import org.soaringforecast.rasp.retrofit.UsgsServerRetrofit;
import org.soaringforecast.rasp.turnpoints.json.NationalMap;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class USGSTests {

    private static final String usgsUrl = "https://nationalmap.gov/";
    UsgsApi client = new UsgsServerRetrofit(new OkHttpClientModule().getOkHttpClient(
            new LoggingInterceptor()), usgsUrl).getRetrofit().create(UsgsApi.class);

    @Test
    public void testUsgsApiCall() throws IOException {
        NationalMap nationalMap = client.getElevation(42.425 + "", -71.791f + "", "Feet").blockingGet();
        assertNotNull(nationalMap.getUSGSElevationPointQueryService().getElevationQuery().getElevation());
        System.out.println("elevation: " + nationalMap.getUSGSElevationPointQueryService().getElevationQuery().getElevation());

    }

}
