package com.dailystudio.tflite.example.image.classification

import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.image.classification.fragment.ImageClassificationFragment

class ExampleActivity : AbsExampleActivity() {

    override fun createExampleFragment(): AbsExampleFragment {
        return ImageClassificationFragment()
    }

}
