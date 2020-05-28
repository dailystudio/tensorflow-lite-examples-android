package com.dailystudio.tflite.example

import com.nostra13.universalimageloader.core.DisplayImageOptions

object Constants {

    val DEFAULT_IMAGE_LOADER_OPTIONS: DisplayImageOptions = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.color.transparent)
        .showImageOnFail(R.color.transparent)
        .showImageForEmptyUri(R.color.transparent)
        .resetViewBeforeLoading(true)
        .build()

}