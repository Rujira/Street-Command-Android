package com.codinghub.apps.streetcommand.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.ui.home.HomeFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.fragment_location.*

class LocationsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    private var currentPlaceAddress: String = ""

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private val TAG = LocationsFragment::class.qualifiedName

    companion object {

        private const val REQUEST_LOCATION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_location, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupLocationClient()
        setupPlacesClient()


        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        view?.let {

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        getCurrentLocation()

        mMap.setOnMyLocationClickListener {
            Log.d(TAG, "Clicked")
            displayCurrentPlace()
        }
        mMap.setOnMyLocationButtonClickListener {
            Log.d(TAG, "Clicked2")
            getCurrentLocation()
            displayCurrentPlace()
            true
        }
    }

    private fun setupPlacesClient() {
        Places.initialize(requireContext(), "Google-API-Key")
        placesClient = Places.createClient(requireContext())
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        } else {

            mMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnCompleteListener { val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)

                    currentLatitude = location.latitude
                    currentLongitude = location.longitude

                    //mMap.animateCamera(update)
                    mMap.moveCamera(update)
                    displayCurrentPlace()

                } else {
                    Log.e(TAG, "No location found") }
            }

        }
    }

    private fun displayCurrentPlace() {

        val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS)
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        }
        else {
            placeTextView.text = "กำลังค้นหาที่อยู่"
            val placeResponse: Task<FindCurrentPlaceResponse> = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener {
                if (it.isSuccessful) {

                    val response: FindCurrentPlaceResponse = it.result!!
                    if (placeTextView != null) {
                        placeTextView.text = response.placeLikelihoods.first().place.address.toString()
                        currentPlaceAddress = response.placeLikelihoods.first().place.address.toString()
                    }


                } else {
                    val exception = it.exception
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        Log.e(TAG, "Place not found: " + exception.message + ", " + "status code : " + statusCode)
                    }
                    placeTextView.text = "${exception?.message}"
                    currentPlaceAddress = "ไม่สามารถหาที่อยู่ปัจจุบันได้"

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "LOCATION PERMISSION DENIED")
            }
        }
    }

}