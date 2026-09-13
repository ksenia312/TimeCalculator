package com.xenikii.timecalculator.features.paywall.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import com.xenikii.timecalculator.shared.navigator.LocalNavigator

/**
 * Hosts RevenueCat's own paywall UI. Premium status itself is derived from
 * [com.revenuecat.purchases.Purchases.sharedInstance]'s customer info listener (see
 * PremiumRepositoryImpl in :data), so this screen only needs to close itself once a
 * purchase or restore completes - it doesn't have to read the result.
 */
@Composable
fun PaywallScreen() {
    val navigator = LocalNavigator.current
    val onDismiss = navigator::navigateBack

    val listener = remember(onDismiss) {
        object : PaywallListener {
            override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) {
                onDismiss()
            }

            override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                onDismiss()
            }
        }
    }

    val options = remember(listener) {
        PaywallOptions.Builder(dismissRequest = onDismiss)
            .setListener(listener)
            .build()
    }

    Paywall(options = options)
}
