package org.tensorflow.litex

import android.content.Context
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.development.Logger.debug
import org.tensorflow.lite.Delegate
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.model.Model
import java.io.IOException

data class _TFLiteInterpreter(
    val modelPath: String,
    val device: Model.Device = Model.Device.CPU,
    private val numOfThreads: Int = 1,
    val useXNNPACK: Boolean = true
) {

    private var delegate: Delegate? = null
    private val tfLiteOptions = Interpreter.Options()

    var tfLiteInterpreter: Interpreter? = null

    fun open(context: Context) {
        delegate = when (device) {
            Model.Device.NNAPI ->  NnApiDelegate()
            Model.Device.GPU ->  {
                val compatList = CompatibilityList()
                if(compatList.isDelegateSupportedOnThisDevice) {
                    val delegateOptions: GpuDelegate.Options =
                        compatList.bestOptionsForThisDevice

                    GpuDelegate(delegateOptions)
                } else {
                    tfLiteOptions.setUseXNNPACK(useXNNPACK)
                    null
                }
            }
            Model.Device.CPU -> {
                tfLiteOptions.setUseXNNPACK(useXNNPACK)
                null
            }
            else -> null
        }

        delegate?.let {
            tfLiteOptions.addDelegate(it)
        }

        tfLiteOptions.setNumThreads(numOfThreads)

        val modelBuffer = try {
            FileUtil.loadMappedFile(context, modelPath)
        } catch (e: IOException) {
            Logger.error("load mapped model file failed: $e")
            null
        }

        val ret = (modelBuffer != null)
        debug("[$ret] load Tensorflow Lite model: delegate = [${delegate}]")
        debug("[$ret] load Tensorflow Lite model: model = [${modelPath}], device = $device, threads = $numOfThreads, xnnpack = $useXNNPACK")

        modelBuffer?.let {
            tfLiteInterpreter = Interpreter(it, tfLiteOptions)
        }
    }

    fun close() {
        tfLiteInterpreter?.let {
            it.close()
            tfLiteInterpreter = null
        }

        delegate?.let {
            when (it) {
                is GpuDelegate -> it.close()
                is NnApiDelegate -> it.close()
            }

            delegate = null
        }
    }

}

open class TFLiteModel(val context: Context,
                       private val modelPaths: Array<String>,
                       val devices: Array<Model.Device>,
                       val numOfThreads: Array<Int>,
                       val useXNNPACK: Boolean = true
) {

    constructor(
        context: Context,
        modelPath: String,
        device: Model.Device = Model.Device.CPU,
        numOfThreads: Int = 1,
        useXNNPACK: Boolean = true
    ) : this (context, arrayOf(modelPath),
        arrayOf(device), arrayOf(numOfThreads), useXNNPACK)

    private val interpreters: MutableList<_TFLiteInterpreter> = mutableListOf()

    init {
        createInterpreters()
    }

    private fun createInterpreters() {
        for ((i, path) in modelPaths.withIndex()) {
            interpreters.add(
                createInterpreter(path, devices[i], numOfThreads[i], useXNNPACK)
            )
        }
    }

    protected open fun createInterpreter(modelPath: String,
                                         device: Model.Device,
                                         numOfThreads: Int,
                                         useXNNPACK: Boolean,
    ): _TFLiteInterpreter {
        return _TFLiteInterpreter(modelPath, device, numOfThreads, useXNNPACK).apply {
            open(context)
        }
    }

    fun getInterpreter(): Interpreter? {
        return getInterpreter(0)
    }

    fun getInterpreter(index: Int = 0): Interpreter? {
        return if (interpreters.size > 0) {
            interpreters.getOrNull(index)?.tfLiteInterpreter
        } else {
            null
        }
    }

    open fun close() {
        interpreters.forEach {
            it.close()
        }
    }

}