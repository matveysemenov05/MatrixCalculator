package com.example.matrixcalculator

import kotlin.math.abs

class MatrixMath {

    // Сложение A + B
    fun add(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray>? {
        if (a.size != b.size || a[0].size != b[0].size) return null
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] + b[i][j] } }
    }

    // Вычитание A - B
    fun subtract(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray>? {
        if (a.size != b.size || a[0].size != b[0].size) return null
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] - b[i][j] } }
    }

    // Умножение A * B
    fun multiply(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray>? {
        val r1 = a.size
        val c1 = a[0].size
        val r2 = b.size
        val c2 = b[0].size
        if (c1 != r2) return null // Число столбцов первой должно быть равно строкам второй

        val result = Array(r1) { DoubleArray(c2) }
        for (i in 0 until r1) {
            for (j in 0 until c2) {
                var sum = 0.0
                for (k in 0 until c1) {
                    sum += a[i][k] * b[k][j]
                }
                result[i][j] = sum
            }
        }
        return result
    }

    // Умножение на число (скаляр)
    fun multiplyScalar(a: Array<DoubleArray>, k: Double): Array<DoubleArray> {
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] * k } }
    }

    // Транспонирование Aᵀ
    fun transpose(a: Array<DoubleArray>): Array<DoubleArray> {
        val r = a.size
        val c = a[0].size
        val res = Array(c) { DoubleArray(r) }
        for (i in 0 until r) {
            for (j in 0 until c) res[j][i] = a[i][j]
        }
        return res
    }

    // Определитель (det) через приведение к верхнетреугольной матрице
    fun determinant(matrix: Array<DoubleArray>): Double? {
        val n = matrix.size
        if (n != matrix[0].size) return null
        val a = Array(n) { i -> matrix[i].clone() }
        var det = 1.0

        for (i in 0 until n) {
            var pivot = i
            for (j in i + 1 until n) {
                if (abs(a[j][i]) > abs(a[pivot][i])) pivot = j
            }
            if (abs(a[pivot][i]) < 1e-9) return 0.0
            if (i != pivot) {
                val temp = a[i]
                a[i] = a[pivot]
                a[pivot] = temp
                det = -det
            }
            det *= a[i][i]
            for (j in i + 1 until n) {
                val factor = a[j][i] / a[i][i]
                for (k in i + 1 until n) {
                    a[j][k] -= factor * a[i][k]
                }
            }
        }
        return if (abs(det) < 1e-9) 0.0 else det
    }

    // Обратная матрица A⁻¹ (Гаусс-Жордан)
    fun inverse(data: Array<DoubleArray>): Array<DoubleArray>? {
        val n = data.size
        if (n == 0 || n != data[0].size) return null

        val aug = Array(n) { i ->
            DoubleArray(2 * n) { j ->
                if (j < n) data[i][j] else if (j - n == i) 1.0 else 0.0
            }
        }

        for (i in 0 until n) {
            var maxR = i
            for (k in i + 1 until n) {
                if (abs(aug[k][i]) > abs(aug[maxR][i])) maxR = k
            }
            val temp = aug[i]
            aug[i] = aug[maxR]
            aug[maxR] = temp

            if (abs(aug[i][i]) < 1e-9) return null

            val div = aug[i][i]
            for (j in 0 until 2 * n) aug[i][j] /= div

            for (k in 0 until n) {
                if (k != i) {
                    val factor = aug[k][i]
                    for (j in 0 until 2 * n) aug[k][j] -= factor * aug[i][j]
                }
            }
        }

        return Array(n) { i -> DoubleArray(n) { j -> aug[i][j + n] } }
    }

    // След матрицы (сумма элементов главной диагонали)
    fun trace(a: Array<DoubleArray>): Double? {
        if (a.size != a[0].size) return null
        var sum = 0.0
        for (i in a.indices) sum += a[i][i]
        return sum
    }

    // Ранг матрицы
    fun rank(matrix: Array<DoubleArray>): Int {
        val r = matrix.size
        val c = matrix[0].size
        val a = Array(r) { i -> matrix[i].clone() }
        var rank = c

        for (row in 0 until rank) {
            if (row < r && abs(a[row][row]) > 1e-9) {
                for (col in 0 until r) {
                    if (col != row) {
                        val mult = a[col][row] / a[row][row]
                        for (i in 0 until rank) a[col][i] -= mult * a[row][i]
                    }
                }
            } else {
                var reduce = true
                for (i in row + 1 until r) {
                    if (abs(a[i][row]) > 1e-9) {
                        val temp = a[row]
                        a[row] = a[i]
                        a[i] = temp
                        reduce = false
                        break
                    }
                }
                if (reduce) {
                    rank--
                    for (i in 0 until r) a[i][row] = a[i][rank]
                }
                row.dec()
            }
        }
        return rank
    }
    // Скалярное произведение (Dot Product)
    fun dotProduct(a: Array<DoubleArray>, b: Array<DoubleArray>): Double? {
        val flatA = a.flatMap { it.toList() }
        val flatB = b.flatMap { it.toList() }
        if (flatA.size != flatB.size) return null
        return flatA.indices.sumOf { flatA[it] * flatB[it] }
    }

    // Векторное произведение для векторов длины 3 (Cross Product)
    fun crossProduct(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray>? {
        val flatA = a.flatMap { it.toList() }
        val flatB = b.flatMap { it.toList() }
        if (flatA.size != 3 || flatB.size != 3) return null

        val x = flatA[1] * flatB[2] - flatA[2] * flatB[1]
        val y = flatA[2] * flatB[0] - flatA[0] * flatB[2]
        val z = flatA[0] * flatB[1] - flatA[1] * flatB[0]

        return arrayOf(doubleArrayOf(x, y, z)) // Возвращаем как вектор-строку 1x3
    }

    // Норма Фробениуса / Длина вектора (Norm)
    fun norm(a: Array<DoubleArray>): Double {
        var sumSq = 0.0
        for (row in a) {
            for (v in row) sumSq += v * v
        }
        return kotlin.math.sqrt(sumSq)
    }
}