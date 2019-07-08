package org.soaringforecast.rasp.retrofit;

import org.soaringforecast.rasp.task.json.TurnpointRegions;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface GBSCJsonApi {

    // Get list of turnpoint files
    @GET("/soaringforecast/turnpoint_download_list")
    Single<TurnpointRegions> getTurnpointRegions();



}
