package net.ijool.controller

import android.content.Context
import net.ijool.model.User
import org.json.JSONObject
import net.ijool.controller.volley.web.PostController
import net.ijool.controller.volley.web.GetController

class UserController(private val context: Context) {
  private var user: User = User(context)

  fun login(username: String, password: String) = PostController(
    context,
    "login",
    JSONObject()
      .put("username", username)
      .put("password", password)
  )

  fun auth() = GetController(
    context,
    "check",
    user.getString("token")
  )

  fun subscribed() = GetController(
    context,
    "subscribe",
    user.getString("token")
  )

  fun withdraw(amount: String, wallet: String) = PostController(
    context,
    "coin.withdraw",
    JSONObject()
      .put("amount", amount)
      .put("wallet", wallet),
    user.getString("token")
  )

  fun transfer(amount: String, type: String = "doge", isAll: Int = 0) = PostController(
    context,
    "coin.transfer",
    JSONObject()
      .put("amount", amount)
      .put("type", type)
      .put("all", isAll),
    user.getString("token")
  )

  fun logout() = GetController(context, "logout", user.getString("token"))
}