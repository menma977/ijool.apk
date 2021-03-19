package net.ijool.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import net.ijool.R
import net.ijool.config.Loading
import net.ijool.controller.UserController
import net.ijool.model.User
import java.util.*
import kotlin.concurrent.schedule

class LoginActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var move: Intent
  private lateinit var username: EditText
  private lateinit var password: EditText
  private lateinit var signIn: Button
  private lateinit var signUp: TextView
  private lateinit var forgetPassword: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    user = User(this)
    loading = Loading(this)

    username = findViewById(R.id.editTextUsername)
    password = findViewById(R.id.editTextPassword)
    signIn = findViewById(R.id.buttonLogin)
    signUp = findViewById(R.id.textViewRegister)
    forgetPassword = findViewById(R.id.textViewForgetPassword)

    signIn.setOnClickListener {
      if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
        doRequestPermission()
      } else {
        loading.openDialog()
        Timer().schedule(1000) {
          val login = UserController().login(applicationContext, username.text.toString(), password.text.toString())
          if (login.getInt("code") < 400) {
            runOnUiThread {
              move = Intent(applicationContext, NavigationActivity::class.java)
              startActivity(move)
              loading.closeDialog()
              finish()
            }
          } else {
            runOnUiThread {
              loading.closeDialog()
              Toast.makeText(applicationContext, login.getString("message"), Toast.LENGTH_LONG).show()
            }
          }
        }
      }
    }

    signUp.setOnClickListener {
      move = Intent(Intent.ACTION_VIEW, Uri.parse("https://ijool.net/register"))
      startActivity(move)
      finish()
    }

    forgetPassword.setOnClickListener {
      move = Intent(Intent.ACTION_VIEW, Uri.parse("https://ijool.net/forgot-password"))
      startActivity(move)
      finish()
    }
  }

  private fun doRequestPermission() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
    }
  }
}