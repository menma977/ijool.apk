package net.ijool.controller.volley.web

import com.android.volley.VolleyError
import org.json.JSONObject
import java.nio.charset.Charset

class HandleError(private val error: VolleyError) {
  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  fun result(): JSONObject {
    val raw = String(error.networkResponse.data, Charset.forName("utf-8"))
    val jsonMessage: JSONObject
    if (raw.isNotEmpty()) {
      jsonMessage = JSONObject(raw)
    } else {
      jsonMessage = JSONObject().put("message", "something when wrong")
    }
    return when (error.networkResponse.statusCode) {
      401 -> {
        JSONObject().put("message", jsonMessage.getString("message")).put("logout", true)
      }
      else -> {
        JSONObject().put("message", jsonMessage.getString("message")).put("logout", false)
      }
    }
  }
}