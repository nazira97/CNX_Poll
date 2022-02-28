@file:Suppress("DEPRECATION")
package com.concentrix.cnxpoll.firebase

import android.app.Activity
import com.concentrix.cnxpoll.model.UserLoginModel
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Observable

/**
 * Created by Kusuma 21/05/19
 */

/*
 * This class is used firebase login authentication
 */

open class FirebaseAuthentication(var activity: Activity) {
    private var mAuth: FirebaseAuth? = null

    fun firebaseLogin(user: UserLoginModel): Observable<String> {
        return Observable.create { emitter ->
            mAuth = FirebaseAuth.getInstance()
            mAuth!!.signInWithEmailAndPassword(user.mEmail, user.mPassword)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        emitter.onNext("Login Success")
                    } else {
                        emitter.onNext("Login Failure")
                    }
                }
        }
    }

    fun FirebaseLogout(){
        mAuth = FirebaseAuth.getInstance()
        mAuth!!.signOut()
    }
}