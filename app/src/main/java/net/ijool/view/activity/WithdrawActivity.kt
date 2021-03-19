package net.ijool.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.config.Loading
import net.ijool.controller.NavigationController
import net.ijool.controller.UserController
import net.ijool.model.User
import java.util.*
import kotlin.concurrent.schedule

class WithdrawActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var frameScanner: FrameLayout
  private lateinit var scannerEngine: ZXingScannerView
  private lateinit var wallet: String
  private lateinit var title: TextView
  private lateinit var balance: TextView
  private lateinit var walletText: EditText
  private lateinit var balanceText: EditText
  private lateinit var send: Button
  private var isHasCode = false
  private var isStart = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_withdraw)

    user = User(this)
    loading = Loading(this)
    loading.openDialog()

    title = findViewById(R.id.textViewTitle)
    balance = findViewById(R.id.textViewBalance)
    walletText = findViewById(R.id.editTextWallet)
    frameScanner = findViewById(R.id.frameLayoutScanner)
    balanceText = findViewById(R.id.editTextBalance)
    send = findViewById(R.id.buttonSend)

    title.text = "Send Doge from deposit"

    initScannerView()

    frameScanner.setOnClickListener {
      if (isStart) {
        scannerEngine.startCamera()
        isStart = false
      }
    }

    send.setOnClickListener {
      sendBalance()
    }

    Timer().schedule(100) {
      val textBalance = "${Coin.decimalToCoin(NavigationController().getBalance(applicationContext).toBigDecimal()).toPlainString()} DOGE"
      runOnUiThread {
        balance.text = textBalance
        loading.closeDialog()
      }
    }
  }

  private fun sendBalance() {
    loading.openDialog()
    Timer().schedule(100) {
      val post = UserController().withdraw(applicationContext, balanceText.text.toString(), walletText.text.toString())
      runOnUiThread {
        Toast.makeText(applicationContext, post.getString("message"), Toast.LENGTH_LONG).show()
        loading.closeDialog()
        finish()
      }
    }
  }

  override fun handleResult(rawResult: Result?) {
    if (rawResult?.text?.isNotEmpty()!!) {
      isHasCode = true
      wallet = rawResult.text.toString()
      walletText.setText(wallet)
    } else {
      isHasCode = false
    }
  }

  private fun initScannerView() {
    scannerEngine = ZXingScannerView(this)
    scannerEngine.setAutoFocus(true)
    scannerEngine.setResultHandler(this)
    frameScanner.addView(scannerEngine)
  }

  override fun onPause() {
    scannerEngine.stopCamera()
    super.onPause()
  }
}