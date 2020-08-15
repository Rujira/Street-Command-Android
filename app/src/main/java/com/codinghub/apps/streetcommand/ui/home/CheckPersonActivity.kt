package com.codinghub.apps.streetcommand.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.codinghub.apps.streetcommand.BuildConfig
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.models.thcard.SmartCardDevice
import com.codinghub.apps.streetcommand.models.thcard.ThaiSmartCard
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import com.codinghub.apps.streetcommand.viewmodels.CheckPersonViewModel
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
import kotlinx.android.synthetic.main.activity_check_alpr.*
import kotlinx.android.synthetic.main.activity_check_person.*
import kotlinx.android.synthetic.main.activity_check_person.contentView
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class CheckPersonActivity : AppCompatActivity() {

    private lateinit var checkPersonViewModel: CheckPersonViewModel

    private val TAG = CheckPersonActivity::class.qualifiedName

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

    private var personName: String = ""
    private var personID: String = ""

    var task = MyTask()
    private val CARD_READ = 1
    private val CARD_EMPTY = 2
    private val CARD_DETACHED = 3
    private var card_status = CARD_EMPTY

    var smartCardReader: SmartCardDevice? = null

    private lateinit var receiver: BroadcastReceiver

    private var usbDeviceIsAttached = false

    companion object {
        private const val REQUEST_LOCATION = 1
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_person)

        checkPersonViewModel = ViewModelProvider(this).get(CheckPersonViewModel::class.java)

        this.title = getString(R.string.home_menu_check_person)

        if(supportActionBar != null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.elevation = 0.0f
        }

        contentView.setOnTouchListener { view , _ ->
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        checkPersonButton.setSafeOnClickListener {
            onCheckButtonPressed()
        }

        cidTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateUI()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fullNameTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateUI()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        remarkPersonImage.setSafeOnClickListener {
            showBottomSheetDialog()
        }

        clearPersonButton.setSafeOnClickListener {
            onClearButtonPressed()
        }

        setupLocationClient()
        setupPlacesClient()
        getCurrentLocation()
        getCurrentPlace()
        updateUI()
        updateConnection()
        checkCardReaderAttached()
        if (task.status != AsyncTask.Status.RUNNING) {
            task.execute()
        }
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

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

    }

    private fun updateUI() {

        if(cidTextView.text!!.isNotEmpty() || fullNameTextView.text!!.isNotEmpty()) {
            enableCheckButton()
        } else {
            disableCheckButton()
        }
    }

    private fun enableCheckButton() {
        checkPersonButton.isEnabled = true
    }

    private fun disableCheckButton() {
        checkPersonButton.isEnabled = false
    }



    private fun setTextView(cid: String, fullName: String) {
        cidTextView.setText(cid)
        fullNameTextView.setText(fullName)
        updateUI()
    }

//    private fun resetTextView() {
//        cidTextView.setText("")
//        fullNameTextView.setText("")
//        updateUI()
//    }

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
                remarkPersonImage.setImageBitmap(checkPersonViewModel.modifyOrientation(this ,snapImage, image_uri!!))
                contentResolver.delete(image_uri!!, null, null)
            }
        }

        else if (requestCode == IMAGE_GALLERY_CODE && resultCode == Activity.RESULT_OK) {

            isTakePhoto = true

            exifData = data?.data!!
            val ins: InputStream? = contentResolver.openInputStream(exifData!!)
            snapImage = BitmapFactory.decodeStream(ins)

            remarkPersonImage.setImageBitmap(checkPersonViewModel.modifyOrientation(this ,snapImage, exifData!!))

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

//        if (cidTextView.text.toString() == testCID || fullNameTextView.text.toString() == testFullName) {
//            onFoundSuspect()
//        } else {
//            onNotFoundSuspect()
//        }
    }

    private fun onFoundSuspect() {
        Snackbar.make(contentView, "พบประวัติ", Snackbar.LENGTH_LONG)

            .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.dangerColor))
            .setActionTextColor(ContextCompat.getColor(applicationContext, R.color.whiteColor))
            .show()
    }

    private fun onNotFoundSuspect() {
        Snackbar.make(contentView, "ไม่พบประวัติ", Snackbar.LENGTH_LONG)

            .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.successColor))
            .setActionTextColor(ContextCompat.getColor(applicationContext, R.color.whiteColor))
            .show()
    }

    //Card Reader

    override fun onResume() {
        //   reset()
        super.onResume()

        updateConnection()
        checkCardReaderAttached()
        updateUI()
        if (task.status != AsyncTask.Status.RUNNING) {
            task.execute()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        task.cancel(true)
        if (receiver.isOrderedBroadcast) {
            unregisterReceiver(receiver)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        task.cancel(true)
        if (receiver.isOrderedBroadcast) {
            unregisterReceiver(receiver)
        }

        finish()
        return true
    }

    fun handleUsbAttached() {

        usbDeviceIsAttached = true
        checkCardReaderAttached()
    }

    fun handleUsbDetached() {

        usbDeviceIsAttached = false
        card_status = CARD_EMPTY
        smartCardReader = null
    }

    private fun updateConnection() {

        // Register USB Broadcast for Detecting USB

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                when (intent?.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> handleUsbAttached()
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> handleUsbDetached()
                }
            }
        }
        this.registerReceiver(receiver, filter)
    }

    private fun checkCardReaderAttached() {

        if (smartCardReader == null) {
            smartCardReader = SmartCardDevice.getSmartCardDevice(
                this.applicationContext,
                object : SmartCardDevice.SmartCardDeviceEvent {

                    override fun OnReady(device: SmartCardDevice) {

                        if (task.status != AsyncTask.Status.RUNNING) {
                            task.execute()
                        }
                    }

                    override fun OnDetached(device: SmartCardDevice) {


                    }
                }
            )
        }
    }

    fun readDataFromCardReader() {

        if (smartCardReader != null) {

            val thaiSmartCard = ThaiSmartCard(smartCardReader)
            if (thaiSmartCard.isInserted) {
                if (card_status != CARD_READ) {

                    val info = thaiSmartCard.personalInformation

                    if (info != null) {

                        personID = info.PersonalID
                        personName = info.NameTH

                        setTextView(personID, personName)

                        if (readImageSwitch.isChecked) {
                            remarkPersonImage.setImageBitmap(thaiSmartCard.personalPicture)
                        }

                        card_status = CARD_READ

                    } else {
                        //resetTextView()
                        card_status = CARD_EMPTY
                    }
                }

            } else {
               // resetTextView()
                card_status = CARD_DETACHED
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    inner class MyTask : AsyncTask<String, Int, String>() {

        override fun onPreExecute() {

           // checkCardReaderAttached()
        }

        override fun doInBackground(vararg params: String): String {
            var i =0
            while (i != -1) {
                Thread.sleep(1000)
                i++
                if (usbDeviceIsAttached) {

                    runOnUiThread {
                        readDataFromCardReader()
                    }
                }
            }
            return ""
        }

        override fun onPostExecute(result: String) {

        }
    }

    private fun onClearButtonPressed() {
        isTakePhoto = false
        remarkPersonImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_upload_image))
        cidTextView.setText("")
        fullNameTextView.setText("")
        remarkPersonTextView.setText("")

    }

}