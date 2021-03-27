package net.ijool.view.activity

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.config.Loading
import net.ijool.controller.DogeController
import net.ijool.controller.UserController
import net.ijool.controller.volley.web.HandleError
import net.ijool.controller.volley.web.HandleResponse
import net.ijool.model.User
import org.json.JSONObject

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

    DogeController(this).balance(user.getString("cookie_doge")).cll({
      val response = JSONObject(it)
      val textBalance = "${Coin.decimalToCoin(response.getString("Balance").toBigDecimal()).toPlainString()} DOGE"
      GlobalScope.launch(Main) {
        balance.text = textBalance
        loading.closeDialog()
      }
    }, {
      GlobalScope.launch(Main) {
        balance.text = "0 DOGE"
        loading.closeDialog()
      }
    })
  }

  private fun sendBalance() {
    loading.openDialog()
    var json: JSONObject
    UserController(applicationContext).withdraw(balanceText.text.toString(), walletText.text.toString()).call({ response ->
      json = HandleResponse(response).result()
      Toast.makeText(applicationContext, json.getString("data"), Toast.LENGTH_LONG).show()
      loading.closeParent()
    }, { error ->
      json = HandleError(error).result()
      Toast.makeText(applicationContext, json.getString("message"), Toast.LENGTH_LONG).show()
      loading.closeDialog()
    })
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