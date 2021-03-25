package net.ijool.controller

import android.content.Context
import net.ijool.controller.web.GetController
import net.ijool.controller.web.PostController
import net.ijool.model.User
import okhttp3.FormBody
import org.json.JSONObject
import net.ijool.controller.volley.web.PostController as VolleyPostController

class UserController {
  private lateinit var user: User

  fun login(context: Context, username: String, password: String) = VolleyPostController(
    context,
    "login",
    JSONObject()
      .put("username", username)
      .put("password", password)
  )

  fun auth(context: Context): JSONObject {
    user = User(context)

    val json = JSONObject()

    val get = GetController("check", user.getString("token")).call()
    println(get)
    when {
      get.getBoolean("logout") -> {
        json.put("code", get.getInt("code"))
        json.put("auth", false)
      }
      get.getInt("code") < 400 -> {
        json.put("code", get.getInt("code"))
        json.put("auth", get.getJSONObject("data").getBoolean("auth"))
      }
      else -> {
        json.put("code", get.getInt("code"))
        json.put("auth", false)
      }
    }

    return json
  }

  fun subscribed(context: Context): JSONObject {
    user = User(context)

    val json = JSONObject()

    val get = GetController("subscribe", user.getString("token")).call()

    json.put("code", get.getInt("code"))
    if (get.getInt("code") < 400) {
      json.put("subscribe", get.getJSONObject("data").getBoolean("subscribe"))
    } else {
      json.put("subscribe", false)
      json.put("message", get.getString("data"))
    }

    return json
  }

  fun withdraw(context: Context, amount: String, wallet: String): JSONObject {
    user = User(context)

    val json = JSONObject()

    val body = FormBody.Builder()
    body.addEncoded("amount", amount)
    body.addEncoded("wallet", wallet)

    val post = PostController("coin.withdraw", user.getString("token"), body).call()

    if (post.getInt("code") < 200) {
      json.put("code", post.getInt("code"))
      json.put("message", post.getString("data"))
    } else {
      json.put("code", post.getInt("code"))
      json.put("message", post.getString("data"))
    }

    return json
  }

  fun transfer(context: Context, amount: String, type: String = "doge", isAll: Int = 0): JSONObject {
    user = User(context)

    val json = JSONObject()

    val body = FormBody.Builder()
    body.addEncoded("amount", amount)
    body.addEncoded("type", type)
    body.addEncoded("all", isAll.toString())

    val post = PostController("coin.transfer", user.getString("token"), body).call()

    if (post.getInt("code") < 200) {
      json.put("code", post.getInt("code"))
      json.put("message", post.getString("data"))
    } else {
      json.put("code", post.getInt("code"))
      json.put("message", post.getString("data"))
    }

    return json
  }
}