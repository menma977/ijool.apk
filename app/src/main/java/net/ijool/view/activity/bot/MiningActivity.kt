package net.ijool.view.activity.bot

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.controller.DogeController
import net.ijool.controller.volley.doge.HandleError
import net.ijool.model.User
import net.ijool.view.activity.NavigationActivity
import org.json.JSONObject
import java.math.BigDecimal

class MiningActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var balanceBot: TextView
  private lateinit var profit: TextView
  private lateinit var defaultAmount: EditText
  private lateinit var amount: EditText
  private lateinit var chance: EditText
  private lateinit var miningButton: Button
  private lateinit var amountResetButton: Button
  private lateinit var amountDoubleButton: Button
  private lateinit var amountHalfButton: Button
  private lateinit var chanceResetButton: Button
  private lateinit var chanceDoubleButton: Button
  private lateinit var chanceHalfButton: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_mining)

    user = User(this)

    balanceBot = findViewById(R.id.textViewBalanceBot)
    profit = findViewById(R.id.textViewProfit)
    defaultAmount = findViewById(R.id.editTextDefaultMining)
    amount = findViewById(R.id.editTextMining)
    chance = findViewById(R.id.editTextChance)
    miningButton = findViewById(R.id.buttonMining)
    amountResetButton = findViewById(R.id.buttonResetMining)
    amountDoubleButton = findViewById(R.id.buttonDoubleMining)
    amountHalfButton = findViewById(R.id.buttonHalfMining)
    chanceResetButton = findViewById(R.id.buttonResetChance)
    chanceDoubleButton = findViewById(R.id.buttonDoubleChance)
    chanceHalfButton = findViewById(R.id.buttonHalfChance)

    defaultAmount.setText("0.01")
    amount.setText("0.01")
    chance.setText("49.99")

    initBalance()

    defaultAmount.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable) {
        amount.setText(s.toString())
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })

    miningButton.setOnClickListener {
      when {
        amount.text.toString().isEmpty() -> {
          Toast.makeText(applicationContext, "Wager size required", Toast.LENGTH_LONG).show()
          amount.requestFocus()
        }
        chance.text.toString().isEmpty() -> {
          Toast.makeText(applicationContext, "Chance required", Toast.LENGTH_LONG).show()
          amount.requestFocus()
        }
        else -> {
          initMining()
        }
      }
    }

    amountResetButton.setOnClickListener {
      amount.setText(defaultAmount.text.toString())
    }

    amountDoubleButton.setOnClickListener {
      val amountValue = amount.text.toString().toBigDecimal()
      amount.setText((amountValue * BigDecimal(2)).toPlainString())
    }

    amountHalfButton.setOnClickListener {
      val amountValue = amount.text.toString().toBigDecimal()
      amount.setText(Coin.decimalToCoin((Coin.coinToDecimal(amountValue) / BigDecimal(2))).toPlainString())
    }

    chanceResetButton.setOnClickListener {
      chance.setText("49.99")
    }

    chanceDoubleButton.setOnClickListener {
      val amountValue = chance.text.toString().toBigDecimal()
      var result = (amountValue * BigDecimal(2))
      if (result > BigDecimal(99)) {
        result = BigDecimal(99.99).setScale(2, BigDecimal.ROUND_HALF_DOWN)
      }
      chance.setText(result.toPlainString())
    }

    chanceHalfButton.setOnClickListener {
      val amountValue = chance.text.toString().toBigDecimal()
      var result = (amountValue / BigDecimal(2))
      if (result < BigDecimal(5)) {
        result = BigDecimal(5)
      }
      chance.setText(result.toPlainString())
    }
  }

  private fun initBalance() {
    DogeController(this).balance(user.getString("cookie_bot")).cll({
      val response = JSONObject(it)
      val textBalance = "${Coin.decimalToCoin(response.getString("Balance").toBigDecimal()).toPlainString()} DOGE"
      GlobalScope.launch(Main) {
        balanceBot.text = textBalance
      }
    }, {
      GlobalScope.launch(Main) {
        balanceBot.text = "0 DOGE"
      }
    })
  }

  private fun initMining() {
    miningButton.text = "Please wait..."
    miningButton.isEnabled = false
    DogeController(this).wager(user.getString("cookie_bot"), amount.text.toString().toBigDecimal(), chance.text.toString().toFloat(), (100000..999999).random().toString()).cll({
      val response = JSONObject(it)
      val payOut = response.getString("PayOut").toBigDecimal()
      val resultProfit = payOut - Coin.coinToDecimal(amount.text.toString().toBigDecimal())
      val balanceText = "${Coin.decimalToCoin(response.getString("StartingBalance").toBigDecimal()).toPlainString()} DOGE"
      balanceBot.text = balanceText
      if (resultProfit > BigDecimal(0)) {
        profit.text = "WIN"
        profit.setTextColor(getColor(R.color.Success))
      } else {
        profit.text = "LOSE"
        profit.setTextColor(getColor(R.color.Danger))
      }
      miningButton.text = "Wager"
      miningButton.isEnabled = true
    }, { error ->
      Toast.makeText(this, HandleError(error).result().getString("message"), Toast.LENGTH_SHORT).show()
      miningButton.text = "Wager"
      miningButton.isEnabled = true
    })
  }

  override fun onBackPressed() {
    super.onBackPressed()
    val move = Intent(this, NavigationActivity::class.java)
    startActivity(move)
    finish()
  }
}