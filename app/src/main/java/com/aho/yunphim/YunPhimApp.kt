package com.aho.yunphim

import android.app.Application
import com.aho.yunphim.di.AppContainer

class YunPhimApp : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
