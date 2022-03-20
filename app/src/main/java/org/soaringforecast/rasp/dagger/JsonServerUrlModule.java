package org.soaringforecast.rasp.dagger;

import android.content.Context;
import org.soaringforecast.rasp.R;
import javax.inject.Named;
import dagger.Module;
import dagger.Provides;

@Module
public class JsonServerUrlModule {

    @Provides
    @Named("json_server_url")
    public String getJsonUrl(Context context){
        return context.getString(R.string.json_server_url);
    }

}
