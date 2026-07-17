package re.savio.mobile.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Tokens typographie — Rubik, alignés handoff Violett. */
object SavioType {
    private val rubik = rubikFontFamily

    val Hero = TextStyle(
        fontFamily = rubik,
        fontSize = 34.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-0.01).sp,
        lineHeight = 36.sp,
    )
    val PageTitle = TextStyle(
        fontFamily = rubik,
        fontSize = 17.sp,
        fontWeight = FontWeight.Medium,
    )
    val SectionTitle = TextStyle(
        fontFamily = rubik,
        fontSize = 19.sp,
        fontWeight = FontWeight.Medium,
    )
    val BodyMedium = TextStyle(
        fontFamily = rubik,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
    )
    val H1 = TextStyle(
        fontFamily = rubik,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
    )
    val H2 = TextStyle(
        fontFamily = rubik,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
    )
    val H3 = TextStyle(
        fontFamily = rubik,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
    )
    val BodyLarge = TextStyle(
        fontFamily = rubik,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
    )
    val BodySmall = TextStyle(
        fontFamily = rubik,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
    )
    val Label = TextStyle(
        fontFamily = rubik,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
    )
    val NavLabel = TextStyle(
        fontFamily = rubik,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
    )
    val Caption = TextStyle(
        fontFamily = rubik,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
    )
    val Value = TextStyle(
        fontFamily = rubik,
        fontSize = 15.5.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp,
    )
}
