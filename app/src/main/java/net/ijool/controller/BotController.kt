package net.ijool.controller

import android.content.Context
import net.ijool.config.Coin
import net.ijool.controller.doge.PostController
import net.ijool.model.User
import okhttp3.FormBody
import org.json.JSONObject
import java.math.BigDecimal

class BotController {
  private lateinit var user: User

  fun mining(context: Context, amount: BigDecimal, high: BigDecimal): JSONObject {
    user = User(context)

    val json = JSONObject()
    val payIn = Coin.coinToDecimal(amount)

    val body = FormBody.Builder()
    body.addEncoded("s", user.getString("cookie_bot"))
    body.addEncoded("PayIn", payIn.toPlainString())
    body.addEncoded("Low", "0")
    body.addEncoded("High", (high * BigDecimal(10000)).toInt().toString())
    body.addEncoded("ClientSeed", (0..100000).random().toString())
    body.addEncoded("Currency", "doge")
    body.addEncoded("ProtocolVersion", "2")

    val post = PostController("PlaceBet", body).call()
    if (post.getInt("code") < 400) {
      val payOut = post.getJSONObject("data").getString("PayOut").toBigDecimal()
      val profit = payOut - payIn
      val balance = post.getJSONObject("data").getString("StartingBalance").toBigDecimal()

      json.put("code", post.getInt("code"))
      json.put("profit", profit.toPlainString())
      json.put("balance", (balance + profit).toPlainString())
    } else {
      json.put("code", post.getInt("code"))
      json.put("message", post.getString("data"))
    }

    return json
  }

  fun tsunami(context: Context, amount: BigDecimal, seed: String): JSONObject {
    user = User(context)

    val payIn: String
    val json = JSONObject()
    val coinAmount = Coin.decimalToCoin(amount)
    payIn = if (coinAmount < BigDecimal(10)) {
      "100"
    } else if (coinAmount >= BigDecimal(10) && coinAmount < BigDecimal(100)) {
      "1000"
    } else if (coinAmount >= BigDecimal(100) && coinAmount < BigDecimal(10000)) {
      "10000"
    } else if (coinAmount >= BigDecimal(1000) && coinAmount < BigDecimal(10000)) {
      "100000"
    } else {
      "1000000"
    }

    val body = FormBody.Builder()
    body.addEncoded("s", user.getString("cookie_bot"))
    body.addEncoded("BasePayIn", "10000")
    body.addEncoded("High", "499999")
    body.addEncoded("Low", "0")
    body.addEncoded("MaxBets", "200")
    body.addEncoded("ResetOnWin", "1")
    body.addEncoded("ResetOnLose", "0")
    body.addEncoded("IncreaseOnWinPercent", "0")
    body.addEncoded("IncreaseOnLosePercent", "1")
    body.addEncoded("MaxPayIn", "0")
    body.addEncoded("ResetOnLoseMaxBet", "0")
    body.addEncoded("StopOnLoseMaxBet", "0")
    body.addEncoded("StopMaxBalance", "0")
    body.addEncoded("StopMinBalance", "0")
    body.addEncoded("StartingPayIn", payIn)
    body.addEncoded("Compact", "1")
    body.addEncoded("ClientSeed", seed)
    body.addEncoded("Currency", "doge")
    body.addEncoded("ProtocolVersion", "2")

    val post = PostController("PlaceAutomatedBets", body).call()
    if (post.getInt("code") < 400) {
      val nextSeed = post.getJSONObject("data").getString("Next")
      val payInPost = post.getJSONObject("data").getString("PayIn").toBigDecimal()
      val payOut = post.getJSONObject("data").getString("PayOut").toBigDecimal()
      val profit = payOut + payInPost
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