# SoaringForecast
Display soaring forecast and weather information of interest to New England glider pilots.

Currently displays:
1. New England soaring forecasts from Greater Boston Soaring Club RASP (http://www.soargbsc.com/rasp/  courtesy of Steve Paavola)
2. SeeYou turnpoints can be imported to the app. Tasks can then be defined and overlaid on the RASP forecast.
3. Skew-t soundings from selected NE locations (sounding from soarbgsc.com/rasp/ also)
4. METAR and TAF (from aviationweather.gov)
5. Satellite images for visibile, water vapor and infrared color/bw images. Satellite images are animated. (aviationweather.gov)
6. Options to start browsers to display either Dr. Jacks and/or SkySight
7. Added customized version of Windy with task overlay.

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
Used jaxb xjc command to initially create weather POJO classes then editted to fit to simplexml format. (This was faster than using other code generators I found that were available on the web.)

Turnpoint filenames from the New England Worldwide Turnpoint Exchange(http://soaringweb.org/TP/) are hard coded in the app. (It looks like the filenames may change as updates are made and files are generated with a new name.)

Worldwide Turnpoint Exchange (http://soaringweb.org/TP/) SUA file for Sterling converted to KML format (needed for Google Map overly) via https://mygeodata.cloud/converter/.

Airport information for TAF/METARS downloaded from http://ourairports.com/data/. Note that on weekends, the file may be empty (being updated?) for a period of time, but the app should check for that and schedule downloads until what appears to be a full file is available.
