package net.ijool.controller.volley.doge

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import net.ijool.model.Url
import org.json.JSONObject

class PostController(private var context: Context, private var action: String, private var body: JSONObject, private var withKey: Boolean = false) {
  fun cll(response: Response.Listener<String>, error: Response.ErrorListener) {
    body.put("a", action)
    if (withKey) {
      body.put("key", Url.key())
    }
    val bodyToMap = body
    val request = Volley.newRequestQueue(context)
    val jsonRequest = object : StringRequest(Method.POST, Url.doge(), response, error) {
      override fun getHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
        headers["Content-type"] = "application/x-www-form-urlencoded; charset=UTF-8"
        return headers
      }

      override fun getParams(): MutableMap<String, String> {
        val rawBody = HashMap<String, String>()
        for (i in 0 until bodyToMap.names()!!.length()) {
          rawBody[bodyToMap.names()!!.getString(i)] = bodyToMap.getString(bodyToMap.names()!!.getString(i))
        }
        return rawBody
      }
    }

    request.add(jsonRequest)
  }
}