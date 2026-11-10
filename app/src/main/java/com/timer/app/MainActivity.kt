package com.timer.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.timer.app.data.repository.CouponRepository
import com.timer.app.data.repository.CouponRedeemResult
import com.timer.app.presentation.navigation.NavGraph
import com.timer.app.presentation.theme.TimerTheme
import com.timer.app.service.UsageTrackingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var couponRepository: CouponRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTrackingService()
        handleDeepLinkIntent(intent)
        setContent {
            TimerTheme {
                NavGraph()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "timer" || uri.host != "coupon") return
        val duration = uri.getQueryParameter("duration")?.toIntOrNull() ?: return
        val nonce = uri.getQueryParameter("nonce") ?: return
        val sig = uri.getQueryParameter("sig") ?: return

        lifecycleScope.launch {
            val result = couponRepository.redeemDeepLinkCoupon(duration, nonce, sig)
            val msg = when (result) {
                CouponRedeemResult.SUCCESS -> "✓ Cupón de ${duration}min agregado a tu wallet"
                CouponRedeemResult.ALREADY_USED -> "Este cupón ya fue canjeado"
                CouponRedeemResult.INVALID_SIGNATURE -> "Cupón inválido"
                CouponRedeemResult.INVALID_FORMAT -> "Formato de cupón incorrecto"
            }
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun startTrackingService() {
        val serviceIntent = Intent(this, UsageTrackingService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
