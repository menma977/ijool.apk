package net.ijool.controller.web

import okhttp3.Response
import org.json.JSONObject

class Handler(private var response: Response, private var json: JSONObject) {
  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  fun result(): JSONObject {
    if (response.isSuccessful) {
      return when {
        json.toString().contains("message") -> {
          JSONObject().put("code", response.code).put("data", json.getString("message")).put("logout", false)
        }
        else -> {
          JSONObject().put("code", response.code).put("data", json).put("logout", false)
        }
      }
    } else {
      return when {
        json.toString().contains("Unauthenticated") -> {
          JSONObject().put("code", response.code).put("data", json.getString("message")).put("logout", true)
        }
        json.toString().contains("errors") -> {
          JSONObject().put("code", response.code).put("data", json.getJSONObject("errors").getJSONArray(json.getJSONObject("errors").names()[0].toString())[0]).put("logout", false)
        }
        json.toString().contains("message") -> {
          JSONObject().put("code", response.code).put("data", json.getString("message")).put("logout", false)
        }
        else -> {
          JSONObject().put("code", response.code).put("data", json).put("logout", false)
        }
      }
    }
  }
}