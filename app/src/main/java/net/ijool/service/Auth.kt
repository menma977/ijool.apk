package net.ijool.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.ijool.controller.UserController
import java.util.*
import kotlin.concurrent.schedule

class Auth : Service() {
  private var service: Boolean = false

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  private fun onHandleIntent() {
    var time = System.currentTimeMillis()
    val trigger = Object()

    Timer().schedule(1000) {
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 30000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (service) {
            synchronized(trigger) {
              try {
                val getAuth = UserController().auth(applicationContext)
                if (getAuth.getInt("code") < 200) {
                  privateIntent.putExtra("auth", getAuth.getBoolean("auth"))
                  privateIntent.action = "web.auth"
                  LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(privateIntent)
                } else {
                  trigger.wait(60000)
                }
              } catch (e: Exception) {
                Log.w("error", e.message.toString())
                trigger.wait(60000)
              }
            }
          } else {
            stopSelf()
          }
        }
      }
    }
  }

  override fun onCreate() {
    super.onCreate()
    onHandleIntent()
    service = true
  }

  override fun onDestroy() {
    super.onDestroy()
    service = false
  }

  override fun onBind(intent: Intent?): IBinder? {
    TODO("Not yet implemented")
  }
}