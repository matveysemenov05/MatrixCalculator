package com.example.matrixcalculator

import java.util.Locale
import kotlin.math.*

data class Complex(val re: Double, val im: Double = 0.0) {
    operator fun plus(b: Complex) = Complex(re + b.re, im + b.im)
    operator fun minus(b: Complex) = Complex(re - b.re, im - b.im)
    operator fun unaryMinus() = Complex(-re, -im)
    operator fun times(b: Complex) = Complex(re * b.re - im * b.im, re * b.im + im * b.re)
    operator fun times(k: Double) = Complex(re * k, im * k)
    operator fun div(k: Double) = Complex(re / k, im / k)

    operator fun div(b: Complex): Complex {
        val d = b.re * b.re + b.im * b.im
        return if (abs(d) < 1e-15) Complex(0.0, 0.0)
        else Complex((re * b.re + im * b.im) / d, (im * b.re - re * b.im) / d)
    }

    fun abs(): Double = hypot(re, im)

    override fun toString(): String {
        val cleanRe = if (abs(re) < 1e-4) 0.0 else re
        val cleanIm = if (abs(im) < 1e-4) 0.0 else im
        val fmtRe = formatNum(cleanRe)
        val fmtIm = formatNum(abs(cleanIm))

        return when {
            cleanIm == 0.0 -> fmtRe
            cleanRe == 0.0 -> if (cleanIm > 0) "${fmtIm}i" else "-${fmtIm}i"
            cleanIm > 0 -> "$fmtRe + ${fmtIm}i"
            else -> "$fmtRe - ${fmtIm}i"
        }
    }

    private fun formatNum(v: Double): String {
        val r = Math.round(v * 1000.0) / 1000.0
        return if (r % 1.0 == 0.0) r.toInt().toString() else String.format(Locale.US, "%.3f", r).trimEnd('0').trimEnd('.')
    }
}