package com.enkod.enkodpushlibrary

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UpdateTokenService : Service() {

    private val TAG = "EnkodPushLibrary"
    private val ACCOUNT_TAG: String = "${TAG}_ACCOUNT"



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, EnkodPushLibrary.createdNotificationForNetworkService(this), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }
        else {
            startForeground(1, EnkodPushLibrary.createdNotificationForNetworkService(this))
        }


        CoroutineScope(Dispatchers.IO).launch {

            delay(3000)

            EnkodPushLibrary.initRetrofit()


            val preferences = applicationContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
            var preferencesAcc = preferences.getString(ACCOUNT_TAG, null)

            if (preferencesAcc != null) {

                try {

                    EnkodPushLibrary.initRetrofit()

                    FirebaseMessaging.getInstance().deleteToken()

                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {

                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->

                                    if (task.isSuccessful) {

                                        val token = task.result

                                        EnkodPushLibrary.init(
                                            applicationContext,
                                            preferencesAcc!!,
                                            token
                                        )

                                        BackgroundTasks(applicationContext).verificationOfTokenWorker(applicationContext)

                                        CoroutineScope(Dispatchers.IO).launch {

                                            delay(5000)

                                            stopSelf()
                                        }

                                    } else {
                                        Log.d("doWork", "error_token_receiving")

                                        stopSelf()
                                    }
                                }

                            } else {
                                Log.d("doWork", "error_token_delete")

                                stopSelf()
                            }
                        }

                } catch (e: Exception) {

                    Log.d("doWork", "error_fcm_request")

                    stopSelf()

                }
            }
        }
    }


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

}