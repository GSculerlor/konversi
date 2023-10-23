package moe.ganen.konversi.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import moe.ganen.konversi.android.home.HomeRoute
import moe.ganen.konversi.design.CelliniaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CelliniaTheme {
                HomeRoute()
            }
        }
    }
}
