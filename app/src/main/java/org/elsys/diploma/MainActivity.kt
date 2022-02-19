package org.elsys.diploma

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var positionSave: Location

    private var brMarkers: Int = 0

    private val markerSave: MutableMap<GeoPoint, MarkerData> = HashMap()

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}


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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        positionSave = Location("")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val addButton: Button = findViewById(R.id.location_button)


        addButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java).putExtra("docCleaned", 0)
            startActivity(intent)
        }

        val delButton: Button = findViewById(R.id.delete_button)
        delButton.setOnClickListener {
            mMap!!.setOnMarkerClickListener { marker ->


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


                mMap!!.setOnMarkerClickListener(onMarkerClickListener)
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


    private fun updateMap(data: List<MarkerData>) {
        if (mMap != null) {
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
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                                )
                        )
                    } else {
                        val marker =
                            MarkerOptions().position(LatLng(document.latitude, document.longitude))
                        mMap!!.addMarker(marker)
                    }


                }

                markerSave.put(GeoPoint(document.latitude, document.longitude), document)
            }

            brMarkers = data.size
            pinCount.text = brMarkers.toString()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        pinCount = findViewById(R.id.numberPins)

        val data = (application as MyApplication).apiService.locationLoadData().value

        if (data != null) {
            updateMap(data)
        }

        mMap!!.setOnMarkerClickListener(onMarkerClickListener)

    }


}
