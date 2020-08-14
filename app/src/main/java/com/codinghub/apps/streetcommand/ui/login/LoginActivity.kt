package com.codinghub.apps.streetcommand.ui.login


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codinghub.apps.streetcommand.BuildConfig
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.models.error.ApiError
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.error.Status
import com.codinghub.apps.streetcommand.models.login.LoginResponse
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import com.codinghub.apps.streetcommand.ui.main.MainActivity
import com.codinghub.apps.streetcommand.viewmodels.LoginViewModel
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    private var username: String = ""
    private var password: String = ""
    private val TAG = LoginActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        checkLoginStatus()
        updateUI()

        usernameTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                username = usernameTextView.text.toString()
                updateUI()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        passwordTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                password = passwordTextView.text.toString()
                updateUI()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        loginBotton.setSafeOnClickListener {
            performLogin(username, password)
        }

        constraintLayout.setOnClickListener {
            hideKeyboard()
        }

        appVersionTextView.text = getString(R.string.app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
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
        loginBotton.isEnabled = usernameTextView.text!!.isNotEmpty() && passwordTextView.text!!.isNotEmpty()
    }

    private fun performLogin(username: String, password: String) {

        hideKeyboard()

        val identifyDialog: AlertDialog? = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("กำลังเข้าสู่ระบบ")
            .setCancelable(false)
            .build()
            .apply {
                show()
            }

        loginViewModel.streetCommandLogin(username, password).observe(this, Observer<Either<LoginResponse>> { either ->
            if (either?.status == Status.SUCCESS && either.data != null) {
                if (either.data.ret == 0) {
                    saveLoginState(either.data.access_token)
                    pushToMainActivity()
                } else {
                    Toast.makeText(this, either.data.msg, Toast.LENGTH_SHORT).show()
                }

            } else {
                if (either?.error == ApiError.LOGIN) {
                    Toast.makeText(this, "ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้", Toast.LENGTH_SHORT).show()
                }

            }
            identifyDialog?.dismiss()
        })
    }

    private fun checkLoginStatus() {
        if (loginViewModel.getLoginStatus()) {
            pushToMainActivity()
        }
    }

    private fun saveLoginState(accessToken: String) {
        loginViewModel.saveAccessToken(accessToken)
        loginViewModel.saveLoginStatus(true)
    }


    private fun pushToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }

}