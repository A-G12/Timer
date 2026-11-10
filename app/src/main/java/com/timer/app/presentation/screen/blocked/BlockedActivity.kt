package com.timer.app.presentation.screen.blocked

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.app.data.repository.CouponRepository
import com.timer.app.presentation.theme.BlockedRed
import com.timer.app.presentation.theme.CouponGold
import com.timer.app.presentation.theme.TimerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockedViewModel @Inject constructor(private val couponRepo: CouponRepository) : ViewModel() {
    val availableCount: StateFlow<Int> = couponRepo.getAvailableCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _usedCoupon = MutableSharedFlow<Boolean>()
    val usedCoupon: SharedFlow<Boolean> = _usedCoupon.asSharedFlow()

    fun useCoupon() {
        viewModelScope.launch {
            val result = couponRepo.useCoupon()
            _usedCoupon.emit(result)
        }
    }
}

@AndroidEntryPoint
class BlockedActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
    }

    private val vm: BlockedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val blockedPkg = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val blockedName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(blockedPkg, 0)
            ).toString()
        } catch (e: Exception) { blockedPkg }

        setContent {
            TimerTheme {
                val coupons by vm.availableCount.collectAsState()

                LaunchedEffect(vm.usedCoupon) {
                    vm.usedCoupon.collect { success ->
                        if (success) goHome()
                    }
                }

                BlockedScreen(
                    blockedAppName = blockedName,
                    availableCoupons = coupons,
                    onUseCoupon = { vm.useCoupon() },
                    onGoHome = { goHome() }
                )
            }
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    override fun onBackPressed() {
        goHome()
    }
}

@Composable
private fun BlockedScreen(
    blockedAppName: String,
    availableCoupons: Int,
    onUseCoupon: () -> Unit,
    onGoHome: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BlockedRed.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = Color.White
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Tiempo agotado",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                blockedAppName,
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Has alcanzado tu límite diario de uso.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))

            if (availableCoupons > 0) {
                Button(
                    onClick = onUseCoupon,
                    colors = ButtonDefaults.buttonColors(containerColor = CouponGold),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Filled.ConfirmationNumber, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Quemar cupón ($availableCoupons disponibles)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Icon(Icons.Filled.Home, null)
                Spacer(Modifier.width(8.dp))
                Text("Ir al inicio", color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (availableCoupons == 0) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Sin cupones disponibles.\nSe generan 2 nuevos cupones cada semana.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
