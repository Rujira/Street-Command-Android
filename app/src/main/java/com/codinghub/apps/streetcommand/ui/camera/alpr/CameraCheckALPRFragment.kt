package com.codinghub.apps.streetcommand.ui.camera.alpr

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRResponse
import com.codinghub.apps.streetcommand.models.error.ApiError
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.error.Status
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import com.codinghub.apps.streetcommand.ui.main.MainActivity
import com.codinghub.apps.streetcommand.viewmodels.CameraViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_check_alpr.contentView
import kotlinx.android.synthetic.main.activity_check_person.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.fragment_camera_check_alpr.view.*
import java.io.ByteArrayOutputStream
import java.util.*

class CameraCheckALPRFragment : Fragment() {

    private lateinit var cameraViewModel: CameraViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var currentAddress : String = ""
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private val TAG = CameraCheckALPRFragment::class.qualifiedName

    companion object {
        private const val REQUEST_LOCATION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_camera_check_alpr, container, false)

        cameraViewModel = ViewModelProvider(this).get(CameraViewModel::class.java)

        view.takeALPRPhotoButton.setSafeOnClickListener {
            onCheckButtonPressed()
        }

        setupLocationClient()
        setupPlacesClient()
        getCurrentLocation()
        getCurrentPlace()

        return view
    }

    //Location
    //Location Function
    private fun setupPlacesClient() {
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION
        )
    }

    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        } else {

            fusedLocationClient.lastLocation.addOnCompleteListener { val location = it.result
                if (location != null) {

                    currentLatitude = location.latitude
                    currentLongitude = location.longitude

                } else {
                    Log.e(TAG, "No location found") }
            }

        }
    }

    private fun getCurrentPlace() {

        val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS)
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        }
        else {
            val placeResponse: Task<FindCurrentPlaceResponse> = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener {
                if (it.isSuccessful) {

                    val response: FindCurrentPlaceResponse = it.result!!

                    currentAddress = response.placeLikelihoods.first().place.address.toString()

                } else {
                    val exception = it.exception
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        Log.e(TAG, "Place not found: " + exception.message + ", " + "status code : " + statusCode)
                    }

                    currentAddress = "ไม่สามารถหาที่อยู่ปัจจุบันได้"

                }
            }
        }
    }


    private fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    private fun convertBitmapToBase64String(bitmap: Bitmap): String {
        val base64String: String
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            base64String = Base64.getEncoder().encodeToString(byteArray)
        } else {
            base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
        }

        return base64String
    }

    private fun onCheckButtonPressed() {

        val identifyDialog: AlertDialog? = SpotsDialog.Builder()
            .setContext(requireContext())
            .setMessage("กำลังตรวจสอบ")
            .setCancelable(false)
            .build()
            .apply {
                show()
            }

        val image = convertBitmapToBase64String(requireParentFragment().previewTextureView.bitmap)
        val latitude = currentLatitude
        val longitude = currentLongitude
        val address = currentAddress

        cameraViewModel.identifyALPR(image, latitude, longitude, address).observe(viewLifecycleOwner, Observer<Either<IdentifyALPRResponse>> { either ->

            if (either?.status == Status.SUCCESS && either.data != null) {
                if (either.data.ret == 0) {
                    if(either.data.alpr.alpr_type == "0" || either.data.alpr.alpr_type == "5") {
                        onNotFoundSuspect(either.data.alpr.plate, either.data.alpr.province)
                    }
                    else {
                        onFoundSuspect(either.data.alpr.plate, either.data.alpr.province, either.data.alpr.alpr_type)
                    }
                } else if (either.data.ret == -3) {
                    Toast.makeText(context, "มีผู้ใช้งานอื่นใช้บัญชีนี้", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity).logout()

                } else {
                    Toast.makeText(context, either.data.msg, Toast.LENGTH_SHORT).show()
                }


            } else {
                if (either?.error == ApiError.IDENTIFYALPR) {
                    Toast.makeText(requireContext().applicationContext, "ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้", Toast.LENGTH_SHORT).show()
                }
            }
            identifyDialog?.dismiss()
        })
    }

    private fun onFoundSuspect(plate: String, province: String, message: String) {
        if (message.toLowerCase(Locale.getDefault()).contains("whitelist")) {
            Snackbar.make(contentView,"พบในฐานข้อมูล - $plate $province $message",  cameraViewModel.getSnackbarsDuration())
                .setAnchorView(requireActivity().nav_view_bottom)
                .setBackgroundTint(ContextCompat.getColor(requireContext().applicationContext, R.color.infoColor))
                .setActionTextColor(ContextCompat.getColor(requireContext().applicationContext, R.color.whiteColor))
                .show()

        } else {
            Snackbar.make(contentView,"พบประวัติ - $plate $province $message",  cameraViewModel.getSnackbarsDuration())
                .setAnchorView(requireActivity().nav_view_bottom)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.dangerColor))
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.whiteColor))
                .show()
        }
    }

    private fun onNotFoundSuspect(plate: String?, province: String?) {

        var message = ""
        message = if (plate.isNullOrEmpty() || province.isNullOrEmpty()) {
            "ไม่พบป้ายทะเบียน"
        } else {
            "$plate $province"
        }
        Snackbar.make(contentView, "ไม่พบประวัติ - $message",  cameraViewModel.getSnackbarsDuration())
            .setAnchorView(requireActivity().nav_view_bottom)
            .setBackgroundTint(ContextCompat.getColor(requireActivity().applicationContext, R.color.successColor))
            .setActionTextColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.whiteColor))
            .show()
    }

}