package com.example.morningcalculator.shared.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(
    name = "85% Dark Default locale Foldable",
    fontScale = 0.85f,
    device = "spec:width=673dp,height=841dp,dpi=240,orientation=landscape",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL
)
@Preview(name = "100% Light English", fontScale = 1f, uiMode = UI_MODE_TYPE_NORMAL, locale = "en")
@Preview(
    name = "360x640",
    widthDp = 360,
    heightDp = 640,
)
@Preview(
    name = "150% Dutch",
    fontScale = 1.5f,
    locale = "nl"
)
@Preview(
    name = "200% French",
    fontScale = 2f,
    locale = "fr" // should fall back to english if no French translations are available
)
annotation class PreviewAll