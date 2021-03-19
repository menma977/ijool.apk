package net.ijool

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import net.ijool.model.User
import net.ijool.view.activity.LoginActivity
import net.ijool.view.activity.NavigationActivity
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var move: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    user = User(this)

    Timer().schedule(1000) {
      Log.i("token", user.getString("token"))
      if (user.getString("token").isNotEmpty()) {
        runOnUiThread {
          move = Intent(applicationContext, NavigationActivity::class.java)
          startActivity(move)
          finish()
        }
      } else {
        runOnUiThread {
          user.clear()
          move = Intent(applicationContext, LoginActivity::class.java)
          startActivity(move)
          finish()
        }
      }
    }
  }
}