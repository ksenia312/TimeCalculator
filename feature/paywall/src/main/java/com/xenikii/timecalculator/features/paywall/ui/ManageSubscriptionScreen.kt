package com.xenikii.timecalculator.features.paywall.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ManageSubscriptionsCallback
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.navigator.LocalNavigator

/**
 * Not a real screen: [Purchases.showManageSubscriptions] launches Google Play's own subscription
 * management UI, so there's nothing of ours left to render - this just fires that call once and
 * pops itself off the back stack. Lives in :feature:paywall (not :feature:settings) because it's
 * the only feature module allowed to reference RevenueCat types directly.
 */
@Composable
fun ManageSubscriptionScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val errorMessage = stringResource(R.string.paywall_manage_subscription_error)

    LaunchedEffect(Unit) {
        Purchases.sharedInstance.showManageSubscriptions(
            context,
            object : ManageSubscriptionsCallback {
                override fun onSuccess() {
                    navigator.navigateBack()
                }

                override fun onError(error: PurchasesError) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    navigator.navigateBack()
                }
            },
        )
    }
}
