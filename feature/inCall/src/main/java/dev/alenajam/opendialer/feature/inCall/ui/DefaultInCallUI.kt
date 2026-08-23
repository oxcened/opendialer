package dev.alenajam.opendialer.feature.inCall.ui

import androidx.compose.runtime.Composable
import dev.alenajam.opendialer.core.common.ui.InCallUI
import javax.inject.Inject

class DefaultInCallUI @Inject constructor() : InCallUI {
    @Composable
    override fun Content() {
        InCallScreen()
    }
}
