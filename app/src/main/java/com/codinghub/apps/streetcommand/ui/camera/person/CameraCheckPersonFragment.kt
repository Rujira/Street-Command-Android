package com.codinghub.apps.streetcommand.ui.camera.person

import android.Manifest
import android.app.AlertDialog
import com.codinghub.apps.streetcommand.models.person.Person
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.codinghub.apps.streetcommand.models.person.IdentifyPersonResponse
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import com.codinghub.apps.streetcommand.ui.camera.alpr.CameraCheckALPRFragment
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
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_check_alpr.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.fragment_camera_check_person.view.*
import java.io.ByteArrayOutputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class CameraCheckPersonFragment : Fragment() {

    private lateinit var cameraViewModel: CameraViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var currentAddress : String = ""
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private val TAG = CameraCheckPersonFragment::class.qualifiedName

    companion object {
        private const val REQUEST_LOCATION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_camera_check_person, container, false)

        cameraViewModel = ViewModelProvider(this).get(CameraViewModel::class.java)

        view.takeFacePhotoButton.setSafeOnClickListener {
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

        cameraViewModel.identifyPerson(image, latitude, longitude, address).observe(viewLifecycleOwner, Observer<Either<IdentifyPersonResponse>> { either ->

            if (either?.status == Status.SUCCESS && either.data != null) {
                if (either.data.ret == 0) {

                    val personType = either.data.person.person_type.toLowerCase(Locale.getDefault())

                    when {
                        personType.contains("whitelist") && personType.contains("civilian") -> {
                            onNotFoundSuspect(either.data.person)
                        }
                        personType.contains("whitelist") -> {
                            onFoundWhitelist(either.data.person)
                        }
                        personType.contains("blacklist") -> {
                            onFoundBlacklist(either.data.person)
                        }
                        else -> {
                            onFaceNotFound()
                        }
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

    private fun onNotFoundSuspect(person: Person) {
        Snackbar.make(contentView, "ไม่พบประวัติ - ${person.person_type}", cameraViewModel.getSnackbarsDuration())
            .setAnchorView(requireActivity().nav_view_bottom)
            .setBackgroundTint(ContextCompat.getColor(requireActivity().applicationContext, R.color.successColor))
            .setActionTextColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.whiteColor))
            .show()
    }

    private fun onFaceNotFound() {
        Snackbar.make(contentView, "ไม่พบใบหน้า",  cameraViewModel.getSnackbarsDuration())
            .setAnchorView(requireActivity().nav_view_bottom)
            .setBackgroundTint(ContextCompat.getColor(requireActivity().applicationContext, R.color.warningColor))
            .setActionTextColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.whiteColor))
            .show()
    }

    private fun onFoundWhitelist(person: Person) {
        Snackbar.make(contentView,"พบในฐานข้อมูล - ${person.person_type}",  cameraViewModel.getSnackbarsDuration())
            .setAction("เพิ่มเติม") {
                showMoreDialog(person)
            }
            .setAnchorView(requireActivity().nav_view_bottom)
            .setBackgroundTint(ContextCompat.getColor(requireContext().applicationContext, R.color.infoColor))
            .setActionTextColor(ContextCompat.getColor(requireContext().applicationContext, R.color.whiteColor))
            .show()
    }

    private fun onFoundBlacklist(person: Person) {
        Snackbar.make(contentView, "พบประวัติ - ${person.person_type}",  cameraViewModel.getSnackbarsDuration())
            .setAction("เพิ่มเติม") {
                showMoreDialog(person)
            }
            .setAnchorView(requireActivity().nav_view_bottom)
            .setBackgroundTint(ContextCompat.getColor(requireActivity().applicationContext, R.color.dangerColor))
            .setActionTextColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.whiteColor))
            .show()
    }

    private fun showMoreDialog(person: Person) {

        val dialogBuilder = AlertDialog.Builder(activity)
        val dialogView = this.layoutInflater.inflate(R.layout.dialog_person, null)
        dialogBuilder.setView(dialogView)

        dialogBuilder.setTitle(person.person_type)

        val percentEditText = dialogView.findViewById<TextView>(R.id.percentEditText)
        val personNameEditText = dialogView.findViewById<TextView>(R.id.personNameEditText)
        val personIDEditText = dialogView.findViewById<TextView>(R.id.personIDEditText)
        val personImageView1 = dialogView.findViewById<ImageView>(R.id.personImageView1)
        val personImageView2 = dialogView.findViewById<ImageView>(R.id.personImageView2)

        val df = DecimalFormat("##.##")
        df.roundingMode = RoundingMode.CEILING

        percentEditText.text =  Editable.Factory.getInstance().newEditable(getString(R.string.similarity_string, df.format(person.similarity)))
        personNameEditText.text =  Editable.Factory.getInstance().newEditable(getString(R.string.person_fullname_string, person.fullname))
        personIDEditText.text = Editable.Factory.getInstance().newEditable(getString(R.string.cid_string, person.citizen_id))

        Picasso.get().load(person.image_search).into(personImageView1)
        Picasso.get().load(person.image_match).into(personImageView2)

        dialogBuilder.setNegativeButton("ปิด") { _, _->
            //pass
        }

        val dialog = dialogBuilder.create()
        dialog.show()

    }

}