# SoaringForecast
Display soaring forecast and weather information of interest to glider pilots.

1. Soaring forecasts displayed from Greater Boston Soaring Club RASP (http://www.soargbsc.com/rasp/  courtesy of Steve Paavola). Currently NewEngland forecasts are always generated. Mifflin forecasts are also being generated but they may be dropped post Mifflin encampment in May.
2. Special Use Airspace (SUA) can be displayed. (The GeoJson SUA files were created based on SUA files in Tim Newport-Peace format  from the Worldwide Turnpoint Exchange and run through a converted at  https://mygeodata.cloud/converter/ to produce the GeoJson files)
3. Turnpoints can be downloaded from Worldwide Turnpoint Exchange (http://soaringweb.org/TP/) or you can import your own (in SeeYou .cup format)
2. Tasks can be defined using the imported turnpoints and overlaid on the RASP forecast.
3. Skew-t soundings can be displayed from selected NE locations (soundings from soarbgsc.com/rasp/ also)
4. METAR and TAF (from aviationweather.gov)
5. NOAA satellite images for visibile, water vapor and infrared color/bw images. Satellite images are animated. (aviationweather.gov)
6. GEOS East current and animated (GIF) images.
7. Customized version of Windy with task overlay. 
8. Options to start browsers to display either Dr. Jacks and/or SkySight

Airport information for TAF/METARS downloaded from http://ourairports.com/data/. Note that on weekends, the file may be empty (being updated?) for a period of time, but the app should check for that and schedule downloads until what appears to be a full file is available.

Currently project involves:
1. Android data binding for MVVM binding.
2. Room 
3. Dagger2 
4. Retrofit/OkHTTP3
5. RxJava/RxAndroid
6. Glide 
7. Webview/Javacript
8. Some Junit4/AndroidJunit4 testing

http://www.jsonschema2pojo.org/ used to convert JSON strings to Java classes. However some of the generated classes were then modified, so if you regen the classes make sure you add back the mods.

Aviation weather xsd downloaded from https://aviationweather.gov/adds/schema/.
Used jaxb xjc command to initially create weather POJO classes then edited to fit to simplexml format. (This was faster than using other code generators I found that were available on the web.)
