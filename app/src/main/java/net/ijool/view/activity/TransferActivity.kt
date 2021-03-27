package net.ijool.view.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.config.Loading
import net.ijool.controller.DogeController
import net.ijool.controller.UserController
import net.ijool.controller.volley.web.HandleError
import net.ijool.controller.volley.web.HandleResponse
import net.ijool.model.User
import org.json.JSONObject

class TransferActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var balanceDoge: TextView
  private lateinit var amountDoge: EditText
  private lateinit var buttonSendDoge: Button
  private lateinit var buttonSendDogeAll: Button
  private lateinit var balanceBot: TextView
  private lateinit var amountBot: EditText
  private lateinit var buttonSendBot: Button
  private lateinit var buttonSendBotAll: Button
  private lateinit var json: JSONObject

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_transfer)

    user = User(this)
    loading = Loading(this)
    loading.openDialog()

    balanceDoge = findViewById(R.id.textViewBalanceDoge)
    amountDoge = findViewById(R.id.editTextAmountDoge)
    buttonSendDoge = findViewById(R.id.buttonSendDoge)
    buttonSendDogeAll = findViewById(R.id.buttonSendDogeAll)
    balanceBot = findViewById(R.id.textViewBalanceBot)
    amountBot = findViewById(R.id.editTextAmountBot)
    buttonSendBot = findViewById(R.id.buttonSendBot)
    buttonSendBotAll = findViewById(R.id.buttonSendBotAll)

    DogeController(this).balance(user.getString("cookie_doge")).cll({
      val response = JSONObject(it)
      val textBalance = "${Coin.decimalToCoin(response.getString("Balance").toBigDecimal()).toPlainString()} DOGE"
      GlobalScope.launch(Main) {
        balanceDoge.text = textBalance
        loading.closeDialog()
      }
    }, {
      GlobalScope.launch(Main) {
        balanceDoge.text = "0 DOGE"
        loading.closeDialog()
      }
    })

    DogeController(this).balance(user.getString("cookie_bot")).cll({
      val response = JSONObject(it)
      val textBalance = "${Coin.decimalToCoin(response.getString("Balance").toBigDecimal()).toPlainString()} DOGE"
      GlobalScope.launch(Main) {
        balanceBot.text = textBalance
        loading.closeDialog()
      }
    }, {
      GlobalScope.launch(Main) {
        balanceBot.text = "0 DOGE"
        loading.closeDialog()
      }
    })

    buttonSendDoge.setOnClickListener {
      loading.openDialog()
      UserController(this).transfer(amountDoge.text.toString(), "bot").call({ response ->
        json = HandleResponse(response).result()
        Toast.makeText(this, json.getString("data"), Toast.LENGTH_LONG).show()
        loading.closeParent()
      }, { error ->
        json = HandleError(error).result()
        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()
        loading.closeDialog()
      })
    }

    buttonSendDogeAll.setOnClickListener {
      loading.openDialog()
      UserController(this).transfer("0", "bot", 1).call({ response ->
        json = HandleResponse(response).result()
        Toast.makeText(this, json.getString("data"), Toast.LENGTH_LONG).show()
        loading.closeParent()
      }, { error ->
        json = HandleError(error).result()
        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()
        loading.closeDialog()
      })
    }

    buttonSendBot.setOnClickListener {
      loading.openDialog()
      UserController(this).transfer(amountBot.text.toString(), "doge").call({ response ->
        json = HandleResponse(response).result()
        Toast.makeText(this, json.getString("data"), Toast.LENGTH_LONG).show()
        loading.closeParent()
      }, { error ->
        json = HandleError(error).result()
        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()
        loading.closeDialog()
      })
    }

    buttonSendBotAll.setOnClickListener {
      loading.openDialog()
      UserController(this).transfer("0", "doge", 1).call({ response ->
        json = HandleResponse(response).result()
        Toast.makeText(this, json.getString("data"), Toast.LENGTH_LONG).show()
        loading.closeParent()
      }, { error ->
        json = HandleError(error).result()
        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()
        loading.closeDialog()
      })
    }
  }
}