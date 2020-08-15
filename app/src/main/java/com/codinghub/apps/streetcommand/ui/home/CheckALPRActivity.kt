package com.codinghub.apps.streetcommand.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.models.alpr.CheckALPRResponse
import com.codinghub.apps.streetcommand.models.alpr.ProvinceList
import com.codinghub.apps.streetcommand.models.error.ApiError
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.error.Status
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import com.codinghub.apps.streetcommand.ui.main.MainActivity
import com.codinghub.apps.streetcommand.viewmodels.CheckALPRViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonParseException
import dmax.dialog.SpotsDialog
import gnu.kawa.android.defs.activity
import kotlinx.android.synthetic.main.activity_check_alpr.*
import kotlinx.android.synthetic.main.activity_check_person.*
import kotlinx.android.synthetic.main.activity_check_person.contentView
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*

class CheckALPRActivity : AppCompatActivity() {

    private lateinit var checkALPRViewModel: CheckALPRViewModel

    private val TAG = CheckALPRActivity::class.qualifiedName

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var currentAddress : String = ""
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private var isTakePhoto : Boolean = false
    var image_uri: Uri? = null
    var exifData: Uri? = null
    internal lateinit var snapImage: Bitmap


    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private val IMAGE_GALLERY_CODE = 1002

    companion object {
        private const val REQUEST_LOCATION = 1
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_alpr)

        checkALPRViewModel = ViewModelProvider(this).get(CheckALPRViewModel::class.java)

        this.title = getString(R.string.home_menu_check_alpr)

        if(supportActionBar != null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.elevation = 0.0f
        }

        contentView.setOnTouchListener { view , _ ->
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        checkALPRButton.setSafeOnClickListener {
            onCheckButtonPressed()
        }

        plateTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateUI()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        dropdown.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateUI()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        remarkImage.setSafeOnClickListener {
            showBottomSheetDialog()
        }

        clearButton.setSafeOnClickListener {
            onClearButtonPressed()
        }

        setupProvincesList()
        setupLocationClient()
        setupPlacesClient()
        getCurrentLocation()
        getCurrentPlace()
        updateUI()

    }

