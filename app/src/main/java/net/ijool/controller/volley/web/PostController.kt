package net.ijool.controller.volley.web

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import net.ijool.model.Url
import org.json.JSONObject

class PostController(private var context: Context, private var targetUrl: String, private var body: JSONObject, private var token: String? = null) {
  constructor(context: Context, targetUrl: String, body: JSONObject) : this(context, targetUrl, body, null)

  fun call(response: Response.Listener<JSONObject>, error: Response.ErrorListener) {
    val request = Volley.newRequestQueue(context)
    val jsonRequest = object : JsonObjectRequest(Method.POST, Url.web(targetUrl), body, response, error) {
      override fun getHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
        if (!token.isNullOrEmpty()) {
          headers["Authorization"] = "Bearer ${token!!}"
        }
        headers["X-Requested-With"] = "XMLHttpRequest"
        headers["Content-Type"] = "application/json; charset=utf-8"
        return headers
      }
    }

    request.add(jsonRequest)
  }
}