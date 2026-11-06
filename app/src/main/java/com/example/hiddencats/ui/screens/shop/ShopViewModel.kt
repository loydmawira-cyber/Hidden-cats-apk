package com.example.hiddencats.ui.screens.shop

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hiddencats.billing.BillingProduct
import com.example.hiddencats.billing.BillingRepository
import com.example.hiddencats.billing.PurchaseResult
import com.example.hiddencats.core.AppContainer
import com.example.hiddencats.data.local.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ShopProductUiModel(
    val id: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val isOwned: Boolean = false
)

data class ShopUiState(
    val products: List<ShopProductUiModel> = emptyList(),
    val isPremium: Boolean = false,
    val adsRemoved: Boolean = false,
    val hintBalance: Int = PreferencesManager.DEFAULT_HINTS,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

class ShopViewModel(
    private val billingRepository: BillingRepository,
    private val appContainer: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        billingRepository.startConnection()

        viewModelScope.launch {
            combine(
                billingRepository.productPrices,
                appContainer.gameRepository.preferences
            ) { prices, prefs ->
                val list = BillingProduct.ALL.map { p ->
                    val isOwned = when (p) {
                        BillingProduct.PremiumUpgrade -> prefs.premiumUnlocked
                        BillingProduct.RemoveAds -> prefs.adsRemoved || prefs.premiumUnlocked
                        else -> false
                    }
                    ShopProductUiModel(
                        id = p.productId,
                        title = p.title,
                        description = p.description,
                        formattedPrice = prices[p.productId] ?: p.defaultPriceFormatted,
                        isOwned = isOwned
                    )
                }
                _uiState.update { current ->
                    current.copy(
                        products = list,
                        isPremium = prefs.premiumUnlocked,
                        adsRemoved = prefs.adsRemoved,
                        hintBalance = prefs.hintBalance
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            billingRepository.purchaseEvents.collect { result ->
                when (result) {
                    is PurchaseResult.Success -> {
                        // Unlocking/granting is handled app-wide by PurchaseProcessor.
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userMessage = "Purchase successful! Thank you for supporting the game."
                            )
                        }
                    }
                    is PurchaseResult.UserCancelled -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is PurchaseResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, userMessage = result.message)
                        }
                    }
                    is PurchaseResult.AlreadyOwned -> {
                        _uiState.update {
                            it.copy(isLoading = false, userMessage = "Item is already owned!")
                        }
                    }
                    is PurchaseResult.Pending -> {
                        _uiState.update {
                            it.copy(isLoading = false, userMessage = "Purchase pending confirmation.")
                        }
                    }
                }
            }
        }
    }

    fun buyProduct(activity: Activity?, productId: String) {
        _uiState.update { it.copy(isLoading = true) }
        billingRepository.launchBillingFlow(activity, productId)
    }

    fun restorePurchases() {
        _uiState.update { it.copy(isLoading = true) }
        billingRepository.restorePurchases { count ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userMessage = if (count > 0) "Restored $count purchases." else "No past purchases found to restore."
                )
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ShopViewModel(
                        billingRepository = appContainer.billingRepository,
                        appContainer = appContainer
                    ) as T
                }
            }
    }
}
