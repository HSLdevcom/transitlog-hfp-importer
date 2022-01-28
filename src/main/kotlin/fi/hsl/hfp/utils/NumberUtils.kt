package fi.hsl.hfp.utils

import java.math.RoundingMode

fun Double.roundToString(scale: Int): String = this.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toPlainString()