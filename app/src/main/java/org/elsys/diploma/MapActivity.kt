package org.elsys.diploma

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_secondary_page) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val userLocation = intent.getParcelableExtra<LatLng?>("userLocation")

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation!!, 13f))


        mMap.setOnMapClickListener { point ->

            (application as MyApplication).apiService.addLocationData(point)

            val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude)).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            )


            mMap.addMarker(marker)

            finish()

        }

    }
}