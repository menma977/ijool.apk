package net.ijool.controller.doge

import net.ijool.controller.Render
import net.ijool.model.Url
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class PostController(private var action: String, private var body: FormBody.Builder, private var withKey: Boolean = false) : Callable<JSONObject> {
  override fun call(): JSONObject {
    return try {
      val client = OkHttpClient.Builder()
      client.connectTimeout(30, TimeUnit.SECONDS)
      client.writeTimeout(30, TimeUnit.SECONDS)
      client.readTimeout(30, TimeUnit.SECONDS)
      val request = Request.Builder()
      request.url(Url.doge())
      request.addHeader("Access-Control-Allow-Origin", "*")
      request.addHeader("X-Requested-With", "XMLHttpRequest")
      request.addHeader("Connection", "close")
      request.header("Connection", "close")
      body.addEncoded("a", action)
      if (withKey) {
        body.addEncoded("key", Url.key())
      }
      request.post(body.build())
      val response: Response = client.build().newCall(request.build()).execute()
      val raw = Render(response).result()
      val convertJSON = JSONObject(raw)
      return responseHandler(response.code, convertJSON)
    } catch (e: Exception) {
      e.printStackTrace()
      JSONObject().put("code", 500).put("data", e.message.toString().replace("999doge", "WEB"))
    }
  }

  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  private fun responseHandler(code: Int, json: JSONObject): JSONObject {
    return when {
      json.toString().contains("ChanceTooHigh") -> {
        JSONObject().put("code", 500).put("data", "Chance Too High")
      }
      json.toString().contains("ChanceTooLow") -> {
        JSONObject().put("code", 500).put("data", "Chance Too Low")
      }
      json.toString().contains("InsufficientFunds") -> {
        JSONObject().put("code", 408).put("data", "Insufficient Funds")
      }
      json.toString().contains("NoPossibleProfit") -> {
        JSONObject().put("code", 500).put("data", "No Possible Profit")
      }
      json.toString().contains("MaxPayoutExceeded") -> {
        JSONObject().put("code", 500).put("data", "Max Payout Exceeded")
      }
      json.toString().contains("999doge") -> {
        JSONObject().put("code", 500).put("data", "Invalid request On Server Wait 5 minute to try again")
      }
      json.toString().contains("error") -> {
        JSONObject().put("code", 500).put("data", "Invalid request")
      }
      json.toString().contains("TooFast") -> {
        JSONObject().put("code", 500).put("data", "Too Fast")
      }
      json.toString().contains("TooSmall") -> {
        JSONObject().put("code", 500).put("data", "Too Small")
      }
      json.toString().contains("LoginRequired") -> {
        JSONObject().put("code", 500).put("data", "Login Required")
      }
      else -> {
        JSONObject().put("code", code).put("data", json)
      }
    }
  }
}