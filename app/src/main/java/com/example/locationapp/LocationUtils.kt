package com.example.locationapp

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class LocationUtils(
    /*
    *  ----| Context `
    *
    *   In Android development, “Context” is a fundamental concept that represents the current
    *   state of your application and provides access to global information about the application environment.
    *   It’s like a handle to the system; it provides services like resolving resources, obtaining access to databases and preferences, and so on.
    *   In the context of adding location to your app, Context is used to interact with location services and to check permissions.
    *
    * */
    val context: Context
) {

    /*
    *  ----| FusedLocationProviderClient `
    *
    *   Imagine you have a friend who’s really good at finding places and can do it
    *   in different ways—by looking at the stars, reading maps, or asking locals. The
    *   FusedLocationProviderClient is like that friend. It combines various data sources to provide the best
    *   location information in an efficient way. It’s a part of Google Play services and provides a simple API for
    *   getting location information.
    *
    *  ----| getFusedLocationProviderClient(context) `
    *
    *   This is like saying, “Hey, I need to use your location-finding
    *   skills.” By calling this function and giving it the context, you create an instance of
    *   FusedLocationProviderClient that you can use to start asking for location updates.
    *
    * */

    private val _fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)



    /*
    *  ----| @SuppressLint() `
    *
    *   @SuppressLint is an annotation in Android that tells the lint tool
    *   (which checks your code for potential bugs and improvements) to ignore
    *   specific warnings for the annotated part of the code.
    *
    *   "MissingPermission" is the warning that lint would normally give you because
    *   your code is doing something that requires a permission check, and lint can’t
    *   see that check happening.
    *
    * */
    @Suppress("MissingPermission")
    fun requestLocationUpdates(viewModel: LocationViewModel) {
        /*
        *  ----| LocationCallback() `
        *
        *   Now, once you’ve asked your friend (the FusedLocationProviderClient) to find a
        *   location, you need a way for them to tell you once they’ve found it, or if something has changed. That’s what
        *   LocationCallback is for. You provide this callback so that you get notified about location updates or changes.
        *
        * */
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let {
                    val location = LocationData(
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                    viewModel.updateLocation(location)
                }
            }
        }


        /*
        *  ----| LocationRequest.Builder `
        *
        *   Before you send your friend off to find a location, you need to tell them how you
        *   want them to search for it. Do you need a super-accurate location, or is a general idea okay? Do you need
        *   updates every second, or is every few minutes fine? LocationRequest.Builder lets you build a set of
        *   criteria that tells the FusedLocationProviderClient exactly how you want it to act—how often to
        *   check for location, how accurate it should be, etc.
        *
        *  */
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).build()
        _fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun hasLocationPermission(context: Context): Boolean {
        /*
        *  ----| checkSelfPermission() `
        *
        *   We define a function named checkLocationPermission that takes a Context as an input. This Context is like a snapshot of our app’s current situation.
        *   We then check if we have the “Fine Location” permission using a function called checkSelfPermission.
        *   We do the same for the “Coarse Location” permission.
        *   If both checks are true, it means we have both permissions, and we return true.
        *   If either is false, our function will return false, indicating that we do not have permission to access the location.
        *
        * */

        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }


    /*
    *   A Geocoder is a class in Android that performs geocoding and reverse geocoding.
    *   Geocoding is the process of transforming a street address or other description of
    *   a location into a (latitude, longitude) coordinate. Reverse geocoding, on the other hand,
    *   converts geographic coordinates into a human-readable address.
    *
    *   Geocoder uses a backend service that it communicates with to perform its translations. Here’s how you can use it:
    *       1. Geocoding: You have an address or place name, and you want to find its coordinates.
    *       2. Reverse Geocoding: You have the latitude and longitude, and you need to find out the address.
    *
    *   And in our application we are using 2. Reverse Geocoding
    * */
    fun reverseGeocodeLocation(location: LocationData): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val coordinates = LatLng(location.latitude, location.longitude)
        val addresses: MutableList<Address>? =
            geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
        return if (addresses?.isNotEmpty() == true) {
            addresses[0].getAddressLine(0)
        } else {
            "Address not found"
        }
    }
}