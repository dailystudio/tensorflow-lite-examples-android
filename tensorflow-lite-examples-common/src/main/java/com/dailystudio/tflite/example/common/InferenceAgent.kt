package com.dailystudio.tflite.example.common

import com.dailystudio.devbricksx.development.Logger
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.call
import kotlin.math.roundToLong

open class InferenceAgent<Info: InferenceInfo, Results>(
    var resultsUpdateInterval: Long = (1000/30f).roundToLong()) {

    private var lastDelivered: Long = -1

    fun deliverInferenceInfo(info: Info) {
        KDispatcher.call(Constants.EVENT_INFERENCE_INFO_UPDATE, info)
    }

    fun deliverResults(results: Results) {
        val interval = resultsUpdateInterval
        Logger.debug("interval = $interval")

        if (interval <= 0L || lastDelivered == -1L) {
            triggerResultsCallbacks(results)
        } else {
            val now = System.currentTimeMillis()
            if (now - lastDelivered > interval) {
                triggerResultsCallbacks(results)
            } else {
                Logger.warn("skip results, since interval[${now - lastDelivered}] is less than $interval")
            }
        }
    }

    private fun triggerResultsCallbacks(results: Results) {
        KDispatcher.call(Constants.EVENT_RESULTS_UPDATE, results)

        lastDelivered = System.currentTimeMillis()
    }

}