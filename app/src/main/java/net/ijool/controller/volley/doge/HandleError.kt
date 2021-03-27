package net.ijool.controller.volley.doge

import com.android.volley.VolleyError
import org.json.JSONObject
import java.nio.charset.Charset

class HandleError(private val error: VolleyError) {
  fun result(): JSONObject {
    val raw = String(error.networkResponse.data, Charset.forName("utf-8"))
    val jsonMessage: JSONObject = if (raw.isNotEmpty()) {
      JSONObject(raw)
    } else {
      JSONObject().put("message", "something when wrong")
    }
    return when {
      jsonMessage.toString().contains("ChanceTooHigh") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "Chance Too High")
      }
      jsonMessage.toString().contains("ChanceTooLow") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "Chance Too Low")
      }
      jsonMessage.toString().contains("InsufficientFunds") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "Insufficient Funds")
      }
      jsonMessage.toString().contains("NoPossibleProfit") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "No Possible Profit")
      }
      jsonMessage.toString().contains("MaxPayoutExceeded") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "Max Payout Exceeded")
      }
      jsonMessage.toString().contains("999doge") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "Invalid request On Server Wait 5 minute to try again")
      }
      jsonMessage.toString().contains("error") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "Invalid request")
      }
      jsonMessage.toString().contains("TooFast") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "Too Fast")
      }
      jsonMessage.toString().contains("TooSmall") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "Too Small")
      }
      jsonMessage.toString().contains("LoginRequired") -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", "Login Required")
      }
      else -> {
        JSONObject().put("code", error.networkResponse.statusCode).put("message", jsonMessage)
      }
    }
  }
}