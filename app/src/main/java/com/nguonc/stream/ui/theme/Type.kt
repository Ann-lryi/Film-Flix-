package com.nguonc.stream.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.nguonc.stream.R
import androidx.compose.ui.text.font.Font

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
    Font(GoogleFont("Outfit"), provider, androidx.compose.ui.text.font.FontWeight.Normal),
    Font(GoogleFont("Outfit"), provider, androidx.compose.ui.text.font.FontWeight.Medium),
    Font(GoogleFont("Outfit"), provider, androidx.compose.ui.text.font.FontWeight.SemiBold),
    Font(GoogleFont("Outfit"), provider, androidx.compose.ui.text.font.FontWeight.Bold),
    Font(GoogleFont("Outfit"), provider, androidx.compose.ui.text.font.FontWeight.ExtraBold),
    Font(GoogleFont("Outfit"), provider, androidx.compose.ui.text.font.FontWeight.Black),
)

/** Display / hero font — Sora (characterful geometric). */
val AppDisplayFontFamily: FontFamily = FontFamily(
    Font(GoogleFont("Sora"), provider, androidx.compose.ui.text.font.FontWeight.SemiBold),
    Font(GoogleFont("Sora"), provider, androidx.compose.ui.text.font.FontWeight.Bold),
    Font(GoogleFont("Sora"), provider, androidx.compose.ui.text.font.FontWeight.ExtraBold),
    Font(GoogleFont("Sora"), provider, androidx.compose.ui.text.font.FontWeight.Black),
)
