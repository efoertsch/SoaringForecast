# SoaringForecast
Display soaring forecast and weather information of interest to New England glider pilots.

Currently displays:
1. New England soaring forecasts from Greater Boston Soaring Club RASP (http://www.soargbsc.com/rasp/  courtesy of Steve Paavola)
   SeeYou turnpoints can be imported to the app. Tasks can then be defined and overlaid on the RASP forecast.
2. METAR and TAF (from aviationweather.gov)
3. Satellite images for visibile, water vapor and infrared color/bw images. Satellite images are animated. (aviationweather.gov)
4. Options to start browsers to display either Dr. Jacks and/or SkySight

Currently project involves:
1. Android data binding for MVVM binding.
2. Dagger2 
3. OkHTTP3
4. RxJava/RxAndroid
5. Some Junit4/AndroidJunit4 testing


Aviation weather xsd downloaded from https://aviationweather.gov/adds/schema/.
Used jaxb xjc command to initially create weather POJO classes then editted to fit to simplexml format. (This was faster than using other code generators I found that were available on the web.)

