package com.codinghub.apps.streetcommand.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codinghub.apps.streetcommand.R
import com.codinghub.apps.streetcommand.models.error.ApiError
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.error.Status
import com.codinghub.apps.streetcommand.models.userinfo.UserInfoResponse
import com.codinghub.apps.streetcommand.models.utilities.SafeClickListener
import com.codinghub.apps.streetcommand.ui.main.MainActivity
import com.codinghub.apps.streetcommand.viewmodels.HomeViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private val TAG = HomeFragment::class.qualifiedName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

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

    private fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserInfoData()

    }

    override fun onResume() {
        super.onResume()
        loadUserInfoData()

    }

    private fun loadUserInfoData() {

        homeViewModel.getUserInfo().observe(viewLifecycleOwner, Observer<Either<UserInfoResponse>> { either ->
            if (either?.status == Status.SUCCESS && either.data != null) {
                if (either.data.ret == 0) {

                    idTextView.text = getString(R.string.user_id_string, either.data.user.username, either.data.user.user_type)
                    nameTextView.text = either.data.user.full_name
                    workDescriptionTextView.text = either.data.user.work_description ?: getString(R.string.no_work_descp_string)
                    aorTextView.text = either.data.user.area_of_responsibility ?: getString(R.string.no_aor_string)

                } else if (either.data.ret == -3) {
                    Toast.makeText(context, "มีผู้ใช้งานอื่นใช้บัญชีนี้", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity).logout()

                } else {
                    Toast.makeText(context, either.data.msg, Toast.LENGTH_SHORT).show()
                }
            } else { 
                if (either?.error == ApiError.USERINFO) {
                    Toast.makeText(context, "ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้", Toast.LENGTH_SHORT).show()
                }
            }
        })
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