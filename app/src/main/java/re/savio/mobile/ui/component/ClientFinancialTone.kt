package re.savio.mobile.ui.component

import re.savio.mobile.ui.designsystem.AppBadgeStyle

fun statusToneToBadgeStyle(tone: String): AppBadgeStyle =
    when (tone.lowercase()) {
        "success" -> AppBadgeStyle.Success
        "warning" -> AppBadgeStyle.Warning
        "danger" -> AppBadgeStyle.Error
        "info" -> AppBadgeStyle.Info
        else -> AppBadgeStyle.Primary
    }
