package net.ijool.controller

import android.content.Context
import net.ijool.controller.web.GetController
import net.ijool.model.User
import okhttp3.FormBody
import net.ijool.controller.doge.PostController as postDoge

class NavigationController {
  private lateinit var user: User

  fun getBalance(context: Context, isBot: Boolean = false): Float {
    user = User(context)

    val body = FormBody.Builder()
    body.addEncoded("s", if (isBot) user.getString("cookie_bot") else user.getString("cookie_doge"))
    body.addEncoded("Currency", "doge")

    val post = postDoge("GetBalance", body).call()
    return if (post.getInt("code") < 400) {
      post.getJSONObject("data").getString("Balance").toFloat()
    } else {
      0F
    }
  }

  fun logout(context: Context) {
    user = User(context)

    GetController("logout", user.getString("token"))
  }
}