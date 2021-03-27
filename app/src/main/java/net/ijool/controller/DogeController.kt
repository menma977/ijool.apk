package net.ijool.controller

import android.content.Context
import net.ijool.config.Coin
import net.ijool.controller.volley.doge.PostController
import org.json.JSONObject
import java.math.BigDecimal

class DogeController(private val context: Context) {

  fun balance(cookie: String) = PostController(
    context,
    "GetBalance",
    JSONObject()
      .put("s", cookie)
      .put("Currency", "doge")
  )

  fun wager(cookie: String, payIn: BigDecimal, high: Float, seed: String) = PostController(
    context,
    "PlaceBet",
    JSONObject()
      .put("s", cookie)
      .put("PayIn", Coin.coinToDecimal(payIn).toPlainString())
      .put("Low", "0").put("High", (high * 10000).toInt().toString())
      .put("ClientSeed", seed)
      .put("Currency", "doge")
      .put("ProtocolVersion", "2")
  )

  fun tsunami(cookie: String, payIn: BigDecimal, seed: String) = PostController(
    context,
    "PlaceAutomatedBets",
    JSONObject()
      .put("s", cookie)
      .put("BasePayIn", tsunamiPayIn(payIn))
      .put("High", "499999")
      .put("Low", "0")
      .put("MaxBets", "16")
      .put("ResetOnWin", "1")
      .put("ResetOnLose", "0")
      .put("IncreaseOnWinPercent", "0")
      .put("IncreaseOnLosePercent", "1")
      .put("MaxPayIn", "0")
      .put("ResetOnLoseMaxBet", "0")
      .put("StopOnLoseMaxBet", "0")
      .put("StopMaxBalance", "0")
      .put("StopMinBalance", "0")
      .put("StartingPayIn", "0")
      .put("Compact", "1")
      .put("ClientSeed", seed)
      .put("Currency", "doge")
      .put("ProtocolVersion", "2")
  )

  private fun tsunamiPayIn(payIn: BigDecimal): String {
    val coinAmount = Coin.decimalToCoin(payIn)
    return if (coinAmount < BigDecimal(100)) {
      "100000"
    } else if (coinAmount >= BigDecimal(100) && coinAmount < BigDecimal(1000)) {
      "1000000"
    } else {
      "10000000"
    }
  }

  fun ninku(cookie: String, payIn: BigDecimal, seed: String, high: String = "499999") = PostController(
    context,
    "PlaceAutomatedBets",
    JSONObject()
      .put("s", cookie)
      .put("BasePayIn", ninkuPayIn(payIn))
      .put("High", high)
      .put("Low", "0")
      .put("MaxBets", "8")
      .put("ResetOnWin", "1")
      .put("ResetOnLose", "0")
      .put("IncreaseOnWinPercent", "0")
      .put("IncreaseOnLosePercent", "1")
      .put("MaxPayIn", "0")
      .put("ResetOnLoseMaxBet", "0")
      .put("StopOnLoseMaxBet", "0")
      .put("StopMaxBalance", "0")
      .put("StopMinBalance", "0")
      .put("StartingPayIn", "0")
      .put("Compact", "1")
      .put("ClientSeed", seed)
      .put("Currency", "doge")
      .put("ProtocolVersion", "2")
  )

  private fun ninkuPayIn(payIn: BigDecimal): String {
    val coinAmount = Coin.decimalToCoin(payIn)
    return if (coinAmount < BigDecimal(1000)) {
      "10000000"
    } else {
      "100000000"
    }
  }

  fun mosee(cookie: String, payIn: BigDecimal, seed: String, high: String = "940000") = PostController(
    context,
    "PlaceBet",
    JSONObject()
      .put("s", cookie)
      .put("PayIn", payIn.toPlainString())
      .put("Low", "0")
      .put("High", high)
      .put("ClientSeed", seed)
      .put("Currency", "doge")
      .put("ProtocolVersion", "2")
  )
}