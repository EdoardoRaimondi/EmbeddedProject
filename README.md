# Mappy


Mappy is an Android application based on Google Maps and Google Places APIs. Designed to search interesting places nearby your device, using Google Maps backend (GPS is required for high accuracy). Research radius is adjustable according to your taste. The most awesome place is a click beside you.

  - Find in the list the place you want to search
  - Save your home/hotel/residence in case you forgot it
  - Save your favourite places
  - Get local help phone numbers in case of emergency

### Tech

Mappy uses:

* [Android](https://en.wikipedia.org/wiki/Android_(operating_system)) - as OS on your device
* [Java](https://www.java.com/en/) - the programming language we used in Android Studio
* [Google Places Search](https://developers.google.com/places/web-service/search#PlaceSearchRequests)
* [Google Location Data](https://developers.google.com/maps/documentation/android-sdk/location)
* [Google Places SDK](https://developers.google.com/places/android-sdk/reference/com/google/android/libraries/places/api/Places)


### Test

Mappy APP was tested on:
- Physical devices: LG-G4 (LGE LG-H815 Android 6.0), rockchip tablet (rk312x Android 4.4.4)
- Emulators: Pixel 2 (Android R), Nexus 5 (Android R)


### Support

Since our APP uses Google back-ends, limitations comes in especially on the number of Place search queries.
THE KEY INSTALLED RIGHT NOW IS DISABLED, SO THE APP DOESN'T WORK PROPERLY.If you are interested to invest in our app 
or see how it works, please contact us.
If you want to use your own Google Maps Platform key, put it in the files:
```app/src/debug/res/values/google_maps_api.xml``` 
and 
```app/src/release/res/values/google_maps_api.xml```
