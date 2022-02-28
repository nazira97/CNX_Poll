package com.concentrix.cnxpoll.viewmodels

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.concentrix.cnxpoll.firebase.PollFirestore
import com.concentrix.cnxpoll.views.NomineeListCustomAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import com.concentrix.cnxpoll.utils.CheckInternet
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Kusuma 23/05/19
 */

open class NominationViewModel {
    lateinit var listAdapter: NomineeListCustomAdapter
    val fireStore= object : PollFirestore(){}
    var nominatedNamesList = ArrayList<String>()
    var isLoading = BehaviorSubject.create<Boolean>()
    var alertMessage = BehaviorSubject.create<String>()

    fun updateNomineeList(activity: Activity, nomineeList: ListView, searchEditText: EditText, email: String, btnNominate: Button, tvSelectedNomenee: TextView){
        if (CheckInternet.isConnectingToInternet(activity)) {
            isLoading.onNext(true)
            val nominated = fireStore.fetchNominees(activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    nominatedNamesList = it

                    val nominated = fireStore.userDetails(activity, email)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            val empName = it.get("name")
                            nominatedNamesList.remove(empName)

                            listAdapter = NomineeListCustomAdapter(
                                activity,
                                nominatedNamesList,
                                nomineeList,
                                searchEditText,
                                btnNominate,
                                tvSelectedNomenee
                            )
                            nomineeList.setAdapter(listAdapter)
                            isLoading.onNext(false)
                            fun filter(text: String) {
                                val filterdNames = ArrayList<String>()

                                for (s in nominatedNamesList) {
                                    //if the existing elements contains the search input
                                    if (s.toLowerCase().contains(text.toLowerCase())) {
                                        //adding the element to filtered list
                                        filterdNames.add(s)
                                    }
                                }
                                listAdapter.filterList(filterdNames)
                                if (text == "") {
                                    listAdapter.nominatedName = ""
                                }
                            }

                            val textWatcher = object : TextWatcher {

                                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                                    filter(s.toString())
                                }

                                override fun beforeTextChanged(
                                    s: CharSequence, start: Int, count: Int, after: Int
                                ) {

                                }

                                override fun afterTextChanged(s: Editable) {

                                }
                            }
                            searchEditText.addTextChangedListener(textWatcher)
                        }
                }
            }else {
            alertMessage.onNext("Please check your Internet Connection.")
            }
    }

    fun submitNominee(activity: Activity, currentUser: String): Observable<String>{
        return Observable.create { emitter ->
            if(listAdapter.nominatedName == ""){
                alertMessage.onNext("Please Select the Candidate you want to Nominate")
            }else {
                if (CheckInternet.isConnectingToInternet(activity)) {
                    isLoading.onNext(true)
                    val nominate = fireStore.storeNominatedCandidate(activity, listAdapter.nominatedName, currentUser)
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

    fun checkNomination(activity: Activity, email: String): Observable<Boolean>{
        return Observable.create { emitter ->
            val nominated = fireStore.userDetails(activity, email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val doneNomination = it
                    if (doneNomination.get("nominated") == false) {
                        emitter.onNext(false)
                    }else{
                        emitter.onNext(true)
                    }
                }
        }
    }

    fun getRandomToken(email: String, activity: Activity): Observable<String>{
        return Observable.create { emitter ->
            fireStore.getLoggedInToken(email, activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    emitter.onNext(it)
                }
        }
    }
}

