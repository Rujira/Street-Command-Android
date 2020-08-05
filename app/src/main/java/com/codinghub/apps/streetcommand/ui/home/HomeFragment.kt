package com.codinghub.apps.streetcommand.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private val TAG = HomeFragment::class.qualifiedName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.checkCidCardLayout.setSafeOnClickListener {
            onCheckPersonPressed()
        }

        view.checkALPRCardLayout.setSafeOnClickListener {
            onCheckALPRPressed()
        }

        view.checkHistoryCardLayout.setSafeOnClickListener {
            onCheckHistoryPressed()
        }

        view.sosButton.setSafeOnClickListener {
            Log.d(TAG,"Button")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()

    }

    private fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }


    private fun updateUI() {
        idTextView.text = getString(R.string.mock_id_string)
        nameTextView.text = getString(R.string.mock_name_string)
    }

    private fun onCheckPersonPressed() {
        val checkPersonIntent = Intent(activity, CheckPersonActivity::class.java)
        startActivity(checkPersonIntent)
    }

    private fun onCheckALPRPressed() {
        var checkPersonIntent = Intent(activity, CheckALPRActivity::class.java)
        startActivity(checkPersonIntent)
    }

    private fun onCheckHistoryPressed() {
        var checkHistoryIntent = Intent(activity, CheckHistoryActivity::class.java)
        startActivity(checkHistoryIntent)
    }



}