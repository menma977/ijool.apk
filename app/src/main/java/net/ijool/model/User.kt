package net.ijool.model

import android.content.Context
import android.content.SharedPreferences

class User(context: Context) {
  companion object {
    private const val preferenceName = "user"
  }

  private val sharedPreferences: SharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
  private val sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()

  fun setInteger(id: String, value: Int) {
    sharedPreferencesEditor.putInt(id, value)
    sharedPreferencesEditor.commit()
  }

  fun setString(id: String, value: String) {
    sharedPreferencesEditor.putString(id, value)
    sharedPreferencesEditor.commit()
  }

  fun setBoolean(id: String, value: Boolean) {
    sharedPreferencesEditor.putBoolean(id, value)
    sharedPreferencesEditor.commit()
  }

  fun setLong(id: String, value: Long) {
    sharedPreferencesEditor.putLong(id, value)
    sharedPreferencesEditor.commit()
  }

  fun getInteger(id: String): Int {
    return sharedPreferences.getInt(id, 0)
  }

  fun getString(id: String): String {
    return sharedPreferences.getString(id, "")!!
  }

  fun getBoolean(id: String): Boolean {
    return sharedPreferences.getBoolean(id, false)
  }

  fun getLong(id: String): Long {
    return sharedPreferences.getLong(id, 0)
  }

  fun clear() {
    sharedPreferencesEditor.clear()
    sharedPreferences.edit().clear().apply()
  }
}