package gr.posokanei4real.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import gr.posokanei4real.app.navigation.AppNavigation
import gr.posokanei4real.app.ui.theme.PosoKanei4RealTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PosoKanei4RealTheme {
                AppNavigation()
            }
        }
    }
}
