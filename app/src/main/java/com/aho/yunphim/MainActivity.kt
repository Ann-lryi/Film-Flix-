package com.aho.yunphim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aho.yunphim.ui.navigation.YunPhimNavHost
import com.aho.yunphim.ui.theme.YunPhimTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as YunPhimApp).container
        setContent {
            YunPhimTheme {
                YunPhimNavHost(container = container)
            }
        }
    }
}
