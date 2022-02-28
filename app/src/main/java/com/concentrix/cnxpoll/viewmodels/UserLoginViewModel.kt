package com.concentrix.cnxpoll.viewmodels

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v4.view.ViewCompat
import android.text.TextUtils
import android.widget.EditText
import com.concentrix.cnxpoll.firebase.FirebaseAuthentication
import com.concentrix.cnxpoll.firebase.PollFirestore
import com.concentrix.cnxpoll.model.UserLoginModel
import com.concentrix.cnxpoll.utils.CheckInternet
import com.concentrix.cnxpoll.views.NominationActivity
import com.concentrix.cnxpoll.views.VotingActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Kusuma 17/05/19
 */

open class UserLoginViewModel(private val activity: Activity, private val et_email: EditText, private val et_password: EditText) {

    val userModel =  UserLoginModel()
    val fireAuthentication = FirebaseAuthentication(activity)
    val firestore = PollFirestore()
    var polltype: String = ""
    var email = ""
    var randomToken = ""
    var isLoading = BehaviorSubject.create<Boolean>()
    var alertMessage = BehaviorSubject.create<String>()

    fun afterEmailTextChanged(s: CharSequence){
        email = s.toString()
        userModel.mEmail = s.toString()
    }

    fun afterPasswordTextChanged(s: CharSequence){
       userModel.mPassword = s.toString()
    }

    fun onLoginClick(){
        doOnLoginClick(userModel)
    }

    fun doOnLoginClick(user: UserLoginModel){
       Thread(Runnable {
            if(!TextUtils.isEmpty(user.mEmail) && !TextUtils.isEmpty(user.mPassword)) {
                isLoading.onNext(true)
                if(CheckInternet.isConnectingToInternet(activity)){
                    val firebaseLogin =  fireAuthentication.firebaseLogin(user)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if(it == "Login Success"){
                                randomToken = getRandomToken(10)
                                val tokenUpdate = firestore.updateLoginToken(email, randomToken)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        if(it == "Token Updated"){
                                            getEnabledPoll(user)
                                        }else{
                                            isLoading.onNext(false)
                                            alertMessage.onNext("Login Failed Try Again")
                                        }
                                    }
                            }else{
                                 isLoading.onNext(false)
                                 alertMessage.onNext("Email or Password not valid")
                            }
                        }
                } else{
                    isLoading.onNext(false)
                    alertMessage.onNext("Please check your Internet Connection.")
                }
            }else if(TextUtils.isEmpty(this.userModel.mEmail) && TextUtils.isEmpty(this.userModel.mPassword)){
                activity.runOnUiThread {
                    val colorStateList = ColorStateList.valueOf(Color.RED)
                    ViewCompat.setBackgroundTintList(et_email, colorStateList)
                    ViewCompat.setBackgroundTintList(et_password, colorStateList)
                    alertMessage.onNext("Please enter Email and Password")
                }
            }else if(TextUtils.isEmpty(this.userModel.mEmail)) {
                activity.runOnUiThread {
                    val colorStateList = ColorStateList.valueOf(Color.RED)
                    ViewCompat.setBackgroundTintList(et_password, colorStateList)
                    alertMessage.onNext("Please enter Email")
                }
            }
            else{
                activity.runOnUiThread {
                    val colorStateList = ColorStateList.valueOf(Color.RED)
                    ViewCompat.setBackgroundTintList(et_email, colorStateList)
                    alertMessage.onNext("Please enter Password")
                }
            }
        }).start()
    }

    fun getEnabledPoll(user: UserLoginModel){
        if(CheckInternet.isConnectingToInternet(activity)) {
            val enabledPoll = firestore.fetchEnabledPoll(activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    polltype = it.get("pollType").toString()

                    if (polltype == "nomination") {
                        val nominated = firestore.userDetails(activity, user.mEmail)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                val doneNomination = it

                                if (doneNomination.get("nominated") == false) {
                                    val intent = Intent(activity, NominationActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                    intent.putExtra("email", user.mEmail)
                                    intent.putExtra("randomToken", randomToken)
                                    activity.startActivity(intent)
                                    isLoading.onNext(false)
                                } else {
                                    isLoading.onNext(false)
                                    alertMessage.onNext("You have already nominated a Candidate")
                                }
                                isLoading.onNext(false)
                            }
                    } else if (polltype == "voting") {
                        val voted = firestore.userDetails(activity, user.mEmail)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                val doneVoting = it

                                if (doneVoting.get("voted") == false) {
                                    val intent = Intent(activity, VotingActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                    intent.putExtra("email", user.mEmail)
                                    intent.putExtra("randomToken", randomToken)
                                    activity.startActivity(intent)
                                    isLoading.onNext(false)
                                } else {
                                    isLoading.onNext(false)
                                    alertMessage.onNext("You have done with Voting")
                                }
                                isLoading.onNext(false)
                            }
                    } else {
                        isLoading.onNext(false)
                        alertMessage.onNext("Poll is not Enabled currently")
                    }
                }
        }else{
            isLoading.onNext(false)
                alertMessage.onNext("Please check your Internet Connection.")
        }
    }
    fun getRandomToken(length: Int) : String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}