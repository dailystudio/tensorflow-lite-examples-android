package org.tensorflow.litex

import android.content.Context
import androidx.annotation.WorkerThread
import com.dailystudio.devbricksx.development.Logger
import org.tensorflow.lite.Delegate
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.model.Model
import java.io.IOException
import java.nio.MappedByteBuffer


abstract class LiteModel protected constructor(
    val device: Model.Device = Model.Device.CPU,
    val numOfThreads: Int = 1,
    val useXNNPack: Boolean = true
) {

    companion object {

        fun fromBuffer(modelBuffer: MappedByteBuffer,
                       device: Model.Device = Model.Device.CPU,
                       numOfThreads: Int = 1,
                       useXNNPack: Boolean = true): LiteModel {
            return BaseLiteModelImpl.Builder(modelBuffer, device, numOfThreads, useXNNPack).build()
        }

        @WorkerThread
        fun fromFile(context: Context,
                     modelPath: String,
                     device: Model.Device = Model.Device.CPU,
                     numOfThreads: Int = 1,
                     useXNNPack: Boolean = true): LiteModel? {
            Logger.debug("create model form file: [${modelPath}]")

            val modelBuffer = try {
                FileUtil.loadMappedFile(context, modelPath)
            } catch (e: IOException) {
                Logger.error("load mapped model file failed: $e")
                null
            } ?: return null

            return fromBuffer(modelBuffer, device, numOfThreads, useXNNPack)
        }
    }

    abstract class Builder (
        var device: Model.Device = Model.Device.CPU,
        var numOfThreads: Int = 1,
        var useXNNPack: Boolean = true
    ) {
        fun device  (device: Model.Device) = apply { this.device = device }
        fun numOfThreads  (numOfThreads: Int) = apply { this.numOfThreads = numOfThreads }
        fun useXNNPack  (useXNNPack: Boolean) = apply { this.useXNNPack = useXNNPack }

        abstract fun build(): LiteModel

        override fun toString(): String {
            return buildString {
                append("device: $device, ")
                append("threads: $numOfThreads, ")
                append("useXNNPack: $useXNNPack")
            }
        }
    }
    var interpreter: InterpreterApi? = null
        protected set

    protected var delegate: Delegate? = null

    abstract fun open()

    open fun close() {
        interpreter?.let {
            it.close()
            interpreter = null
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

open class BaseLiteModelImpl private constructor(
    private val modelBuffer: MappedByteBuffer,
    device: Model.Device = Model.Device.CPU,
    numOfThreads: Int = 1,
    useXNNPack: Boolean = true
): LiteModel(device, numOfThreads, useXNNPack) {

    class Builder(
        private val buffer: MappedByteBuffer,
        device: Model.Device = Model.Device.CPU,
        numOfThreads: Int = 1,
        useXNNPack: Boolean = true
    ): LiteModel.Builder(device, numOfThreads, useXNNPack) {
        override fun build() = BaseLiteModelImpl(buffer, device, numOfThreads, useXNNPack)
    }

    override fun open() {
        val interpreterOptions = Interpreter.Options()

        delegate = when (device) {
            Model.Device.NNAPI ->  NnApiDelegate()
            Model.Device.GPU ->  {
                val compatList = CompatibilityList()
                if(compatList.isDelegateSupportedOnThisDevice) {
                    val delegateOptions: GpuDelegate.Options =
                        compatList.bestOptionsForThisDevice

                    GpuDelegate(delegateOptions)
                } else {
                    interpreterOptions.setUseXNNPACK(useXNNPack)
                    null
                }
            }
            Model.Device.CPU -> {
                interpreterOptions.setUseXNNPACK(useXNNPack)
                null
            }
            else -> null
        }

        delegate?.let {
            interpreterOptions.addDelegate(it)
        }

        interpreterOptions.numThreads = numOfThreads

        interpreter = Interpreter(modelBuffer, interpreterOptions)
        Logger.debug("[NEW MODEL]: device = $device [delegate: $delegate], threads = $numOfThreads, XNNPack = $useXNNPack")
    }
}
