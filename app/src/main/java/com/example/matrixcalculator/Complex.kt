package com.example.matrixcalculator

import java.util.Locale
import kotlin.math.*

data class Complex(val re: Double, val im: Double = 0.0) {
    operator fun plus(c: Complex) = Complex(re + c.re, im + c.im)
    operator fun minus(c: Complex) = Complex(re - c.re, im - c.im)
    operator fun times(c: Complex) = Complex(re * c.re - im * c.im, re * c.im + im * c.re)
    operator fun times(k: Double) = Complex(re * k, im * k)
    operator fun div(c: Complex): Complex {
        val den = c.re * c.re + c.im * c.im
        if (abs(den) < 1e-12) return Complex(0.0, 0.0)
        return Complex((re * c.re + im * c.im) / den, (im * c.re - re * c.im) / den)
    }
    operator fun unaryMinus() = Complex(-re, -im)

    fun abs(): Double = hypot(re, im)

    override fun toString(): String {
        val cleanRe = if (abs(re) < 1e-4) 0.0 else re
        val cleanIm = if (abs(im) < 1e-4) 0.0 else im

        return when {
            cleanIm == 0.0 -> formatD(cleanRe)
            cleanRe == 0.0 -> "${formatD(cleanIm)}i"
            cleanIm > 0 -> "${formatD(cleanRe)} + ${formatD(cleanIm)}i"
            else -> "${formatD(cleanRe)} - ${formatD(abs(cleanIm))}i"
        }
    }

    private fun formatD(v: Double): String {
        return if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.US, "%.3f", v).trimEnd('0').trimEnd('.')
    }
}