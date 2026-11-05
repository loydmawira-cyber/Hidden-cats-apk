package com.example.hiddencats.billing

sealed class BillingProduct(
    val productId: String,
    val title: String,
    val description: String,
    val defaultPriceFormatted: String,
    val isConsumable: Boolean
) {
    data object PremiumUpgrade : BillingProduct(
        productId = "premium_upgrade",
        title = "Premium Detective Pass",
        description = "Unlock all board sizes (up to 9x9), infinite hints, and ad-free experience",
        defaultPriceFormatted = "$4.99",
        isConsumable = false
    )

    data object HintPackSmall : BillingProduct(
        productId = "hint_pack_small",
        title = "Pouch of 5 Hints",
        description = "5 deductive hints to help locate hidden cats",
        defaultPriceFormatted = "$0.99",
        isConsumable = true
    )

    data object HintPackLarge : BillingProduct(
        productId = "hint_pack_large",
        title = "Basket of 20 Hints",
        description = "Best value! 20 deductive hints for expert color logic puzzles",
        defaultPriceFormatted = "$2.49",
        isConsumable = true
    )

    data object RemoveAds : BillingProduct(
        productId = "remove_ads",
        title = "Remove All Ads",
        description = "Enjoy clean, uninterrupted cozy puzzle gameplay forever",
        defaultPriceFormatted = "$1.99",
        isConsumable = false
    )

    companion object {
        val ALL = listOf(PremiumUpgrade, HintPackSmall, HintPackLarge, RemoveAds)

        fun findByProductId(id: String): BillingProduct? = ALL.find { it.productId == id }
    }
}

sealed class PurchaseResult {
    data class Success(val productId: String) : PurchaseResult()
    data class Pending(val message: String = "Purchase is pending") : PurchaseResult()
    data class UserCancelled(val message: String = "Purchase cancelled") : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
    data class AlreadyOwned(val productId: String) : PurchaseResult()
}
