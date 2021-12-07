package com.example.google_maps_try

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val map : MapActivity = MapActivity();
    val fm = supportFragmentManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

       val button: Button = findViewById(R.id.location_button)
        map.prefs = this.getSharedPreferences("LatLng", MODE_PRIVATE)

       button.setOnClickListener(){
           val intent = Intent(this, MapActivity::class.java)
           finish();
           overridePendingTransition(0, 0);
           startActivity(intent);
           println("In map activity!!!")
       }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //val locations = FirebaseDatabase.getInstance().getReference().child("location");
        val database = Firebase.database("https://maps-66477-default-rtdb.firebaseio.com/")
        //var reference = database.reference
        val locations : DatabaseReference = database.reference
        //reference = FirebaseDatabase.getInstance().getReference("location")//.child("location")
        println("!!!DATA : $locations")

        locations.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                println("Listener : " + error.message)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //val children = snapshot.getValue().toString()
                val children = snapshot.children
                println("DATA CHANGE!!! : $children")
                var command : String = ""
                children.forEach {
                    println("Listener 2 : " + it.toString())
                    command = it.getValue().toString()
                    println("LATITUDE : " + command)
                }

                var text = command.replace("\\D".toRegex(), "")



                //text = text.trim { it <= ' ' }


                //text = text.replace(" +".toRegex(), " ")


                println("FINAL TEXT : " + text)

            }
        })

        mMap.setOnMapClickListener() { point ->
            println("IM HEREEEEE")
            mMap.addMarker(MarkerOptions().position(point))
            val database = Firebase.database("https://maps-66477-default-rtdb.firebaseio.com/")
            val reference = database.reference
            val data = reference.push().child("location").setValue(point)

        }
        /*!!!!!!!!!!!!!!!!!!!!NE TRIJ!@
        var temp_prefs: SharedPreferences? = map.prefs
        if (temp_prefs != null) {
            if((temp_prefs.contains("Lat")) && (temp_prefs.contains("Lng"))){
                val lat = temp_prefs.getString("Lat", "")
                val lng = temp_prefs.getString("Lng", "")
                val l = LatLng(lat!!.toDouble(), lng!!.toDouble())
                mMap.addMarker(MarkerOptions().position(l))
                val database = Firebase.database("https://maps-66477-default-rtdb.firebaseio.com/")
                val reference = database.reference
                val data = reference.push().child("location").setValue(l)
            }
        }*/

        val starcevo = LatLng(41.4314, 25.0519)

        mMap.addMarker(MarkerOptions().position(starcevo).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(starcevo))
    }
}



