package com.nguonc.stream.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.nguonc.stream.R

/**
 * ───────────────────────────────────────────────────────────────────────────
 *  FilmFlix Premium 3.0 — Custom Font Loading (WIRED UP)
 *  ───────────────────────────────────────────────────────────────────────────
 *
 *  Uses Google Fonts downloadable fonts:
 *   - Body / UI font: Outfit (modern geometric sans, great for UI)
 *   - Display / hero font: Sora (slightly more characterful, for big titles)
 *
 *  If the device is offline or doesn't have Google Play Services,
 *  Compose automatically falls back to the system default (Google Sans on
 *  Android 12+, Roboto on older) — no crash, no broken UI.
 *  ───────────────────────────────────────────────────────────────────────────
 */

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

/** Body / UI font — Outfit (modern geometric sans). */
val AppFontFamily: FontFamily = FontFamily(
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Black),
)

/** Display / hero font — Sora (characterful geometric). */
val AppDisplayFontFamily: FontFamily = FontFamily(
    Font(googleFont = GoogleFont("Sora"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Sora"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Sora"), fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = GoogleFont("Sora"), fontProvider = provider, weight = FontWeight.Black),
)
