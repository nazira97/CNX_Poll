@file:Suppress("DEPRECATION")
package com.concentrix.cnxpoll.views

import android.app.ProgressDialog
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.concentrix.cnxpoll.R
import com.concentrix.cnxpoll.databinding.ActivityUserLoginBinding
import com.concentrix.cnxpoll.firebase.PollFirestore
import com.concentrix.cnxpoll.viewmodels.UserLoginViewModel
import kotlinx.android.synthetic.main.activity_user_login.*
import com.concentrix.cnxpoll.utils.AppConstants.TAG
import com.concentrix.cnxpoll.utils.AppConstants.alertMessage
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Kusuma 17/05/19
 */

class UserLoginActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var mProgressDialog = ProgressDialog(this)

        val binding: ActivityUserLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_login)
        val loginViewModel = object : UserLoginViewModel(this, et_email, et_password){}
        binding.userLoginViewModel = loginViewModel

        val progressDialog = loginViewModel.isLoading
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.e(TAG, "progressBar" + it)
                if (it == true) {
                    mProgressDialog = ProgressDialog.show(this, "Signing In", "Loading ...")
                } else {
                    mProgressDialog.dismiss()
                }
            }

        val alertDialog = loginViewModel.alertMessage
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                alertMessage(this, it)
            }
    }
}