package org.elsys.diploma

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchMyLocations: Switch

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchLocationsForCleaning: Switch

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchCleanedLocations: Switch

    private val onMarkerClickListener = { it: Marker ->
        val positionMarker = GeoPoint(it.position.latitude, it.position.longitude)

        val intentMarkerActivity =
            Intent(this, MarkerActivity::class.java)
                .putExtra("tempMarkerIntent", markerSave.get(positionMarker))

        overridePendingTransition(0, 0)

        getResult.launch(intentMarkerActivity)
        true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (application as MyApplication).apiService.locationLoadData().observe(this) { updateMap(it) }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        switchMyLocations = findViewById(R.id.switchMyLocations)

        switchLocationsForCleaning = findViewById(R.id.switchLocationsForCleaning)

        switchCleanedLocations = findViewById(R.id.switchCleanedLocations)

        pinCount = findViewById(R.id.numberPins)

        val addButton: Button = findViewById(R.id.location_button)
        addButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
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

                if (document.isCleaned) {

                    mMap?.addMarker(
                        MarkerOptions()
                            .position(LatLng(document.latitude, document.longitude))
                            .icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )
                    )
                } else {
                    val currentUser =
                        (application as MyApplication).apiService.currentUser()!!.email
                    if (document.creator == currentUser) {
                        mMap?.addMarker(
                            MarkerOptions()
                                .position(LatLng(document.latitude, document.longitude))
                                .icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_YELLOW
                                    )
                                )
                        )
                    } else {
                        val marker =
                            MarkerOptions().position(
                                LatLng(
                                    document.latitude,
                                    document.longitude
                                )
                            )
                        mMap?.addMarker(marker)

                    }

                }

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
            if (isChecked) {
                switchLocationsForCleaning.isChecked = false
                switchCleanedLocations.isChecked = false
                mMap?.let {
                    mMap?.clear()
                    for (document in data) {
                        val currentUser =
                            (application as MyApplication).apiService.currentUser()!!.email
                        if (document.creator == currentUser) {
                            if (document.isCleaned) {
                                mMap?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(document.latitude, document.longitude))
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN
                                            )
                                        )
                                )
                            } else {
                                mMap?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(document.latitude, document.longitude))
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_YELLOW
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            } else {
                locationVisualization(data)
            }
        }
    }


    private fun locationsForCleaning(data: List<MarkerData>) {
        switchLocationsForCleaning.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchMyLocations.isChecked = false
                switchCleanedLocations.isChecked = false
                mMap?.let {
                    mMap?.clear()
                    for (document in data) {
                        val currentUser =
                            (application as MyApplication).apiService.currentUser()!!.email
                        if (document.creator != currentUser) {
                            if (!document.isCleaned) {
                                mMap?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(document.latitude, document.longitude))
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_RED
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            } else {
                locationVisualization(data)
            }
        }
    }


    private fun cleanedLocations(data: List<MarkerData>) {
        switchCleanedLocations.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchMyLocations.isChecked = false
                switchLocationsForCleaning.isChecked = false
                mMap?.let {
                    mMap?.clear()
                    for (document in data) {
                        if (document.isCleaned) {
                            mMap?.addMarker(
                                MarkerOptions()
                                    .position(LatLng(document.latitude, document.longitude))
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_GREEN
                                        )
                                    )
                            )
                        }
                    }
                }
            } else {
                locationVisualization(data)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val data = (application as MyApplication).apiService.locationLoadData().value

        if (data != null) {
            updateMap(data)
        }

        mMap?.setOnMarkerClickListener(onMarkerClickListener)

    }


}
