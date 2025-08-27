package com.example.morningcalculator.shared.theme.font


import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.morningcalculator.R

@OptIn(ExperimentalTextApi::class)
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val Onest = GoogleFont("Onest")
val OnestFamily = FontFamily(
    Font(googleFont = Onest, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = Onest, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = Onest, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = Onest, fontProvider = provider, weight = FontWeight.Bold),
)