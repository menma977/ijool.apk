package net.ijool.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.config.Loading
import net.ijool.controller.NavigationController
import net.ijool.controller.UserController
import net.ijool.model.User
import java.util.*
import kotlin.concurrent.schedule

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

    Timer().schedule(100) {
      val textBalance = "${Coin.decimalToCoin(NavigationController().getBalance(applicationContext).toBigDecimal()).toPlainString()} DOGE"
      val textBalanceBot = "${Coin.decimalToCoin(NavigationController().getBalance(applicationContext, true).toBigDecimal()).toPlainString()} DOGE"
      runOnUiThread {
        balanceDoge.text = textBalance
        balanceBot.text = textBalanceBot
        loading.closeDialog()
      }
    }

    buttonSendDoge.setOnClickListener {
      loading.openDialog()
      Timer().schedule(100) {
        val post = UserController().transfer(applicationContext, amountDoge.text.toString(), "bot")
        if (post.getInt("code") < 400) {
          runOnUiThread {
            Toast.makeText(applicationContext, post.getString("message"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
            finish()
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, post.getString("message"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
          }
        }
      }
    }

    buttonSendDogeAll.setOnClickListener {
      loading.openDialog()
      Timer().schedule(100) {
        val post = UserController().transfer(applicationContext, "0", "bot", 1)
        if (post.getInt("code") < 400) {
          runOnUiThread {
            Toast.makeText(applicationContext, post.getString("message"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
            finish()
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, post.getString("message"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
          }
        }
      }
    }

    buttonSendBot.setOnClickListener {
      loading.openDialog()
      Timer().schedule(100) {
        val post = UserController().transfer(applicationContext, amountBot.text.toString(), "doge")
        if (post.getInt("code") < 400) {
          runOnUiThread {
            Toast.makeText(applicationContext, post.getString("message"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
            finish()
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, post.getString("message"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
          }
        }
      }
    }

    buttonSendBotAll.setOnClickListener {
      loading.openDialog()
      Timer().schedule(100) {
        val post = UserController().transfer(applicationContext, "0", "doge", 1)
        if (post.getInt("code") < 400) {
          runOnUiThread {
            Toast.makeText(applicationContext, post.getString("message"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
            finish()
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, post.getString("message"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
          }
        }
      }
    }
  }
}