package com.example.momentia

import android.os.Bundle
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

        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigation?.setupWithNavController(navController)
        bottomNavigation.visibility = View.VISIBLE

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
            R.id.changeEmailFragment,
            R.id.changeNumberFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in fragmentsWithoutBottomNav) {
                hideBottomNavigation()
            } else {
                showBottomNavigation()
            }
        }

        if (auth.currentUser != null) {
            navController.navigate(R.id.action_global_cameraFragment)
        }
    }

    fun hideBottomNavigation() {
        bottomNavigation.visibility = View.GONE
    }



//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//
//    }

    fun showBottomNavigation() {
        bottomNavigation.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener { _, _, _ -> }
    }
}