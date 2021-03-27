package net.ijool.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import net.ijool.controller.DogeController
import net.ijool.model.User
import org.json.JSONObject

class Balance : Service() {
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

    CoroutineScope(IO + job).launch {
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 5000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (service) {
            try {
              DogeController(applicationContext).balance(user.getString("cookie_doge")).cll({
                val response = JSONObject(it)
                privateIntent.putExtra("balance", response.getString("Balance").toFloat())
                privateIntent.action = "doge.balances"
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(privateIntent)
              }, {
                GlobalScope.launch(Main) {
                  delay(60000)
                }
              })

              DogeController(applicationContext).balance(user.getString("cookie_bot")).cll({
                val response = JSONObject(it)
                privateIntent.putExtra("balanceBot", response.getString("Balance").toFloat())
                privateIntent.action = "doge.balances.bot"
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(privateIntent)
              }, {
                GlobalScope.launch(Main) {
                  delay(60000)
                }
              })
            } catch (e: Exception) {
              GlobalScope.launch(Main) {
                delay(60000)
              }
            }
          } else {
            stopSelf()
            job.cancel()
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
    job.cancel()
  }

  override fun onBind(intent: Intent?): IBinder? {
    TODO("Not yet implemented")
  }
}