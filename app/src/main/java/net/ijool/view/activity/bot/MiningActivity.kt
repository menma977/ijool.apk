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
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.controller.BotController
import net.ijool.controller.NavigationController
import net.ijool.model.User
import net.ijool.view.activity.NavigationActivity
import java.math.BigDecimal

class MiningActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var jobLoadBalance: CompletableJob
  private lateinit var jobMining: CompletableJob
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
    initMining()

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
          Toast.makeText(applicationContext, "Mining size required", Toast.LENGTH_LONG).show()
          amount.requestFocus()
        }
        chance.text.toString().isEmpty() -> {
          Toast.makeText(applicationContext, "Chance required", Toast.LENGTH_LONG).show()
          amount.requestFocus()
        }
        else -> {
          initMining(true)
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
    if (!::jobLoadBalance.isInitialized || jobLoadBalance.isCompleted) {
      jobLoadBalance = Job()
    }
    CoroutineScope(Dispatchers.IO + jobLoadBalance).launch {
      val balanceRaw = NavigationController().getBalance(applicationContext, true).toBigDecimal()
      GlobalScope.launch(Dispatchers.Main) {
        balanceBot.text = "${Coin.decimalToCoin(balanceRaw).toPlainString()} DOGE"
      }
    }
  }

  private fun initMining(isMining: Boolean = false) {
    if (!::jobMining.isInitialized || jobMining.isCompleted) {
      jobMining = Job()
      jobMining.invokeOnCompletion { throwable ->
        throwable?.message.let {
          var message = it
          if (message.isNullOrBlank()) {
            message = "null error"
          }
          GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
          }
        }
      }
    }

    if (isMining) {
      miningButton.text = "Please wait..."
      miningButton.isEnabled = false
      CoroutineScope(Dispatchers.IO + jobMining).launch {
        val post = BotController().mining(applicationContext, amount.text.toString().toBigDecimal(), chance.text.toString().toBigDecimal())
        if (post.getInt("code") < 400) {
          GlobalScope.launch(Dispatchers.Main) {
            val balanceText = "${Coin.decimalToCoin(post.getString("balance").toBigDecimal()).toPlainString()} DOGE"
            balanceBot.text = balanceText
            if (post.getString("profit").toBigDecimal() > BigDecimal(0)) {
              profit.text = "WIN"
              profit.setTextColor(getColor(R.color.Success))
            } else {
              profit.text = "LOSE"
              profit.setTextColor(getColor(R.color.Danger))
            }
            miningButton.text = "Mining"
            miningButton.isEnabled = true
          }
        } else {
          jobMining.cancel(CancellationException(post.getString("message")))
          GlobalScope.launch(Dispatchers.Main) {
            miningButton.text = "Mining"
            miningButton.isEnabled = true
          }
        }
      }
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    jobMining.cancel(CancellationException("Manual has been close"))
    val move = Intent(this, NavigationActivity::class.java)
    startActivity(move)
    finish()
  }
}