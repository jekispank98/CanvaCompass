package dev.jekis.canvacompass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import dev.jekis.canvacompass.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(setOf(R.id.XmlFragment, R.id.ComposeFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupTabs()
    }

    private fun setupTabs() {
        val toggleLayout = binding.toggleButton
        toggleLayout.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_xml -> {
                        if (navController.currentDestination?.id != R.id.XmlFragment) {
                            navController.navigate(R.id.XmlFragment)
                        }
                    }
                    R.id.button_compose -> {
                        if (navController.currentDestination?.id != R.id.ComposeFragment) {
                            navController.navigate(R.id.ComposeFragment)
                        }
                    }
                }
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.XmlFragment -> {
                    if (toggleLayout.checkedButtonId != R.id.button_xml) {
                        toggleLayout.check(R.id.button_xml)
                    }
                }
                R.id.ComposeFragment -> {
                    if (toggleLayout.checkedButtonId != R.id.button_compose) {
                        toggleLayout.check(R.id.button_compose)
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}