package com.example.locationapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.Manifest
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val viewModel: LocationViewModel = viewModel()

            LocationAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(viewModel)
                }
            }
        }
    }
}


@Composable
fun MyApp(viewModel: LocationViewModel) {

    val context = LocalContext.current
    val locationUtils = LocationUtils(context =  context)
    LocationDisplay(locationUtils, viewModel ,context)
}

@Composable
fun LocationDisplay(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    context: Context
) {

    val location = viewModel.location.value

    val address = location?.let {
        locationUtils.reverseGeocodeLocation(location)
    }


    /*
    *   ----| rememberLauncherForActivityResult() `
    *
    *   rememberLauncherForActivityResult() is a Jetpack Compose specific API used to handle the result of an activity that’s started for a result, such as permission requests.
    *   When you request permissions, Android will show the user a dialog, and the user will either grant or deny the permissions. The system then returns the result to your app, and you need to handle this in your code.
    *   This method provides a way to launch an activity and receive the result back in a composable function without managing the lifecycle or the request code manually.
    *
    *   contract:
    *       This parameter takes an ActivityResultContract, which defines the type of input you need to provide and the type of
    *       output you’ll expect. For permissions, you would use ActivityResultContracts.RequestMultiplePermissions().
    *
    *   onResult:
    *       This lambda is where you handle what happens after the activity finishes and the result is available. For permissions,
    *       it provides a map with the permission names as keys and Boolean values indicating whether each permission was granted.
    *
    * */

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                locationUtils.requestLocationUpdates(viewModel)
            } else {
                // ask for permission
                val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    // we are setting the context of MainActivity
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                        ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (rationalRequired) {
                    Toast.makeText(context, "Location Permission is required for this feature to work", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Location Permission is required. Please enable it in the Android Settings", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

         if (location != null) {
            Text("Address: ${location.latitude} ${location.longitude} \n $address")
         } else {
             Text("Location not available")
         }
        Button(
            onClick = {
                if (locationUtils.hasLocationPermission(context)) {
                    locationUtils.requestLocationUpdates(viewModel)
                    // Permission already granted, update the location
                } else {
                    // Request location permission
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        ) {
            Text("Get Location")
        }
    }
}
