package com.codinghub.apps.streetcommand.ui.home

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.models.alpr.ProvinceList
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.android.synthetic.main.activity_check_alpr.*
import kotlinx.android.synthetic.main.activity_check_person.*
import kotlinx.android.synthetic.main.activity_check_person.contentView
import java.io.InputStream

class CheckALPRActivity : AppCompatActivity() {

    private val testPlate = "กก1234"
    private val testProvince = "กรุงเทพมหานคร"

    private val TAG = CheckALPRActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_alpr)

        this.title = getString(R.string.home_menu_check_alpr)

        if(supportActionBar != null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.elevation = 0.0f
        }

        contentView.setOnClickListener {
            hideKeyboard()
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


        setupProvincesList()
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

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
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

    private fun onCheckButtonPressed() {

        hideKeyboard()

        if (plateTextView.text.toString() == testPlate && dropdown.text.toString() == testProvince) {
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


}