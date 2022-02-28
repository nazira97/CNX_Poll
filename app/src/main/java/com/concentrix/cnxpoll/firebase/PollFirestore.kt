package com.concentrix.cnxpoll.firebase

import android.app.Activity
import android.util.Log
import com.concentrix.cnxpoll.utils.AppConstants.TAG
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.reactivex.Observable
import com.concentrix.cnxpoll.utils.AppConstants.alertMessage
import com.google.firebase.firestore.FirebaseFirestoreSettings
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Kusuma 21/05/19
 */

/*
 * This class is used to update and retrieve data from firestore
 */

open class PollFirestore {
    // To Access a Cloud Firestore instance
    val db = FirebaseFirestore.getInstance()
    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()

    fun fetchEnabledPoll(activity: Activity): Observable<MutableMap<String, Any>> {
        db.firestoreSettings = settings
        return Observable.create { emitter ->
            val docRef = db.collection("EnablePoll").document("poll")
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.data != null) {
                        val poll: MutableMap<String, Any> = document.data!!
                        emitter.onNext(poll)

                    } else {
                        activity.runOnUiThread {
                            alertMessage(activity,"No such document" )
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    activity.runOnUiThread {
                        alertMessage(activity,"something went wrong, please try again later" )
                    }
                }
        }
    }

    fun userDetails(activity: Activity, email: String): Observable<MutableMap<String, Any>>{
        db.firestoreSettings = settings
        return Observable.create { emitter ->
                val emailId = email.toLowerCase()
                val docRef = db.collection("UserDetails").document(emailId)
                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document.data != null) {
                            val nomination: MutableMap<String, Any> = document.data!!
                            emitter.onNext(nomination)

                        } else {
                            activity.runOnUiThread {
                                alertMessage(activity, "No such document")
                                // activity.recreate()
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        activity.runOnUiThread {
                            alertMessage(activity, "something went wrong, please try again later")
                        }
                    }
        }
    }

    fun fetchNominees(activity: Activity): Observable<ArrayList<String>> {
        db.firestoreSettings = settings
        return Observable.create { emitter ->
            val roleType = ArrayList<String>()
            db.collection("Nominees")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        roleType.add(document.id)
                        }
                        emitter.onNext(roleType)
                    }
                .addOnFailureListener { exception ->
                    activity.runOnUiThread {
                         alertMessage(activity, "something went wrong, please try again later")
                        }
                    }
        }
    }

    fun fetchVoting(activity: Activity): Observable<ArrayList<String>> {
        db.firestoreSettings = settings
        return Observable.create { emitter ->
            val getLimit = fetchEnabledPoll(activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val limit = it.get("voteListLimit") as Long
                    val roleType = ArrayList<String>()
                    db.collection("Nominees")
                        .orderBy("nominationCount", Query.Direction.DESCENDING)
                        .limit(limit)
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                roleType.add(document.id)
                            }
                            emitter.onNext(roleType)
                        }
                        .addOnFailureListener { exception ->
                            activity.runOnUiThread {
                                alertMessage(activity, "Fetching Failed")
                            }
                        }
                }
        }
    }

    fun storeNominatedCandidate(activity: Activity, name : String, currentUser : String): Observable<String>{
        db.firestoreSettings = settings
        return Observable.create { emitter ->
                val batch = db.batch()
                val docRefToInc = db.collection("Nominees").document(name)

                batch.update(docRefToInc, "nominationCount", com.google.firebase.firestore.FieldValue.increment(1))

                val docRef = db.collection("UserDetails").document(currentUser.toLowerCase())
                batch.update(docRef, "nominated", true)
                batch.commit()
                    .addOnSuccessListener {
                        emitter.onNext("Nomination Successful")
                    }
                    .addOnFailureListener {
                        emitter.onNext("Nomination Failed")
                    }
        }
    }

    fun storeElectedCandidate(activity: Activity, name : String, currentUser : String): Observable<String> {
        db.firestoreSettings = settings
        return Observable.create { emitter ->
            val batch = db.batch()
            val docRefToInc = db.collection("Nominees").document(name)
            batch.update(docRefToInc, "voteCount", com.google.firebase.firestore.FieldValue.increment(1))
            val docRef = db.collection("UserDetails").document(currentUser.toLowerCase())
            batch.update(docRef, "voted", true)
            batch.commit()
                .addOnSuccessListener {
                    emitter.onNext("Voting Successful")
                }
                .addOnFailureListener {
                    emitter.onNext("Voting Failed")
                }
        }
    }

    fun updateLoginToken(userEmail: String, loginToken: String): Observable<String>{
        return Observable.create { emitter ->
            val keyRef = db.collection("UserLoginToken").document(userEmail)
            val batch = db.batch()
            batch.update(keyRef, "keyString", loginToken)
            batch.commit()
                .addOnSuccessListener {
                    emitter.onNext("Token Updated")
                }
                .addOnFailureListener {
                    emitter.onNext("Token Update failed")
                }
        }
    }

    fun getLoggedInToken(userEmail: String, activity: Activity): Observable<String>{
        return Observable.create { emitter ->
            val docRef = db.collection("UserLoginToken").document(userEmail)
            docRef.get()
                .addOnSuccessListener { document ->
                    val storedToken: String = document.get("keyString").toString()
                    Log.e(TAG, "storedToken" + storedToken)
                    emitter.onNext(storedToken)
                }
                .addOnFailureListener { exception ->
                    activity.runOnUiThread {
                        alertMessage(activity, "something went wrong, please try again later")
                    }
                }
        }
    }
}