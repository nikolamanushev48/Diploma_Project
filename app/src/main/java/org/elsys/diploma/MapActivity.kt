package org.elsys.diploma

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.google_maps_try.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_secondary_page) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val position = LatLng(41.4314, 25.0519)
        mMap.addCircle(CircleOptions().center(position).radius(3500.0).strokeWidth(3f).fillColor(Color.argb(70, 240, 37, 14)))

        mMap.setOnMapClickListener() { point ->

            (application as MyApplication).apiService.addLocationData(point)

            val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude))

            mMap.addMarker(marker)


            finish()

        }

    }
}