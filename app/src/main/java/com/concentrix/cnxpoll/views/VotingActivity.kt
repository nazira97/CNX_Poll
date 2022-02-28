@file:Suppress("DEPRECATION")
package com.concentrix.cnxpoll.views

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.concentrix.cnxpoll.R
import com.concentrix.cnxpoll.firebase.FirebaseAuthentication
import com.concentrix.cnxpoll.firebase.PollFirestore
import com.concentrix.cnxpoll.utils.AppConstants
import com.concentrix.cnxpoll.viewmodels.VotingViewModel
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_voting.*

/**
 * Created by Nazira 22/05/19
 */

class VotingActivity : AppCompatActivity() {

    val votingViewModel = object : VotingViewModel(){}
    lateinit var fireAuthentication : FirebaseAuthentication
    var email: String = ""
    var randomKey = ""
    val firestore = PollFirestore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voting)

        var mProgressDialog = ProgressDialog(this)
        email = intent.getStringExtra("email")
        randomKey = intent.getStringExtra("randomToken")
        fireAuthentication = FirebaseAuthentication(this)
        votingViewModel.displayVotingList(this, voting_list, email, tv_elected_candidate, btn_voting)
        supportActionBar?.title = "Voting"
        btn_voting.isEnabled = false
        btn_voting.setBackgroundColor(ContextCompat.getColor(this, R.color.colorMistyRose))

        val progressDialog = votingViewModel.isLoading
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.e(AppConstants.TAG, "progressBar" + it)
                if (it == true) {
                    mProgressDialog = ProgressDialog.show(this, "Loading...", "Please wait")
                } else {
                    mProgressDialog.dismiss()
                }
            }

        val alertDialog = votingViewModel.alertMessage
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                AppConstants.alertMessage(this, it)
            }
    }

    fun nextButtonClick(): Observable<String> {
        return Observable.create { emitter ->
            btn_voting.setOnClickListener {
                            showDialogOnElect()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    emitter.onNext(it)
                                }
            }
        }

    }

    override fun onStart() {
        super.onStart()

        val electButtonObserver = nextButtonClick()
        electButtonObserver.subscribe() {
            if(it == "Voting Successful"){
                logout()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_logout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            showDialogOnLogoutClick()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
    private fun logout(){
        fireAuthentication.FirebaseLogout()
        navigateToLogin()
    }

    fun navigateToLogin(){
        val intent = Intent(this, UserLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    fun showDialogOnElect() : Observable<String> {
        return Observable.create { emitter ->
            // Late initialize an alert dialog object
            lateinit var dialog: AlertDialog

            // Initialize a new instance of alert dialog builder object
            val builder = AlertDialog.Builder(this)

            // Set a title for alert dialog
            //builder.setTitle("Alert!")

            // Set a message for alert dialog
            builder.setMessage("Are you sure you want to elect selected Candidate? Click OK to Nominate and Logout.")


            // On click listener for dialog buttons
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE ->
                        votingViewModel.getRandomToken(email, this)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                val fetchedToken = it
                                Log.e(AppConstants.TAG, "fetchedToken---->" + fetchedToken)
                                if (randomKey == fetchedToken) {
                                    votingViewModel.electCandidate(this, email)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            emitter.onNext(it)
                                        }
                                } else {
                                    showDialogOnAgainLoggedIn()
                                }
                            }
                    DialogInterface.BUTTON_NEGATIVE -> emitter.onNext("Data Not Uploaded")
                }

            }

            // Set the alert dialog positive/yes button
            builder.setPositiveButton("OK", dialogClickListener)

            // Set the alert dialog negative/no button
            builder.setNegativeButton("Cancel", dialogClickListener)

            // Initialize the AlertDialog using builder object
            dialog = builder.create()

            // Finally, display the alert dialog
            dialog.show()
        }
    }

    fun showDialogOnLogoutClick() {
        // Late initialize an alert dialog object
        lateinit var dialog: AlertDialog

        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(this)

        // Set a title for alert dialog
        builder.setTitle("Logout")

        // Set a message for alert dialog
        builder.setMessage("Are you sure you want to logout")


        // On click listener for dialog buttons
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> logout()

            }
        }
        // Set the alert dialog positive/yes button
        builder.setPositiveButton("YES", dialogClickListener)

        // Set the alert dialog negative/no button
        builder.setNegativeButton("NO", dialogClickListener)

        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
    }

    fun showDialogOnAgainLoggedIn() {
        // Late initialize an alert dialog object
        lateinit var dialog: AlertDialog

        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(this)

        // Set a title for alert dialog
        builder.setTitle("Logout")

        // Set a message for alert dialog
        builder.setMessage("Failed, you have logged in other device. logging out")


        // On click listener for dialog buttons
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> navigateToLogin()
            }
        }
        // Set the alert dialog positive/yes button
        builder.setPositiveButton("OK", dialogClickListener)

        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
    }

}