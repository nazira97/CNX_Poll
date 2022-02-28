package com.concentrix.cnxpoll.utils

import android.app.Activity
import android.support.v7.app.AlertDialog

/**
 * Created by Kusuma 17/05/19
 */

object AppConstants {
    val TAG = "LogTesting"

    /*
     * Method for alert message
     */
    fun alertMessage(activity:Activity, message: String){
        val buildAlert = AlertDialog.Builder(activity)
        buildAlert.setMessage(message)
        buildAlert.setCancelable(true)
        buildAlert.setPositiveButton(
            "Ok"
        ) { dialog, id -> dialog.cancel() }
        val alert = buildAlert.create()
        alert.show()
    }
}