package com.codinghub.apps.streetcommand.ui.camera.alpr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_check_alpr.*
import kotlinx.android.synthetic.main.activity_check_alpr.contentView
import kotlinx.android.synthetic.main.activity_check_person.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_camera_check_alpr.view.*

class CameraCheckALPRFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_camera_check_alpr, container, false)

        view.takeALPRPhotoButton.setSafeOnClickListener {
            onNotFoundSuspect()
        }
        return view
    }

    private fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }


    private fun onCheckButtonPressed() {

    }

    private fun onFoundSuspect() {
        Snackbar.make(contentView, "พบประวัติ", Snackbar.LENGTH_LONG)
            .setAnchorView(requireActivity().nav_view_bottom)
            .setBackgroundTint(ContextCompat.getColor(requireActivity().applicationContext, R.color.dangerColor))
            .setActionTextColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.whiteColor))
            .show()
    }

    private fun onNotFoundSuspect() {
        Snackbar.make(contentView, "ไม่พบประวัติ", Snackbar.LENGTH_LONG)
            .setAnchorView(requireActivity().nav_view_bottom)
            .setBackgroundTint(ContextCompat.getColor(requireActivity().applicationContext, R.color.successColor))
            .setActionTextColor(ContextCompat.getColor(requireActivity().applicationContext, R.color.whiteColor))
            .show()
    }

}