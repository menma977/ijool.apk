package net.ijool.controller.volley.doge

import org.json.JSONObject

class HandleResponse(private val json: JSONObject) {
  fun result(): JSONObject {
    return when {
      json.toString().contains("ChanceTooHigh") -> {
        JSONObject().put("code", 400).put("data", "Chance Too High")
      }
      json.toString().contains("ChanceTooLow") -> {
        JSONObject().put("code", 400).put("data", "Chance Too Low")
      }
      json.toString().contains("InsufficientFunds") -> {
        JSONObject().put("code", 408).put("data", "Insufficient Funds")
      }
      json.toString().contains("NoPossibleProfit") -> {
        JSONObject().put("code", 400).put("data", "No Possible Profit")
      }
      json.toString().contains("MaxPayoutExceeded") -> {
        JSONObject().put("code", 400).put("data", "Max Payout Exceeded")
      }
      json.toString().contains("999doge") -> {
        JSONObject().put("code", 400).put("data", "Invalid request On Server Wait 5 minute to try again")
      }
      json.toString().contains("error") -> {
        JSONObject().put("code", 400).put("data", "Invalid request")
      }
      json.toString().contains("TooFast") -> {
        JSONObject().put("code", 400).put("data", "Too Fast")
      }
      json.toString().contains("TooSmall") -> {
        JSONObject().put("code", 400).put("data", "Too Small")
      }
      json.toString().contains("LoginRequired") -> {
        JSONObject().put("code", 400).put("data", "Login Required")
      }
      else -> {
        JSONObject().put("code", 200).put("data", json)
      }
    }
  }
}