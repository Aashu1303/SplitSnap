package com.splitsnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.splitsnap.ui.navigation.SplitSnapNavGraph
import com.splitsnap.ui.theme.SplitSnapTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var keepSplashScreen = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen - it will use the theme configuration
        // to match our custom splash screen design
        val splashScreen = installSplashScreen()

        // Keep splash screen visible until our custom splash is ready
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            SplitSnapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Hide system splash once our custom splash is rendered
                    LaunchedEffect(Unit) {
                        // Small delay to ensure custom splash screen is fully rendered
                        delay(100)
                        keepSplashScreen = false
                    }
                    
                    SplitSnapNavGraph(navController = navController)
                }
            }
        }
    }
}
