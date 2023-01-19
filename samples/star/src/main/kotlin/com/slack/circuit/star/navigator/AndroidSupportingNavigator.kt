// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.star.navigator

import android.content.Intent
import com.slack.circuit.NavigableScreen
import com.slack.circuit.Navigator
import kotlinx.parcelize.Parcelize

/** Custom navigator that adds support for initiating navigation in standard Android. */
class AndroidSupportingNavigator(
  private val navigator: Navigator,
  private val onAndroidScreen: (AndroidScreen) -> Unit
) : Navigator by navigator {
  override fun goTo(screen: NavigableScreen) =
    when (screen) {
      is AndroidScreen -> onAndroidScreen(screen)
      else -> navigator.goTo(screen)
    }
}

sealed interface AndroidScreen : NavigableScreen {
  override val route: String
    get() = "AndroidScreen"

  @Parcelize data class CustomTabsIntentScreen(val url: String) : AndroidScreen
  @Parcelize data class IntentScreen(val intent: Intent) : AndroidScreen
}
