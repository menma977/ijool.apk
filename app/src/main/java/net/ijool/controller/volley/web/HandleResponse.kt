package net.ijool.controller.volley.web

import org.json.JSONObject

class HandleResponse(private val json: JSONObject) {
  fun result(): JSONObject {
    return when {
      json.has("message") -> {
        JSONObject().put("data", json.getString("message")).put("logout", false)
      }
      else -> {
        JSONObject().put("data", json).put("logout", false)
      }
    }
  }
}