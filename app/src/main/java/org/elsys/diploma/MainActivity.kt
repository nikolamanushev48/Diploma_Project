package org.elsys.diploma

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    private lateinit var pinCount: TextView

    private var brMarkers: Int = 0

    private val markerSave: MutableMap<GeoPoint, MarkerData> = HashMap()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var userLocation: LatLng = LatLng(0.0, 0.0)

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private lateinit var switchMyLocations: SwitchCompat

    private lateinit var switchLocationsForCleaning: SwitchCompat

    private lateinit var switchCleanedLocations: SwitchCompat

    private val onMarkerClickListener = { it: Marker ->
        val positionMarker = GeoPoint(it.position.latitude, it.position.longitude)

        val intentMarkerActivity =
            Intent(this, MarkerActivity::class.java)
                .putExtra("tempMarkerIntent", markerSave.get(positionMarker))


        getResult.launch(intentMarkerActivity)
        true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (application as MyApplication).apiService.locationLoadData()
            .observe(this) { updateMap(it) }//updating map when there are changes in the data

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        switchMyLocations = findViewById(R.id.switchMyLocations)

        switchLocationsForCleaning = findViewById(R.id.switchLocationsForCleaning)

        switchCleanedLocations = findViewById(R.id.switchCleanedLocations)

        pinCount = findViewById(R.id.numberPins)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        val addButton: Button = findViewById(R.id.location_button)
        addButton.setOnClickListener {
            val intent =
                Intent(this, MapActivity::class.java).putExtra("userLocation", userLocation)
            startActivity(intent)
        }

        val delButton: Button = findViewById(R.id.delete_button)
        delButton.setOnClickListener {
            mMap?.setOnMarkerClickListener { marker ->


                (application as MyApplication).apiService.deleteLocation(marker) { succeed ->
                    if (succeed) {
                        marker.remove()
                        brMarkers--
                        pinCount.text = brMarkers.toString()
                    } else {
                        Toast.makeText(
                            this,
                            "Deleting this location is not permitted!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                switchMyLocations.isChecked = false

                mMap?.setOnMarkerClickListener(onMarkerClickListener)
                true
            }
        }

        val logout: Button = findViewById(R.id.logout)
        logout.setOnClickListener {

            (application as MyApplication).apiService.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

        }

    }

    private fun locationVisualization(data: List<MarkerData>) {
        mMap?.let {
            mMap?.clear()
            for (document in data) {
                val marker = MarkerOptions().position(LatLng(document.latitude, document.longitude))

                if (document.isCleaned) {
                    marker.icon(
                        BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        )
                    )
                } else {
                    val currentUser =
                        (application as MyApplication).apiService.currentUser()!!.email
                    if (document.creator == currentUser) {
                        marker.icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_YELLOW
                            )
                        )
                    } else {
                        marker.icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED
                            )
                        )

                    }

                }
                mMap?.addMarker(marker)
            }
        }

    }

    private fun updateMap(data: List<MarkerData>) {

        locationVisualization(data)

        for (document in data) {
            markerSave.put(GeoPoint(document.latitude, document.longitude), document)
        }
        brMarkers = data.size
        pinCount.text = brMarkers.toString()


        myLocations(data)
        locationsForCleaning(data)
        cleanedLocations(data)
    }


    private fun myLocations(data: List<MarkerData>) {
        switchMyLocations.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                locationVisualization(data)
            } else {
                switchLocationsForCleaning.isChecked = false
                switchCleanedLocations.isChecked = false
                mMap?.let {
                    mMap?.clear()
                    for (document in data) {
                        val currentUser =
                            (application as MyApplication).apiService.currentUser()!!.email
                        val marker =
                            MarkerOptions().position(LatLng(document.latitude, document.longitude))

                        if (document.creator == currentUser) {
                            if (document.isCleaned) {
                                marker.icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_GREEN
                                    )
                                )
                                mMap?.addMarker(marker)
                            } else {
                                marker.icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_YELLOW
                                    )
                                )
                                mMap?.addMarker(marker)
                            }
                        }

                    }
                }
            }
        }
    }


    private fun locationsForCleaning(data: List<MarkerData>) {
        switchLocationsForCleaning.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                locationVisualization(data)
            } else {
                switchMyLocations.isChecked = false
                switchCleanedLocations.isChecked = false
                mMap?.let {
                    mMap?.clear()
                    for (document in data) {
                        val currentUser =
                            (application as MyApplication).apiService.currentUser()!!.email
                        val marker =
                            MarkerOptions().position(LatLng(document.latitude, document.longitude))

                        if (document.creator != currentUser) {
                            if (!document.isCleaned) {
                                marker.icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_RED
                                    )
                                )
                                mMap?.addMarker(marker)
                            }
                        }

                    }
                }
            }

        }
    }


    private fun cleanedLocations(data: List<MarkerData>) {
        switchCleanedLocations.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                locationVisualization(data)
            } else {

                switchMyLocations.isChecked = false
                switchLocationsForCleaning.isChecked = false
                mMap?.let {
                    mMap?.clear()
                    for (document in data) {
                        val marker =
                            MarkerOptions().position(LatLng(document.latitude, document.longitude))
                        if (document.isCleaned) {
                            marker.icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_GREEN
                                )
                            )
                            mMap?.addMarker(marker)
                        }
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        checkLocationPermissions()

        val data =
            (application as MyApplication).apiService.locationLoadData().value//the value of livedata

        if (data != null) {
            updateMap(data)
        }

        mMap?.setOnMarkerClickListener(onMarkerClickListener)//setting onMarkerClickListener because of the delete button

        mMap?.setOnCameraIdleListener {
            val northeast = mMap?.projection?.visibleRegion?.latLngBounds?.northeast
            val southwest = mMap?.projection?.visibleRegion?.latLngBounds?.southwest

            (application as MyApplication).apiService.queryLocations(northeast!!, southwest!!)
        }

    }

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                mMap?.isMyLocationEnabled = true
                updateUserLocation()
            }
        }

    private fun checkLocationPermissions() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap?.isMyLocationEnabled = true
            updateUserLocation()
        } else if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }


    @SuppressLint("MissingPermission")
    private fun updateUserLocation() {

        val task = fusedLocationProviderClient.lastLocation

        task.addOnCompleteListener {
            val location = it.result
            if (location != null) {
                with(mMap) {
                    userLocation = LatLng(location.latitude, location.longitude)
                    this!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13f))
                }

            }

        }
    }
}
