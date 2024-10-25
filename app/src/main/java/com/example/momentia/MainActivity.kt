package com.example.momentia

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigation = findViewById(R.id.bottom_nav)
        bottomNavigation.setupWithNavController(navController)
        showBottomNavigation()

        val fragmentsWithoutBottomNav = setOf(
            R.id.loginFragment,
            R.id.registerFragment,
            R.id.passwordFragment,
            R.id.usernameFragment,
            R.id.nameFragment,
            R.id.phoneFragment,
            R.id.profileFragment,
            R.id.editProfileFragment,
            R.id.editNameFragment,
            R.id.changePasswordFragment,
            R.id.changeNumberFragment
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
}