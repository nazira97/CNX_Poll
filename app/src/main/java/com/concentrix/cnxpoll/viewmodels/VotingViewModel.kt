package com.concentrix.cnxpoll.viewmodels

import android.R
import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.*
import com.concentrix.cnxpoll.firebase.PollFirestore
import com.concentrix.cnxpoll.utils.CheckInternet
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Nazira 22/05/19
 */

open class VotingViewModel() {

    val firestore = object : PollFirestore(){}
    var selectedName:String = ""
    var isLoading = BehaviorSubject.create<Boolean>()
    var alertMessage = BehaviorSubject.create<String>()

    fun displayVotingList(activity: Activity, voting_list: ListView, email: String, tv_elected_candidate: TextView, btnVoting: Button){

        if (CheckInternet.isConnectingToInternet(activity)) {
            isLoading.onNext(true)
            var empName: Any? = ""
            val votingList = firestore.fetchVoting(activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val list = it
                    val removeName = firestore.userDetails(activity, email)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            empName = it.get("name")
                            list.remove(empName)
                            val listAdapter =
                                ArrayAdapter<String>(activity, R.layout.simple_list_item_1, R.id.text1, list)
                            voting_list.setAdapter(listAdapter)
                            isLoading.onNext(false)
                        }
                    voting_list.onItemClickListener = object : AdapterView.OnItemClickListener {

                        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            selectedName = list[position]
                            tv_elected_candidate.text = selectedName
                            btnVoting.isEnabled = true
                            btnVoting.setBackgroundColor(
                                ContextCompat.getColor(
                                    activity,
                                    com.concentrix.cnxpoll.R.color.colorpink
                                )
                            )
                        }
                    }
                }
        }else {
            alertMessage.onNext("Please check your Internet Connection.")
        }
    }

    fun electCandidate(activity: Activity,currentUser : String) : Observable<String>{
        return Observable.create {emitter ->
            if(selectedName == ""){
                alertMessage.onNext("Please Select the Candidate you want to Elect")
            }else {
                if (CheckInternet.isConnectingToInternet(activity)) {
                    isLoading.onNext(true)
                    val successMessage = firestore.storeElectedCandidate(activity, selectedName, currentUser)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            emitter.onNext(it)
                        }
                    isLoading.onNext(false)
                }else {
                    alertMessage.onNext("Please check your Internet Connection.")
                }
            }
        }
    }
    fun getRandomToken(email: String, activity: Activity): Observable<String>{
        return Observable.create { emitter ->
            firestore.getLoggedInToken(email, activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    emitter.onNext(it)
                }
        }
    }
}