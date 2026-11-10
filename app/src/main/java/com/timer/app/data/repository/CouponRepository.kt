package com.timer.app.data.repository

import android.content.Context
import com.timer.app.data.database.dao.CouponDao
import com.timer.app.data.database.dao.UsedNonceDao
import com.timer.app.data.database.entity.CouponEntity
import com.timer.app.data.database.entity.UsedNonceEntity
import com.timer.app.data.datastore.SettingsDataStore
import com.timer.app.domain.model.Coupon
import com.timer.app.service.BlockingState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

enum class CouponRedeemResult {
    SUCCESS, INVALID_SIGNATURE, ALREADY_USED, INVALID_FORMAT
}

@Singleton
class CouponRepository @Inject constructor(
    private val dao: CouponDao,
    private val nonceDao: UsedNonceDao,
    private val settings: SettingsDataStore,
    @ApplicationContext private val context: Context
) {
    fun getAllCoupons(): Flow<List<Coupon>> = dao.getAllCoupons().map { entities ->
        entities.map { it.toModel() }
    }

    fun getAvailableCount(): Flow<Int> = dao.getAvailableCount()

    suspend fun generateWeeklyCouponsIfNeeded() {
        val currentWeek = getCurrentWeekOfYear()
        val lastWeek = settings.lastCouponWeek.first()
        val hasNoCoupons = dao.getAvailableCoupons().isEmpty()
        // Regenerate if: new week OR DB was wiped (DataStore says generated but no coupons exist)
        if (currentWeek != lastWeek || (lastWeek == currentWeek && hasNoCoupons)) {
            val now = System.currentTimeMillis()
            val duration = settings.couponDurationMinutes.first()
            dao.insert(CouponEntity(generatedAt = now, durationMinutes = duration))
            dao.insert(CouponEntity(generatedAt = now + 1, durationMinutes = duration))
            settings.setLastCouponWeek(currentWeek)
        }
    }

    suspend fun useCoupon(): Boolean {
        val available = dao.getAvailableCoupons()
        if (available.isEmpty()) return false
        val coupon = available.first()
        val duration = coupon.durationMinutes
        dao.update(coupon.copy(usedAt = System.currentTimeMillis()))
        val unlockUntil = System.currentTimeMillis() + (duration * 60 * 1000L)
        settings.setCouponUnlockUntil(unlockUntil)
        BlockingState.setCouponUnlockUntil(context, unlockUntil)
        return true
    }

    suspend fun isCouponActive(): Boolean {
        val unlockUntil = settings.couponUnlockUntil.first()
        return System.currentTimeMillis() < unlockUntil
    }

    // ── Deep link coupon redemption ────────────────────────────────────────

    suspend fun redeemDeepLinkCoupon(duration: Int, nonce: String, sig: String): CouponRedeemResult {
        if (duration !in listOf(15, 20, 30) || nonce.isBlank() || sig.isBlank()) {
            return CouponRedeemResult.INVALID_FORMAT
        }
        val secret = settings.couponSecret.first()
        val expected = hmac(secret, "$duration$nonce").take(8).uppercase()
        if (sig.uppercase() != expected) {
            return CouponRedeemResult.INVALID_SIGNATURE
        }
        if (nonceDao.exists(nonce) > 0) {
            return CouponRedeemResult.ALREADY_USED
        }
        // Add coupon to wallet (not spent yet)
        dao.insert(CouponEntity(generatedAt = System.currentTimeMillis(), durationMinutes = duration))
        nonceDao.insert(UsedNonceEntity(nonce = nonce, usedAt = System.currentTimeMillis()))
        // Clean up nonces older than 30 days
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        nonceDao.deleteOld(cutoff)
        return CouponRedeemResult.SUCCESS
    }

    private fun hmac(secret: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun getCurrentWeekOfYear(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.WEEK_OF_YEAR) + cal.get(Calendar.YEAR) * 100
    }

    private fun CouponEntity.toModel() = Coupon(
        id = id,
        generatedAt = generatedAt,
        usedAt = usedAt,
        durationMinutes = durationMinutes
    )
}
