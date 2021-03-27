package net.ijool.controller

import android.content.Context
import net.ijool.controller.doge.PostController
import net.ijool.model.User
import okhttp3.FormBody
import org.json.JSONObject
import java.math.BigDecimal

class BotController {
  private lateinit var user: User

  fun ninku(context: Context, amount: BigDecimal, seed: String, high: String = "499999"): JSONObject {
    user = User(context)

    val json = JSONObject()

    val body = FormBody.Builder()
    body.addEncoded("s", user.getString("cookie_bot"))
    body.addEncoded("PayIn", amount.toPlainString())
    body.addEncoded("Low", "0")
    body.addEncoded("High", high)
    body.addEncoded("ClientSeed", seed)
    body.addEncoded("Currency", "doge")
    body.addEncoded("ProtocolVersion", "2")

    val post = PostController("PlaceBet", body).call()
    if (post.getInt("code") < 400) {
      val nextSeed = post.getJSONObject("data").getString("Next")
      val payOut = post.getJSONObject("data").getString("PayOut").toBigDecimal()
      val profit = payOut - amount
      val balance = post.getJSONObject("data").getString("StartingBalance").toBigDecimal()

      json.put("code", post.getInt("code"))
      json.put("balance", (balance + profit).toPlainString())
      json.put("seed", nextSeed)
    } else {
      json.put("code", post.getInt("code"))
      json.put("message", post.getString("data"))
    }

    return json
  }
}