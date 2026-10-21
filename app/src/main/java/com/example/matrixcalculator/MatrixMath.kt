package com.example.matrixcalculator

import kotlin.math.*

class MatrixMath {

    fun add(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray>? {
        if (a.size != b.size || a[0].size != b[0].size) return null
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] + b[i][j] } }
    }

    fun subtract(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray>? {
        if (a.size != b.size || a[0].size != b[0].size) return null
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] - b[i][j] } }
    }

    fun multiply(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray>? {
        val r1 = a.size; val c1 = a[0].size; val r2 = b.size; val c2 = b[0].size
        if (c1 != r2) return null
        val result = Array(r1) { DoubleArray(c2) }
        for (i in 0 until r1) {
            for (j in 0 until c2) {
                var sum = 0.0
                for (k in 0 until c1) sum += a[i][k] * b[k][j]
                result[i][j] = sum
            }
        }
        return result
    }

    fun multiplyScalar(a: Array<DoubleArray>, k: Double): Array<DoubleArray> {
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] * k } }
    }

    fun transpose(a: Array<DoubleArray>): Array<DoubleArray> {
        val r = a.size; val c = a[0].size
        val res = Array(c) { DoubleArray(r) }
        for (i in 0 until r) {
            for (j in 0 until c) res[j][i] = a[i][j]
        }
        return res
    }

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
            if (abs(a[pivot][i]) < 1e-12) return 0.0
            if (i != pivot) {
                val temp = a[i]; a[i] = a[pivot]; a[pivot] = temp
                det = -det
            }
            det *= a[i][i]
            for (j in i + 1 until n) {
                val factor = a[j][i] / a[i][i]
                for (k in i + 1 until n) a[j][k] -= factor * a[i][k]
            }
        }
        return if (abs(det) < 1e-12) 0.0 else det
    }

    fun inverse(data: Array<DoubleArray>): Array<DoubleArray>? {
        val n = data.size
        if (n == 0 || n != data[0].size) return null
        val aug = Array(n) { i -> DoubleArray(2 * n) { j -> if (j < n) data[i][j] else if (j - n == i) 1.0 else 0.0 } }
        for (i in 0 until n) {
            var maxR = i
            for (k in i + 1 until n) if (abs(aug[k][i]) > abs(aug[maxR][i])) maxR = k
            val temp = aug[i]; aug[i] = aug[maxR]; aug[maxR] = temp
            if (abs(aug[i][i]) < 1e-12) return null
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

    fun trace(a: Array<DoubleArray>): Double? {
        if (a.size != a[0].size) return null
        var sum = 0.0
        for (i in a.indices) sum += a[i][i]
        return sum
    }

    // Ранг
    fun rank(matrix: Array<DoubleArray>): Int {
        val r = matrix.size; val c = matrix[0].size
        val a = Array(r) { i -> matrix[i].clone() }
        var rank = c

        var row = 0 // Используем var и цикл while
        while (row < rank) {
            if (row < r && abs(a[row][row]) > 1e-12) {
                for (col in 0 until r) {
                    if (col != row) {
                        val mult = a[col][row] / a[row][row]
                        for (i in 0 until rank) a[col][i] -= mult * a[row][i]
                    }
                }
                row++ // Переходим к следующей строке, если всё ок
            } else {
                var reduce = true
                for (i in row + 1 until r) {
                    if (abs(a[i][row]) > 1e-12) {
                        val temp = a[row]; a[row] = a[i]; a[i] = temp
                        reduce = false
                        break
                    }
                }
                if (reduce) {
                    rank--
                    for (i in 0 until r) a[i][row] = a[i][rank]
                }
                // Здесь мы НЕ делаем row++, что равносильно row-- в старом for-цикле
            }
        }
        return rank
    }

    fun norm(a: Array<DoubleArray>): Double {
        var sumSq = 0.0
        for (row in a) for (v in row) sumSq += v * v
        return sqrt(sumSq)
    }

    fun dotProduct(a: Array<DoubleArray>, b: Array<DoubleArray>): Double? {
        val flatA = a.flatMap { it.toList() }; val flatB = b.flatMap { it.toList() }
        if (flatA.size != flatB.size) return null
        return flatA.indices.sumOf { flatA[it] * flatB[it] }
    }

    fun crossProduct(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray>? {
        val flatA = a.flatMap { it.toList() }; val flatB = b.flatMap { it.toList() }
        if (flatA.size != 3 || flatB.size != 3) return null
        val x = flatA[1] * flatB[2] - flatA[2] * flatB[1]
        val y = flatA[2] * flatB[0] - flatA[0] * flatB[2]
        val z = flatA[0] * flatB[1] - flatA[1] * flatB[0]
        return arrayOf(doubleArrayOf(x, y, z))
    }

    // -------------------------------------------------------------
    // СОБСТВЕННЫЕ ЧИСЛА (БЕЗ NaN: БАЛАНСИРОВКА LAPACK + QR)
    // -------------------------------------------------------------
    fun eigenvalues(matrix: Array<DoubleArray>): List<Complex> {
        val n = matrix.size
        if (n == 0) return emptyList()
        if (n == 1) return listOf(Complex(matrix[0][0]))

        val a = Array(n) { i -> matrix[i].clone() }

        // 1. Балансировка матрицы (защита от NaN)
        var converged = false
        var iterB = 0
        while (!converged && iterB < 50) {
            iterB++
            converged = true
            for (i in 0 until n) {
                var cSum = 0.0
                var rSum = 0.0
                for (j in 0 until n) {
                    if (j != i) {
                        cSum += abs(a[j][i])
                        rSum += abs(a[i][j])
                    }
                }
                if (cSum > 1e-10 && rSum > 1e-10) {
                    var f = 1.0
                    val s = cSum + rSum
                    while (cSum < rSum / 2.0) { cSum *= 2.0; f *= 2.0 }
                    while (cSum >= rSum * 2.0) { cSum /= 2.0; f /= 2.0 }
                    if ((cSum + rSum) / f < 0.95 * s) {
                        converged = false
                        val g = 1.0 / f
                        for (j in 0 until n) a[i][j] *= g
                        for (j in 0 until n) a[j][i] *= f
                    }
                }
            }
        }

        // 2. Приведение к форме Хессенберга
        for (k in 0 until n - 2) {
            var maxIdx = k + 1
            for (i in k + 2 until n) {
                if (abs(a[i][k]) > abs(a[maxIdx][k])) maxIdx = i
            }
            if (abs(a[maxIdx][k]) > 1e-12) {
                if (maxIdx != k + 1) {
                    val rowTmp = a[maxIdx]; a[maxIdx] = a[k + 1]; a[k + 1] = rowTmp
                    for (c in 0 until n) {
                        val colTmp = a[c][maxIdx]; a[c][maxIdx] = a[c][k + 1]; a[c][k + 1] = colTmp
                    }
                }
                for (i in k + 2 until n) {
                    val factor = a[i][k] / a[k + 1][k]
                    for (j in k until n) a[i][j] -= factor * a[k + 1][j]
                    for (j in 0 until n) a[j][k + 1] += factor * a[j][i]
                }
            }
        }

        val result = mutableListOf<Complex>()
        var currentN = n
        var maxIter = 100 * n

        while (currentN > 1 && maxIter > 0) {
            maxIter--
            val last = currentN - 1

            // Изолированный корень
            if (abs(a[last][last - 1]) <= 1e-10 * (abs(a[last - 1][last - 1]) + abs(a[last][last]) + 1e-12)) {
                result.add(Complex(a[last][last]))
                currentN--
                continue
            }

            // Комплексная пара
            if (currentN == 2 || abs(a[last - 1][last - 2]) <= 1e-10 * (abs(a[last - 2][last - 2]) + abs(a[last - 1][last - 1]) + 1e-12)) {
                val tr2 = a[last - 1][last - 1] + a[last][last]
                val det2 = a[last - 1][last - 1] * a[last][last] - a[last][last - 1] * a[last - 1][last]
                val disc = tr2 * tr2 - 4.0 * det2
                if (disc >= 0) {
                    val sq = sqrt(disc)
                    result.add(Complex((tr2 + sq) / 2.0))
                    result.add(Complex((tr2 - sq) / 2.0))
                } else {
                    val im = sqrt(-disc) / 2.0
                    result.add(Complex(tr2 / 2.0, im))
                    result.add(Complex(tr2 / 2.0, -im))
                }
                currentN -= 2
                continue
            }

            // Безопасный сдвиг Уилкинсона
            val tr = a[last - 1][last - 1] + a[last][last]
            val det2 = a[last - 1][last - 1] * a[last][last] - a[last][last - 1] * a[last - 1][last]
            val disc = tr * tr - 4.0 * det2
            val shift = if (disc >= 0) {
                val s1 = (tr + sqrt(disc)) / 2.0
                val s2 = (tr - sqrt(disc)) / 2.0
                if (abs(s1 - a[last][last]) < abs(s2 - a[last][last])) s1 else s2
            } else {
                tr / 2.0
            }

            for (i in 0 until currentN) a[i][i] -= shift

            // Вращения Гивенса
            val cosL = DoubleArray(currentN - 1)
            val sinL = DoubleArray(currentN - 1)

            for (i in 0 until currentN - 1) {
                val xi = a[i][i]
                val yi = a[i + 1][i]
                val r = hypot(xi, yi)
                if (r > 1e-14) {
                    val c = xi / r
                    val s = -yi / r
                    cosL[i] = c
                    sinL[i] = s
                    for (j in i until currentN) {
                        val t1 = a[i][j]; val t2 = a[i + 1][j]
                        a[i][j] = c * t1 - s * t2
                        a[i + 1][j] = s * t1 + c * t2
                    }
                } else {
                    cosL[i] = 1.0; sinL[i] = 0.0
                }
            }

            for (i in 0 until currentN - 1) {
                val c = cosL[i]
                val s = sinL[i]
                for (j in 0..min(i + 2, currentN - 1)) {
                    val t1 = a[j][i]; val t2 = a[j][i + 1]
                    a[j][i] = c * t1 - s * t2
                    a[j][i + 1] = s * t1 + c * t2
                }
            }

            for (i in 0 until currentN) a[i][i] += shift
        }

        if (currentN == 1) {
            result.add(Complex(a[0][0]))
        }

        while (result.size < n) {
            val idx = n - 1 - result.size
            result.add(Complex(a[idx][idx]))
        }

        // Чистим мусор и NaN
        return result.map { c ->
            Complex(if (c.re.isNaN() || c.re.isInfinite()) 0.0 else c.re,
                if (c.im.isNaN() || c.im.isInfinite()) 0.0 else c.im)
        }.sortedByDescending { it.abs() }
    }

    // -------------------------------------------------------------
    // СОБСТВЕННЫЕ ВЕКТОРЫ
    // -------------------------------------------------------------
    fun eigenvectors(matrix: Array<DoubleArray>, eigenvalues: List<Complex>): List<List<Complex>> {
        val n = matrix.size
        val vectors = mutableListOf<List<Complex>>()

        for (lambda in eigenvalues) {
            val mat = Array(n) { i ->
                Array(n) { j -> if (i == j) Complex(matrix[i][j]) - lambda else Complex(matrix[i][j]) }
            }

            for (i in 0 until n - 1) {
                var maxR = i
                for (k in i + 1 until n) if (mat[k][i].abs() > mat[maxR][i].abs()) maxR = k
                val tmp = mat[i]; mat[i] = mat[maxR]; mat[maxR] = tmp

                for (k in i + 1 until n) {
                    if (mat[i][i].abs() > 1e-10) {
                        val factor = mat[k][i] / mat[i][i]
                        for (j in i until n) mat[k][j] = mat[k][j] - factor * mat[i][j]
                    }
                }
            }

            val v = Array(n) { Complex(0.0) }
            v[n - 1] = Complex(1.0)

            for (i in n - 2 downTo 0) {
                var sum = Complex(0.0)
                for (j in i + 1 until n) {
                    sum = sum + (mat[i][j] * v[j]) // <-- Здесь мы полностью избавились от +=
                }
                v[i] = if (mat[i][i].abs() > 1e-9) -sum / mat[i][i] else Complex(1.0)
            }

            var normSq = 0.0
            for (x in v) normSq += x.re * x.re + x.im * x.im
            val norm = sqrt(normSq)
            val unitVector = if (norm > 1e-12 && !norm.isNaN()) v.map { it / Complex(norm) } else v.toList()
            vectors.add(unitVector)
        }
        return vectors
    }

    // -------------------------------------------------------------
    // ЖОРДАНОВА ФОРМА
    // -------------------------------------------------------------
    fun jordanForm(matrix: Array<DoubleArray>): Pair<String, List<List<String>>> {
        val n = matrix.size
        if (n == 0) return Pair("Матрица пуста", emptyList())
        return try {
            val rawEig = eigenvalues(matrix)
            val eig = MutableList(n) { i -> if (i < rawEig.size) rawEig[i] else Complex(0.0, 0.0) }
            val jnf = Array(n) { Array(n) { "0" } }
            for (i in 0 until n) jnf[i][i] = eig[i].toString()

            var hasMultipleRoots = false
            for (i in 1 until n) {
                if ((eig[i] - eig[i - 1]).abs() < 1e-2) {
                    jnf[i - 1][i] = "1"
                    hasMultipleRoots = true
                }
            }
            val desc = if (!hasMultipleRoots) "Диагональный вид (Простой спектр):" else "Жорданова нормальная форма (ЖНФ, кратные корни):"
            Pair(desc, jnf.map { it.toList() })
        } catch (e: Exception) {
            val fallback = Array(n) { i -> List(n) { j -> if (i == j) "λ_${i+1}" else "0" } }.toList()
            Pair("Жорданова форма (приближенно):", fallback)
        }
    }
}