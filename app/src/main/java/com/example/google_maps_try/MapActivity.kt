package com.example.google_maps_try


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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

        val button: Button = findViewById(R.id.maps_close_button)
        button.setOnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val position = LatLng(41.4314, 25.0519)
        mMap.addCircle(CircleOptions().center(position).radius(3500.0).strokeWidth(3f).fillColor(Color.argb(70, 240, 37, 14)))

        mMap.setOnMapClickListener() { point ->

            (application as MyApplication).apiService.addLocationData(point)

            val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude))

            mMap.addMarker(marker)

            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }

    }
}