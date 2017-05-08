# AviationWeather
Display weather info from aviationweather.gov

Currently displays
1. METAR and TAF 
2. Satellite images for visibile, water vapor and infrared color/bw images. Satellite images are animated.

Currently project involves
1. Android data binding for MVVM binding.
2. Dagger2 
3. OkHTTP3
4. RxJava/RxAndroid
5. Some Junit4/AndroidJunit4 testing


Aviation weather xsd downloaded from https://aviationweather.gov/adds/schema/.
Used jaxb xjc command to initially create weather POJO classes then editted to fit to simplexml format. (This was faster than using other code generators I found that were available on the web.)

