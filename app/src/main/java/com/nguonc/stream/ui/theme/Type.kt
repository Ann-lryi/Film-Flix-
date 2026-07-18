package com.nguonc.stream.ui.theme

import androidx.compose.ui.text.font.FontFamily

/*
 * ───────────────────────────────────────────────────────────────────────────
 *  FilmFlix Premium 3.0 — Custom Font Loading
 *  ───────────────────────────────────────────────────────────────────────────
 *
 *  Current status
 *  --------------
 *  We ship with `FontFamily.SansSerif` which on Android 12+ resolves to
 *  Google Sans (a beautiful geometric sans designed for screen). On older
 *  Android versions it falls back to Roboto. Either way the typography
 *  scale + tuned weights below will look polished.
 *
 *  Upgrade path — Google Fonts downloadable Outfit / Sora
 *  ------------------------------------------------------
 *  The dependency `androidx.compose.ui:ui-text-google-fonts` is already
 *  added to app/build.gradle.kts. To wire it up:
 *
 *  1. Add the cert resource file at
 *       `app/src/main/res/values/font_certs.xml`
 *     with the well-known Google Play Services cert arrays
 *     (publicly documented at https://developers.google.com/fonts/docs/android).
 *
 *  2. Replace `AppFontFamily` and `AppDisplayFontFamily` below with:
 *
 *       val provider = GoogleFont.Provider(
 *           providerAuthority = "com.google.android.gms.fonts",
 *           providerPackage = "com.google.android.gms",
 *           certificates = R.array.com_google_android_gms_fonts_certs
 *       )
 *       val outfit = GoogleFont("Outfit")
 *       val AppFontFamily = FontFamily(
 *           Font(outfit, provider, FontWeight.Normal),
 *           Font(outfit, provider, FontWeight.Medium),
 *           Font(outfit, provider, FontWeight.SemiBold),
 *           Font(outfit, provider, FontWeight.Bold),
 *           Font(outfit, provider, FontWeight.ExtraBold),
 *           Font(outfit, provider, FontWeight.Black),
 *       )
 *
 *  3. Add imports:
 *       import androidx.compose.ui.text.googlefonts.GoogleFont
 *       import androidx.compose.ui.text.font.Font
 *       import androidx.compose.ui.text.font.FontWeight
 *       import com.nguonc.stream.R
 *
 *  The font will be fetched on first use; if the device is offline or
 *  doesn't have Google Play Services, Compose automatically falls back
 *  to the system default — no crash, no broken UI.
 *  ───────────────────────────────────────────────────────────────────────────
 */

/** Body / UI font. Outfit when upgraded; Google Sans / Roboto otherwise. */
val AppFontFamily: FontFamily = FontFamily.SansSerif

/** Display / hero font. Can be Sora or Plus Jakarta Sans when upgraded. */
val AppDisplayFontFamily: FontFamily = FontFamily.SansSerif
