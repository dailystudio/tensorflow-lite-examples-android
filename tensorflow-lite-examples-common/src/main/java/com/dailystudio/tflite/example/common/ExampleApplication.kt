package com.dailystudio.tflite.example.common

import com.dailystudio.devbricksx.app.DevBricksApplication
import com.dailystudio.devbricksx.development.Logger
import com.facebook.stetho.Stetho
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

class ExampleApplication : DevBricksApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.USE_STETHO) {
            Stetho.initializeWithDefaults(this)
        }

        val config = ImageLoaderConfiguration.Builder(this).build()

        ImageLoader.getInstance().init(config)

        Logger.info("application is running in %s mode.",
            if (BuildConfig.DEBUG) "DEBUG" else "RELEASE")
    }

    override fun isDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }

}
