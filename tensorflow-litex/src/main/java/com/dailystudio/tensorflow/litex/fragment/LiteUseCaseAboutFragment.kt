package com.dailystudio.tensorflow.litex.fragment

import android.content.res.Configuration
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.fragment.AbsAboutFragment
import com.dailystudio.tensorflow.litex.R


abstract class LiteUseCaseAboutFragment: AbsAboutFragment() {

    private var mediaPlayer: MediaPlayer? = null
    private var videoView: TextureView? = null

    protected open val aboutVideoResId: Int = -1

    private val videoEnabled: Boolean
        get() { return (aboutVideoResId != -1) }

    override val fragmentLayoutResource: Int
        get() = R.layout.fragment_lite_use_case_about

    override fun setupCustomizedView(view: View?) {
        super.setupCustomizedView(view)

        videoView = view?.findViewById(R.id.about_app_thumb_video)
        Logger.debug("videoEnabled = $videoEnabled")

        if (!videoEnabled) {
            videoView?.visibility = View.GONE
        } else {
            mediaPlayer = MediaPlayer.create(requireContext(), aboutVideoResId)

            videoView?.visibility = View.VISIBLE
            videoView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, p1: Int, p2: Int) {
                    val surface = Surface(surfaceTexture)

                    mediaPlayer?.let { player ->
                        player.setSurface(surface)

                        player.start()
                        player.isLooping = true
                    }

                    changeVideoSize();
                }

                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
                }

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                    return true
                }

                override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                }
            }
        }
    }

    override fun bindThumb(view: View?) {
        Logger.debug("videoEnabled = $videoEnabled")
        if (!videoEnabled) {
            super.bindThumb(view)
        }
    }

    fun changeVideoSize() {
        val context = requireContext()
        val videoView = videoView ?: return
        val player = mediaPlayer ?: return

        val wOfVideo = player.videoWidth.toFloat()
        val hOfVideo = player.videoHeight.toFloat()
        val wOfView = videoView.width
        val hOfView = videoView.height

        Logger.debug("SIZE: $wOfVideo x ${hOfVideo}, view: $wOfView x $hOfView")

        val orientation = context.resources.configuration.orientation

        val scaleX = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            hOfView / hOfVideo / (wOfView / wOfVideo)
        } else {
            1.0f
        }
        val scaleY = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            1.0f
        } else {
            wOfView / wOfVideo / (hOfView / hOfVideo)
        }

        // Calculate pivot points, in our case crop from center
        val pivotPointX: Float = wOfView / 2.0f
        val pivotPointY: Float = hOfView / 2.0f

        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY)

        videoView.setTransform(matrix)
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}