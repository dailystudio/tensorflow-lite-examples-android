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

private data class _TFLiteInterpreter(
    val modelPath: String,
    val device: Model.Device = Model.Device.CPU,
    private val numOfThreads: Int = 1
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
                    tfLiteOptions.setUseXNNPACK(true)
                    null
                }
            }
            Model.Device.CPU -> {
                tfLiteOptions.setUseXNNPACK(true)
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
        debug("[$ret] load Tensorflow Lite model: [${modelPath}]", modelPath)

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
                       val device: Model.Device = Model.Device.CPU,
                       val numOfThreads: Int = 1) {

    constructor(
        context: Context,
        modelPath: String,
        device: Model.Device = Model.Device.CPU,
        numOfThreads: Int = 1
    ) : this (context, arrayOf(modelPath), device, numOfThreads)

    private val interpreters: MutableList<_TFLiteInterpreter> = mutableListOf()

    init {
        for (p in modelPaths) {
            interpreters.add(
                _TFLiteInterpreter(p, device, numOfThreads).apply {
                    open(context)
                }
            )
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