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

open class TFLiteModel(@JvmField val context: Context,
                       @JvmField val modelPath: String,
                       @JvmField val device: Model.Device = Model.Device.CPU,
                       @JvmField val numOfThreads: Int = 1) {

    @JvmField protected var delegate: Delegate? = null
    @JvmField  protected var tfLiteInterpreter: Interpreter? = null
    private val tfLiteOptions = Interpreter.Options()

    init {
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

        debug("load Tensorflow Lite model: [%s]", modelPath)
        val modelBuffer = try {
            FileUtil.loadMappedFile(context, modelPath)
        } catch (e: IOException) {
            Logger.error("load mapped model file failed: $e")
            null
        }

        modelBuffer?.let {
            tfLiteInterpreter = Interpreter(it, tfLiteOptions)
        }
    }

    open fun close() {
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