package net.ijool.view.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import net.ijool.MainActivity
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.config.Loading
import net.ijool.controller.DogeController
import net.ijool.controller.UserController
import net.ijool.controller.volley.web.HandleError
import net.ijool.model.User
import net.ijool.service.Auth
import net.ijool.service.Balance
import net.ijool.view.fragment.BotFragment
import net.ijool.view.modal.WalletModal
import org.json.JSONObject

class NavigationActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var jobService: CompletableJob
  private lateinit var dogeBalance: Intent
  private lateinit var webAuth: Intent
  private lateinit var logout: ImageButton
  private lateinit var balance: TextView
  private lateinit var balanceBot: TextView
  private lateinit var name: TextView
  private lateinit var username: TextView
  private lateinit var email: TextView
  private lateinit var depositDoge: ImageButton
  private lateinit var depositBot: ImageButton
  private lateinit var withdrawDoge: ImageButton
  private lateinit var withdrawBot: ImageButton
  private lateinit var frame: FrameLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)

    user = User(this)
    loading = Loading(this)
    loading.openDialog()

    runService()

    logout = findViewById(R.id.imageButtonLogout)
    balance = findViewById(R.id.textViewBalance)
    balanceBot = findViewById(R.id.textViewBalanceBot)
    name = findViewById(R.id.textViewName)
    username = findViewById(R.id.textViewUsername)
    email = findViewById(R.id.textViewEmail)
    depositDoge = findViewById(R.id.imageButtonDepositDoge)
    depositBot = findViewById(R.id.imageButtonDepositBot)
    withdrawDoge = findViewById(R.id.imageButtonWithdrawDoge)
    withdrawBot = findViewById(R.id.imageButtonWithdrawBot)
    frame = findViewById(R.id.contentFragment)

    username.text = user.getString("username")
    email.text = user.getString("email")

    DogeController(applicationContext).balance(user.getString("cookie_doge")).cll({
      val response = JSONObject(it)
      val textBalance = "${Coin.decimalToCoin(response.getString("Balance").toBigDecimal()).toPlainString()} DOGE"
      balance.text = textBalance
    }, {
      balance.text = "0 DOGE"
    })

    DogeController(applicationContext).balance(user.getString("cookie_bot")).cll({
      val response = JSONObject(it)
      val textBalance = "${Coin.decimalToCoin(response.getString("Balance").toBigDecimal()).toPlainString()} DOGE"
      balanceBot.text = textBalance
    }, {
      balanceBot.text = "0 DOGE"
    })

    logout.setOnClickListener {
      onLogout()
    }

    depositDoge.setOnClickListener {
      WalletModal.show(this, user.getString("wallet_doge"))
    }

    depositBot.setOnClickListener {
      WalletModal.show(this, user.getString("wallet_bot"))
    }

    withdrawDoge.setOnClickListener {
      val move = Intent(this, WithdrawActivity::class.java)
      startActivity(move)
    }

    withdrawBot.setOnClickListener {
      val move = Intent(this, TransferActivity::class.java)
      startActivity(move)
    }

    authCheck()
  }

  private fun subscribed() {
    UserController(applicationContext).subscribed().call({ response ->
      user.setBoolean("subscribe", response.getBoolean("subscribe"))
      if (user.getBoolean("subscribe")) {
        frame.visibility = FrameLayout.VISIBLE
        val fragment = BotFragment()
        addFragment(fragment)
      } else {
        frame.visibility = FrameLayout.GONE
      }
      loading.closeDialog()
    }, { error ->
      val json = HandleError(error).result()
      Toast.makeText(applicationContext, json.getString("message"), Toast.LENGTH_LONG).show()
      loading.closeDialog()
    })
  }

  private fun authCheck() {
    UserController(this).auth().call({ response ->
      user.setBoolean("auth", response.getBoolean("auth"))
      subscribed()
    }, {
      onLogout()
    })
  }

  private fun addFragment(fragment: Fragment) {
    val backStateName = fragment.javaClass.simpleName
    val fragmentManager = supportFragmentManager
    val fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0)

    if (!fragmentPopped && fragmentManager.findFragmentByTag(backStateName) == null) {
      val fragmentTransaction = fragmentManager.beginTransaction()
      fragmentTransaction.replace(R.id.contentFragment, fragment, backStateName)
      fragmentTransaction.addToBackStack(backStateName)
      fragmentTransaction.commit()
    }
  }

  private fun runService() {
    if (!::jobService.isInitialized || jobService.isCompleted) {
      jobService = Job()
    }

    CoroutineScope(IO + jobService).launch {
      dogeBalance = Intent(applicationContext, Balance::class.java)
      webAuth = Intent(applicationContext, Auth::class.java)

      GlobalScope.launch(Main) {
        startService(dogeBalance)
        startService(webAuth)

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastBalance, IntentFilter("doge.balances"))
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastBalanceBot, IntentFilter("doge.balances.bot"))
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastAuth, IntentFilter("web.auth"))
        jobService.cancel()
      }
    }
  }

  private fun removeService() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastBalance)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastBalanceBot)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastAuth)

    if (this::dogeBalance.isInitialized) {
      stopService(dogeBalance)
    }

    if (this::webAuth.isInitialized) {
      stopService(webAuth)
    }
  }

  private var broadcastBalance: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      var balanceText = "0 DOGE"
      try {
        val getIntent = intent!!.extras!!
        balanceText = "${Coin.decimalToCoin(getIntent.getFloat("balance").toBigDecimal()).toPlainString()} DOGE"
        balance.text = balanceText
      } catch (e: Exception) {
        balance.text = balanceText
      }
    }
  }

  private var broadcastBalanceBot: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      var balanceBotText = "0 DOGE"
      try {
        val getIntent = intent!!.extras!!
        balanceBotText = "${Coin.decimalToCoin(getIntent.getFloat("balanceBot").toBigDecimal()).toPlainString()} DOGE"
        balanceBot.text = balanceBotText
      } catch (e: Exception) {
        balanceBot.text = balanceBotText
      }
    }
  }

  private var broadcastAuth: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      try {
        val getIntent = intent!!.extras!!
        if (!getIntent.getBoolean("auth") || !user.getBoolean("auth")) {
          onLogout()
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun onLogout() {
    loading.openDialog()
    UserController(applicationContext).logout().call({}, {})
    user.clear()
    runOnUiThread {
      val move = Intent(applicationContext, MainActivity::class.java)
      loading.closeDialog()
      finish()
      startActivity(move)
    }
  }

  override fun onStart() {
    super.onStart()
    runService()
  }

  override fun onResume() {
    super.onResume()
    runService()
  }

  override fun onStop() {
    super.onStop()
    removeService()
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount == 1) {
      removeService()

      finishAffinity()
    } else {
      super.onBackPressed()
    }
  }

  override fun onPause() {
    super.onPause()
    removeService()
  }
}