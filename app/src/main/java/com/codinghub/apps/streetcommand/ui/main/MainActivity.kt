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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
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

    internal enum class TabState {
        HOME,
        CAMERA,
        LOCATION,
        NOTIFICATION
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
        nav_view_bottom.menu.findItem(R.id.nav_home).isChecked = true

        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, homeFragment)
            .add(R.id.main_container, cameraFragment)
            .add(R.id.main_container, locationsFragment)
            .add(R.id.main_container, notificationsFragment)
            .commit()
        setTabStateFragment(TabState.HOME).commit()


        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        return false
    }

    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount > 0 || !homeFragment.isHidden) {
            super.onBackPressed()
        } else {
            setTabStateFragment(TabState.HOME).commit()
            nav_view_bottom.menu.findItem(R.id.nav_home).isChecked = true
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun setTabStateFragment(state: TabState): FragmentTransaction {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val transaction = supportFragmentManager.beginTransaction()
        when (state) {
            TabState.HOME -> {
                transaction.show(homeFragment)
                transaction.hide(cameraFragment)
                transaction.hide(locationsFragment)
                transaction.hide(notificationsFragment)
            }
            TabState.CAMERA -> {
                transaction.hide(homeFragment)
                transaction.show(cameraFragment)
                transaction.hide(locationsFragment)
                transaction.hide(notificationsFragment)
            }
            TabState.LOCATION -> {
                transaction.hide(homeFragment)
                transaction.hide(cameraFragment)
                transaction.show(locationsFragment)
                transaction.hide(notificationsFragment)
            }
            TabState.NOTIFICATION -> {
                transaction.hide(homeFragment)
                transaction.hide(cameraFragment)
                transaction.hide(locationsFragment)
                transaction.show(notificationsFragment)
            }
        }
        return transaction
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                setTabStateFragment(TabState.HOME).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_camera -> {
                setTabStateFragment(TabState.CAMERA).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_locations -> {
                setTabStateFragment(TabState.LOCATION).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_notifications -> {
                setTabStateFragment(TabState.NOTIFICATION).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
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