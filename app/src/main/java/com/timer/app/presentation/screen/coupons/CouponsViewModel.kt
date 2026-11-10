package com.timer.app.presentation.screen.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.app.data.repository.CouponRepository
import com.timer.app.domain.model.Coupon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponsViewModel @Inject constructor(private val repo: CouponRepository) : ViewModel() {

    val coupons: StateFlow<List<Coupon>> = repo.getAllCoupons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCount: StateFlow<Int> = repo.getAvailableCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _useCouponResult = MutableSharedFlow<Boolean>()
    val useCouponResult: SharedFlow<Boolean> = _useCouponResult.asSharedFlow()

    fun useCoupon() {
        viewModelScope.launch {
            val result = repo.useCoupon()
            _useCouponResult.emit(result)
        }
    }
}
