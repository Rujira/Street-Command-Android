package com.codinghub.apps.streetcommand.ui.home

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbManager
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.codinghub.apps.streetcommand.BuildConfig
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.models.thcard.SmartCardDevice
import com.codinghub.apps.streetcommand.models.thcard.ThaiSmartCard
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_check_person.*
import java.io.ByteArrayOutputStream
import java.util.*

class CheckPersonActivity : AppCompatActivity() {

    private val testCID = "1102001014501"
    private val testFullName = "รุจิระ เพชรรุ่ง"

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

    private val TAG = CheckPersonActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_person)

        this.title = getString(R.string.home_menu_check_person)

        if(supportActionBar != null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.elevation = 0.0f
        }

        contentView.setOnClickListener {
            hideKeyboard()
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

    private fun resetTextView() {
        cidTextView.setText("")
        fullNameTextView.setText("")
        updateUI()
    }

    private fun onCheckButtonPressed() {

        hideKeyboard()

        if (cidTextView.text.toString() == testCID || fullNameTextView.text.toString() == testFullName) {
            onFoundSuspect()
        } else {
            onNotFoundSuspect()
        }
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


                       // personImage = thaiSmartCard.personalPicture
//                        val stream = ByteArrayOutputStream()
//                        thaiSmartCard.personalPicture.compress(Bitmap.CompressFormat.JPEG, 90, stream)
//                        val byteArray = stream.toByteArray()
//
//                        var base64String: String
//
//                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
//                            Log.d(TAG, "Android O")
//                            base64String = Base64.getEncoder().encodeToString(byteArray)
//
//                        } else {
//                            Log.d(TAG, "Android Other")
//                            base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
//                        }

//                        picture1 = base64String
//
//                        showFaceDialog()

                        card_status = CARD_READ

                    } else {
                        resetTextView()
                        card_status = CARD_EMPTY
                    }
                }

            } else {
                resetTextView()
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

}