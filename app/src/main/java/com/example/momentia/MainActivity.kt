package com.example.momentia

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigation = findViewById(R.id.bottom_nav)
        bottomNavigation.setupWithNavController(navController)

        val fragmentsWithoutBottomNav = setOf(
            R.id.loginFragment,
            R.id.registerFragment,
            R.id.passwordFragment,
            R.id.nameFragment,
            R.id.phoneFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in fragmentsWithoutBottomNav) {
                hideBottomNavigation()
            } else {
                showBottomNavigation()
            }
        }
    }

    fun hideBottomNavigation() {
        bottomNavigation.visibility = View.GONE
    }

    fun showBottomNavigation() {
        bottomNavigation.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener { _, _, _ -> }
    }
}