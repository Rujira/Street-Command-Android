package com.codinghub.apps.streetcommand.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codinghub.apps.streetcommand.R

class CheckHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_history)

        this.title = getString(R.string.home_menu_check_history)

        if(supportActionBar != null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.elevation = 0.0f
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        finish()
        return true
    }
}