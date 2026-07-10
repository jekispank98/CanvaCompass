package dev.jekis.canvacompass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.tabs.TabLayout
import dev.jekis.canvacompass.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupTabs()
    }

    private fun setupTabs() {
        val tabLayout = binding.tabLayout
        val xmlTab = tabLayout.newTab().setText(R.string.xml)
        val composeTab = tabLayout.newTab().setText(R.string.compose)

        tabLayout.addTab(xmlTab)
        tabLayout.addTab(composeTab)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                when (tab?.position) {
                    0 -> navController.navigate(R.id.XmlFragment)
                    1 -> navController.navigate(R.id.ComposeFragment)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        findNavController(R.id.nav_host_fragment_content_main).addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.XmlFragment -> tabLayout.selectTab(xmlTab)
                R.id.ComposeFragment -> tabLayout.selectTab(composeTab)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}