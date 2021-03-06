package com.codinghub.apps.streetcommand.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.ui.camera.alpr.CameraCheckALPRFragment
import com.codinghub.apps.streetcommand.ui.camera.other.CameraCheckOtherFragment
import com.codinghub.apps.streetcommand.ui.camera.person.CameraCheckPersonFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_camera.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class CameraFragment : Fragment() {

    private val MAX_PREVIEW_WIDTH = 1280
    private val MAX_PREVIEW_HEIGHT = 720

    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder

    private lateinit var cameraDevice: CameraDevice

    private val deviceStateCallback = object : CameraDevice.StateCallback(){

        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "camera device opened")
            cameraDevice = camera
            previewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "camera device disconnected")
            camera.close()
        }

        override fun onError(camera: CameraDevice, p1: Int) {
            Log.d(TAG, "camera device error")
            this@CameraFragment.activity?.finish()
        }

    }
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private val cameraManager by lazy {
        activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 100
        private val TAG = CameraFragment::class.qualifiedName
        @JvmStatic fun newInstance() = CameraFragment()
    }

    lateinit var cameraViewPager: ViewPager
    lateinit var cameraTabs: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        view?.let {
            cameraViewPager = it.findViewById(R.id.cameraViewPager)
            cameraTabs = it.findViewById(R.id.cameraTabs)

        }

        val adapter = CameraViewPagerAdapter(childFragmentManager)
        adapter.addFragment(CameraCheckPersonFragment(), "บุุคคล")
        adapter.addFragment(CameraCheckALPRFragment(), "ยานพาหนะ")
        adapter.addFragment(CameraCheckOtherFragment(), "สิ่งต้องสงสัย")

        cameraViewPager.adapter = adapter
        cameraTabs.setupWithViewPager(cameraViewPager)


    }


    private fun previewSession() {

        val displayMetrics = DisplayMetrics()

        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        Log.d(TAG, width.toString())
        Log.d(TAG, height.toString())

        val surfaceTexture = previewTextureView.surfaceTexture
        surfaceTexture.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT)
        val surface = Surface(surfaceTexture)

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(
            Arrays.asList(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "create capture session failed!")

                }

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                }

                override fun onClosed(session: CameraCaptureSession) {
                    super.onClosed(session)
                    stopBackgroundThread()

                }
            }, null)
    }

    private fun closeCamera() {
        if (this::captureSession.isInitialized) {
            captureSession.close()
        }

        if (this::cameraDevice.isInitialized) {
            cameraDevice.close()
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera2 Kotlin").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }

    }

    private fun <T> cameraCharacteristics(cameraId: String, key: CameraCharacteristics.Key<T>) : T? {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return when (key) {
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)
            else -> throw IllegalArgumentException("Key not recognized")
        }
    }

    private fun cameraId(lens: Int): String {
        var deviceId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter { lens == cameraCharacteristics(it, CameraCharacteristics.LENS_FACING) }

        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
        return deviceId[0]
    }

    @SuppressLint("MissingPermission")
    private fun connectCamera() {
        val deviceId = cameraId(CameraCharacteristics.LENS_FACING_BACK)

        Log.d(TAG, "deviceId: $deviceId")
        try {
            cameraManager.openCamera(deviceId, deviceStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            Log.e(TAG, "Open camera device interrupted while opened")
        }
    }

    private val surfaceListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.d(TAG, "textureSurface width: $width height: $height" )
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @AfterPermissionGranted(REQUEST_CAMERA_PERMISSION)
    private fun checkCameraPermission() {
        if (EasyPermissions.hasPermissions(requireActivity(), Manifest.permission.CAMERA)) {
            Log.d(TAG, "App has camera permission.")
            connectCamera()
        } else {
            EasyPermissions.requestPermissions(requireActivity(),
                getString(R.string.camera_request_rationale), REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA)
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (previewTextureView.isAvailable) {
            openCamera()
        } else {
            previewTextureView.surfaceTextureListener = surfaceListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()

    }

    private fun openCamera() {
        checkCameraPermission()
    }




}