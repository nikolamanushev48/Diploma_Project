package com.example.google_maps_try
//Bazi danni
//kak da smenqm aktivitita bez da rastartvam aktivitito(kak da se zapazqt markerite?)

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var m: Marker? = null
    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
           .findFragmentById(R.id.map_secondary_page) as SupportMapFragment
        mapFragment.getMapAsync(this)

        prefs = this.getSharedPreferences("LatLng", MODE_PRIVATE);
    }

    //val list_latitude: MutableList<Double> = ArrayList()
    //val list_longitude: MutableList<Double> = ArrayList()

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val temp_prefs: SharedPreferences? = prefs
/*!!!!!!!!!!!!!!!!!!!!NE TRIJ!@
        if (temp_prefs != null) {
            if((temp_prefs.contains("Lat")) && (temp_prefs.contains("Lng"))) {
                val lat = temp_prefs.getString("Lat", "")
                val lng = temp_prefs.getString("Lng", "")
                val l = LatLng(lat!!.toDouble(), lng!!.toDouble())
                mMap.addMarker(MarkerOptions().position(l))
                //val database = Firebase.database("https://maps-66477-default-rtdb.firebaseio.com/")
                //val reference = database.reference
                //val data = reference.push().child("location").setValue(l)
            }
        }*/
        val list: MutableList<LatLng> = ArrayList()
        //var list: MutableMap<Int,Int> = mutableMapOf()

        mMap.setOnMapClickListener() { point ->
            /*!!!!!NE TRIJ!!!!!!!!!!!!!!!!!!!!!!!!!!
            prefs!!.edit().putString("Lat", java.lang.String.valueOf(point.latitude)).apply()
            prefs!!.edit().putString("Lng", java.lang.String.valueOf(point.longitude)).apply()
            */

            var br = 1;
            var br2 = 2;

            val database = Firebase.database("https://maps-66477-default-rtdb.firebaseio.com/")
            val reference = database.reference
            val data = reference.push().child("location").setValue(point)


            val marker = MarkerOptions()
                .position(LatLng(point.latitude, point.longitude))
                .title("Destination $br")


            googleMap.addMarker(marker)

            println(point.latitude.toString() + "---" + point.longitude)

           //prefs!!.edit().putString("Lng", java.lang.String.valueOf(point.longitude)).commit()

            br++
            //list_latitude.add(point.latitude)
            //list_longitude.add(point.longitude)
            if(br == br2){
                br2++

                val intent = Intent(this, MainActivity::class.java)
                finish();
                startActivity(intent)
            }
        }




        val starcevo = LatLng(41.4314324, 25.0519345)

        mMap.addMarker(MarkerOptions().position(starcevo).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(starcevo))

    }

//--------------->>>>>.--------->>>>>>>>>>>>.----------------------->>>>>>>>>>---------------------->>>>>>>>>>>--------------------------------->>>>>>>

}