    private fun updateUI() {

        if(plateTextView.text!!.isNotEmpty() &&
            dropdown.text!!.isNotEmpty()) {
            enableAddButton()
        } else {
            disableAddButton()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


    private fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    private fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun enableAddButton() {
        checkALPRButton.isEnabled = true
    }

    private fun disableAddButton() {
        checkALPRButton.isEnabled = false
    }

    private fun setupProvincesList() {

        val json: String?
        val inputStream: InputStream = this.assets.open("province.json")
        json = inputStream.bufferedReader().use { it.readText() }

        try {

            val jsonArray = Gson().fromJson(json, ProvinceList::class.java)

            val provinceTitle = mutableListOf<String>()
            for (province in jsonArray.province) {
                provinceTitle.add(province.name)
            }

            val adapter = ArrayAdapter<String>(this, R.layout.dropdown_menu_popup_item, provinceTitle)

            dropdown.setAdapter(adapter)

        } catch (e : JsonParseException) {

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_LOCATION -> {
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Log.e(TAG, "LOCATION PERMISSION DENIED")
                }
            }
        }
    }

    //Location Function
    private fun setupPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION
        )
    }

    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
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

    //Remark Image
    private fun showBottomSheetDialog() {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = this.layoutInflater.inflate(R.layout.bottom_sheet, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)
        val dialog = dialogBuilder.create()

        dialogView.textViewCamera.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(this.applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE)
            } else {
                openCamera()
                dialog.dismiss()
            }

        }
        dialogView.textViewGallery.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(this.applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE)
            } else {

                openGallery()
                dialog.dismiss()
            }

        }

        dialog.show()
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(intent, IMAGE_GALLERY_CODE)
    }


    private fun openCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            isTakePhoto = true

            val ins : InputStream? = contentResolver.openInputStream(image_uri!!)
            val snapImage : Bitmap? = BitmapFactory.decodeStream(ins)
            ins?.close()

            if (snapImage != null) {
                remarkImage.setImageBitmap(checkALPRViewModel.modifyOrientation(this ,snapImage, image_uri!!))
                contentResolver.delete(image_uri!!, null, null)
            }
        }

        else if (requestCode == IMAGE_GALLERY_CODE && resultCode == Activity.RESULT_OK) {

            isTakePhoto = true

            exifData = data?.data!!
            val ins: InputStream? = contentResolver.openInputStream(exifData!!)
            snapImage = BitmapFactory.decodeStream(ins)

            remarkImage.setImageBitmap(checkALPRViewModel.modifyOrientation(this ,snapImage, exifData!!))

        }

    }

    private fun getImageBase64(image: ImageView): String {

        val bitmap = (image.drawable as BitmapDrawable).bitmap

        val resizedBitmap = resizeBitmap(bitmap,bitmap.width / 4,bitmap.height / 4)

        val stream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()

        val base64String: String

        base64String = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            Log.d(TAG, "Android O")
            Base64.getEncoder().encodeToString(byteArray)

        } else {
            Log.d(TAG, "Android Other")
            android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
        }
        return base64String
    }

    private fun resizeBitmap(bitmap: Bitmap, width:Int, height:Int): Bitmap {

        return Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            false
        )
    }


    private fun onCheckButtonPressed() {

        hideKeyboard()

        val identifyDialog: AlertDialog? = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("กำลังตรวจสอบ")
            .setCancelable(false)
            .build()
            .apply {
                show()
            }

        val plate = plateTextView.text.toString()
        val province = dropdown.text.toString()
        val latitude = currentLatitude
        val longitude = currentLongitude
        val address = currentAddress
        val remark = remarkTextView.text.toString()
        val image = if (isTakePhoto) { getImageBase64(remarkImage) } else { "" }

        checkALPRViewModel.checkVehicle(
            plate,
            province,
            latitude,
            longitude,
            address,
            remark,
            image).observe(this, Observer<Either<CheckALPRResponse>> { either ->

            if (either?.status == Status.SUCCESS && either.data != null) {
                if (either.data.ret == 0) {
                    if(either.data.alpr.alpr_type == "0") {
                        onNotFoundSuspect()
                    }
                    else {
                        onFoundSuspect(either.data.alpr.alpr_type)
                    }

                } else if (either.data.ret == -3) {
                    Toast.makeText(this, "มีผู้ใช้งานอื่นใช้บัญชีนี้", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else {
                    Toast.makeText(this, either.data.msg, Toast.LENGTH_SHORT).show()
                }
            } else {
                if (either?.error == ApiError.CHECKALPR) {
                    Toast.makeText(this, "ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้", Toast.LENGTH_SHORT).show()
                }
            }
            identifyDialog?.dismiss()
        })
    }

    private fun onFoundSuspect(message: String) {

        if (message.toLowerCase(Locale.getDefault()).contains("whitelist")) {

            Snackbar.make(contentView,  "พบในฐานข้อมูล $message", Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.infoColor))
                .setActionTextColor(ContextCompat.getColor(applicationContext, R.color.whiteColor))
                .show()

        } else {

            Snackbar.make(contentView,   "พบประวัติ $message", Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.dangerColor))
                .setActionTextColor(ContextCompat.getColor(applicationContext, R.color.whiteColor))
                .show()

        }
    }

    private fun onNotFoundSuspect() {
        Snackbar.make(contentView, "ไม่พบประวัติ", Snackbar.LENGTH_LONG)

            .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.successColor))
            .setActionTextColor(ContextCompat.getColor(applicationContext, R.color.whiteColor))
            .show()
    }

    private fun onClearButtonPressed() {
        isTakePhoto = false
        remarkImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_upload_image))
        plateTextView.setText("")
        dropdown.setText("")
        remarkTextView.setText("")

    }


}