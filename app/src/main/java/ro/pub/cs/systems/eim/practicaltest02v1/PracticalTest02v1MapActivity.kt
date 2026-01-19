package ro.pub.cs.systems.eim.practicaltest02v1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

//harta de la sub 4
class PracticalTest02v1MapActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test02v1_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Coordonate Ghelmegioaia, România
        val ghelmegioaia = LatLng(44.6147, 22.8347)

        googleMap.addMarker(
            MarkerOptions().position(ghelmegioaia).title("Ghelmegioaia")
        )
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(ghelmegioaia, 15f)
        )
    }
}
