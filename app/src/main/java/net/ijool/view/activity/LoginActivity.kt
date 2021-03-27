package net.ijool.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import net.ijool.R
import net.ijool.config.Loading
import net.ijool.controller.UserController
import net.ijool.controller.volley.web.HandleError
import net.ijool.model.User

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

    //username.setText("menma977")
    //password.setText("ika120517")

    signIn.setOnClickListener {
      if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
        doRequestPermission()
      } else {
        loading.openDialog()
        UserController(this).login(username.text.toString(), password.text.toString()).call({ response ->
          user.setString("name", response.getString("name"))
          user.setString("username", response.getString("username"))
          user.setString("email", response.getString("email"))
          user.setString("token", response.getString("token"))
          user.setString("cookie_doge", response.getString("cookie_doge"))
          user.setString("wallet_doge", response.getString("wallet_doge"))
          user.setString("cookie_bot", response.getString("cookie_bot"))
          user.setString("wallet_bot", response.getString("wallet_bot"))
          move = Intent(applicationContext, NavigationActivity::class.java)
          startActivity(move)
          loading.closeParent()
        }, { error ->
          Toast.makeText(applicationContext, HandleError(error).result().getString("message"), Toast.LENGTH_LONG).show()
          loading.closeDialog()
        })
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