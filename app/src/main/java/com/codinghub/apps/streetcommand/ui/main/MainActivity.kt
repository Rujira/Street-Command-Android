package com.codinghub.apps.streetcommand.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.codinghub.apps.streetcommand.BuildConfig
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.ui.camera.CameraFragment
import com.codinghub.apps.streetcommand.ui.home.HomeFragment
import com.codinghub.apps.streetcommand.ui.location.LocationsFragment
import com.codinghub.apps.streetcommand.ui.login.LoginActivity
import com.codinghub.apps.streetcommand.ui.notification.NotificationsFragment
import com.codinghub.apps.streetcommand.viewmodels.LoginViewModel
import com.codinghub.apps.streetcommand.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mainViewModel: MainViewModel

    private val TAG = MainActivity::class.qualifiedName

    private val homeFragment = HomeFragment()
    private val cameraFragment = CameraFragment()
    private val locationsFragment = LocationsFragment()
    private val notificationsFragment = NotificationsFragment()

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        Log.d(TAG, "onNavigationItemSelectedListener")

        val fragment = when (item.itemId) {
            R.id.nav_home -> homeFragment
            R.id.nav_camera -> cameraFragment
            R.id.nav_locations -> locationsFragment
            R.id.nav_notifications -> notificationsFragment
            else -> HomeFragment()
        }
        switchToFragment(fragment)

        true
    }

    private fun switchToFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, fragment).commit()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        nav_view_bottom.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        switchToFragment(homeFragment)

        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        return false
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.nav_about -> {

            }

            R.id.nav_help -> {

            }

            R.id.nav_settings -> {

            }

            R.id.nav_policy -> {

            }

            R.id.nav_logout -> {
                logout()
            }
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun updateUI() {

        nav_view.getHeaderView(0).headerTitleTextView.text = getString(R.string.navigation_title)
        nav_view.getHeaderView(0).headerSubTitleTextView.text =
            getString(R.string.navigation_subtitle, BuildConfig.VERSION_NAME)

    }

    fun logout() {

        mainViewModel.removeAccessToken()
        mainViewModel.saveLoginStatus(false)

        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(loginIntent)

    }
}