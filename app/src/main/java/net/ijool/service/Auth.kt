package net.ijool.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import net.ijool.controller.UserController
import net.ijool.model.User
import java.util.*
import kotlin.concurrent.schedule

class Auth : Service() {
  private lateinit var user: User
  private lateinit var job: CompletableJob
  private var service: Boolean = false

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  private fun onHandleIntent() {
    if (!::job.isInitialized || job.isCompleted) {
      job = Job()
    }

    user = User(this)
    var time = System.currentTimeMillis()

    CoroutineScope(Dispatchers.IO + job).launch {
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 30000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (service) {
            try {
              UserController(applicationContext).auth().call({ response ->
                privateIntent.putExtra("auth", response.getBoolean("auth"))
                privateIntent.action = "web.auth"
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(privateIntent)
              }, {
                GlobalScope.launch(Dispatchers.Main) {
                  delay(60000)
                }
              })
            } catch (e: Exception) {
              Log.w("error", e.message.toString())
              delay(60000)
            }
          } else {
            stopSelf()
            job.cancel()
          }
        }
      }
    }

    Timer().schedule(1000) {

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
    job.cancel()
  }

  override fun onBind(intent: Intent?): IBinder? {
    TODO("Not yet implemented")
  }
}