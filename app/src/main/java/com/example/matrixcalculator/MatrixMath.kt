package com.example.matrixcalculator

import java.util.Locale
import kotlin.math.*

class MatrixMath {
    fun identityMatrix(n: Int): Array<DoubleArray> {
        return Array(n) { i -> DoubleArray(n) { j -> if (i == j) 1.0 else 0.0 } }
    }

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
        val n = matrix.size; if (n != matrix[0].size) return null
        val a = Array(n) { i -> matrix[i].clone() }; var det = 1.0
        for (i in 0 until n) {
            var pivot = i; for (j in i + 1 until n) if (abs(a[j][i]) > abs(a[pivot][i])) pivot = j
            if (abs(a[pivot][i]) < 1e-12) return 0.0
            if (i != pivot) { val temp = a[i]; a[i] = a[pivot]; a[pivot] = temp; det = -det }
            det *= a[i][i]
            for (j in i + 1 until n) { val factor = a[j][i] / a[i][i]; for (k in i + 1 until n) a[j][k] -= factor * a[i][k] }
        }
        return if (abs(det) < 1e-12) 0.0 else det
    }

    fun inverse(data: Array<DoubleArray>): Array<DoubleArray>? {
        val n = data.size; if (n == 0 || n != data[0].size) return null
        val aug = Array(n) { i -> DoubleArray(2 * n) { j -> if (j < n) data[i][j] else if (j - n == i) 1.0 else 0.0 } }
        for (i in 0 until n) {
            var maxR = i; for (k in i + 1 until n) if (abs(aug[k][i]) > abs(aug[maxR][i])) maxR = k
            val temp = aug[i]; aug[i] = aug[maxR]; aug[maxR] = temp
            if (abs(aug[i][i]) < 1e-12) return null
            val div = aug[i][i]; for (j in 0 until 2 * n) aug[i][j] /= div
            for (k in 0 until n) if (k != i) { val factor = aug[k][i]; for (j in 0 until 2 * n) aug[k][j] -= factor * aug[i][j] }
        }
        return Array(n) { i -> DoubleArray(n) { j -> aug[i][j + n] } }
    }

    fun rank(matrix: Array<DoubleArray>): Int { return rankWithTolerance(matrix, 1e-9) }
    fun trace(a: Array<DoubleArray>): Double? { if (a.size != a[0].size) return null; var sum = 0.0; for (i in a.indices) sum += a[i][i]; return sum }
    fun norm(a: Array<DoubleArray>): Double { var sumSq = 0.0; for (row in a) for (v in row) sumSq += v * v; return sqrt(sumSq) }

    // Спектральная норма (2-норма) ||A||_2 = sqrt(max(lambda(A^T * A)))
    fun spectralNorm(a: Array<DoubleArray>): Double? {
        val at = transpose(a)
        val ata = multiply(at, a) ?: return null
        val eigs = eigenvalues(ata)
        val maxEig = eigs.maxOfOrNull { it.re } ?: 0.0
        return sqrt(maxEig.coerceAtLeast(0.0))
    }

    fun dotProduct(a: Array<DoubleArray>, b: Array<DoubleArray>): Double? { val flatA = a.flatMap { it.toList() }; val flatB = b.flatMap { it.toList() }; if (flatA.size != flatB.size) return null; return flatA.indices.sumOf { flatA[it] * flatB[it] } }
    fun crossProduct(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray>? { val flatA = a.flatMap { it.toList() }; val flatB = b.flatMap { it.toList() }; if (flatA.size != 3 || flatB.size != 3) return null; return arrayOf(doubleArrayOf(flatA[1] * flatB[2] - flatA[2] * flatB[1], flatA[2] * flatB[0] - flatA[0] * flatB[2], flatA[0] * flatB[1] - flatA[1] * flatB[0])) }

    // --- НОВЫЕ ВЫСШИЕ МАТРИЧНЫЕ ФУНКЦИИ ---
    fun matrixPower(a: Array<DoubleArray>, k: Int): Array<DoubleArray>? {
        val n = a.size
        if (k == 0) return identityMatrix(n)
        var res = Array(n) { i -> a[i].clone() }
        for (i in 1 until k) res = multiply(res, a)!!
        return res
    }

    fun matrixSqrt(a: Array<DoubleArray>): Array<DoubleArray>? {
        val n = a.size
        var y = Array(n) { i -> a[i].clone() }
        var z = identityMatrix(n)
        for (iter in 0 until 20) {
            val invY = inverse(y) ?: return null
            val invZ = inverse(z) ?: return null
            val nextY = multiplyScalar(add(y, invZ)!!, 0.5)
            val nextZ = multiplyScalar(add(z, invY)!!, 0.5)
            if (norm(subtract(y, nextY)!!) < 1e-10) return nextY
            y = nextY; z = nextZ
        }
        return y
    }

    fun matrixLog(a: Array<DoubleArray>): Array<DoubleArray>? {
        val n = a.size
        var s = 0
        var x = Array(n) { i -> a[i].clone() }
        val I = identityMatrix(n)
        while (norm(subtract(x, I)!!) > 0.1 && s < 15) {
            x = matrixSqrt(x) ?: return null
            s++
        }
        val z = subtract(x, I)!!
        var term = Array(n) { i -> z[i].clone() }
        var res = Array(n) { i -> z[i].clone() }
        for (k in 2..25) {
            term = multiply(term, z)!!
            val sign = if (k % 2 == 0) -1.0 else 1.0
            val addTerm = multiplyScalar(term, sign / k)
            res = add(res, addTerm)!!
            if (norm(addTerm) < 1e-12) break
        }
        return multiplyScalar(res, 2.0.pow(s))
    }

    fun matrixTrig(a: Array<DoubleArray>): Pair<Array<DoubleArray>, Array<DoubleArray>> {
        val n = a.size
        var s = 0
        var scaledA = Array(n) { i -> a[i].clone() }
        while (norm(scaledA) > 0.5) { scaledA = multiplyScalar(scaledA, 0.5); s++ }

        var sinA = Array(n) { DoubleArray(n) }
        var cosA = identityMatrix(n)
        var term = Array(n) { i -> scaledA[i].clone() }
        sinA = add(sinA, term)!!

        for (k in 2..15) {
            term = multiply(term, scaledA)!!
            var fact = 1.0; for (f in 2..k) fact *= f
            val sign = if (k % 4 == 0 || k % 4 == 1) 1.0 else -1.0
            val addTerm = multiplyScalar(term, sign / fact)
            if (k % 2 == 0) cosA = add(cosA, addTerm)!! else sinA = add(sinA, addTerm)!!
        }

        val I = identityMatrix(n)
        for (i in 0 until s) {
            val nextSin = multiplyScalar(multiply(sinA, cosA)!!, 2.0)
            val nextCos = subtract(multiplyScalar(multiply(cosA, cosA)!!, 2.0), I)!!
            sinA = nextSin; cosA = nextCos
        }
        return Pair(sinA, cosA)
    }

    fun matrixExponential(a: Array<DoubleArray>): Array<DoubleArray>? {
        val n = a.size
        var scaledA = Array(n) { i -> a[i].clone() }; var s = 0
        while (norm(scaledA) > 1.0) { scaledA = multiplyScalar(scaledA, 0.5); s++ }
        var res = identityMatrix(n); var term = identityMatrix(n)
        for (k in 1..15) {
            term = multiply(term, scaledA)!!
            term = multiplyScalar(term, 1.0 / k.toDouble())
            res = add(res, term)!!
        }
        for (i in 0 until s) res = multiply(res, res)!!
        return res
    }

    // --- КРИТЕРИИ КАЛМАНА (ТАУ) ---
    fun controllability(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        val n = a.size
        val wc = Array(n) { DoubleArray(n * n) }
        var current = Array(n) { i -> b[i].clone() }
        for (k in 0 until n) {
            for (i in 0 until n) for (j in 0 until n) wc[i][k * n + j] = current[i][j]
            if (k < n - 1) current = multiply(a, current)!!
        }
        return wc
    }

    fun observability(a: Array<DoubleArray>, c: Array<DoubleArray>): Array<DoubleArray> {
        val n = a.size
        val wo = Array(n * n) { DoubleArray(n) }
        var current = Array(n) { i -> c[i].clone() }
        for (k in 0 until n) {
            for (i in 0 until n) for (j in 0 until n) wo[k * n + i][j] = current[i][j]
            if (k < n - 1) current = multiply(current, a)!!
        }
        return wo
    }

    // --- СОБСТВЕННЫЕ ЧИСЛА И ВЕКТОРЫ ---
    fun eigenvalues(matrix: Array<DoubleArray>): List<Complex> {
        val n = matrix.size; if (n == 0) return emptyList(); if (n == 1) return listOf(Complex(matrix[0][0]))
        val a = Array(n) { i -> matrix[i].clone() }
        var converged = false; var iterB = 0
        while (!converged && iterB < 50) {
            iterB++; converged = true
            for (i in 0 until n) {
                var cSum = 0.0; var rSum = 0.0
                for (j in 0 until n) if (j != i) { cSum += abs(a[j][i]); rSum += abs(a[i][j]) }
                if (cSum > 1e-10 && rSum > 1e-10) {
                    var f = 1.0; val s = cSum + rSum
                    while (cSum < rSum / 2.0) { cSum *= 2.0; f *= 2.0 }
                    while (cSum >= rSum * 2.0) { cSum /= 2.0; f /= 2.0 }
                    if ((cSum + rSum) / f < 0.95 * s) { converged = false; val g = 1.0 / f; for (j in 0 until n) a[i][j] *= g; for (j in 0 until n) a[j][i] *= f }
                }
            }
        }
        for (k in 0 until n - 2) {
            var maxIdx = k + 1; for (i in k + 2 until n) if (abs(a[i][k]) > abs(a[maxIdx][k])) maxIdx = i
            if (abs(a[maxIdx][k]) > 1e-12) {
                if (maxIdx != k + 1) { val rowTmp = a[maxIdx]; a[maxIdx] = a[k + 1]; a[k + 1] = rowTmp; for (c in 0 until n) { val colTmp = a[c][maxIdx]; a[c][maxIdx] = a[c][k + 1]; a[c][k + 1] = colTmp } }
                for (i in k + 2 until n) { val factor = a[i][k] / a[k + 1][k]; for (j in k until n) a[i][j] -= factor * a[k + 1][j]; for (j in 0 until n) a[j][k + 1] += factor * a[j][i] }
            }
        }

        val result = mutableListOf<Complex>(); var currentN = n; var maxIter = 100 * n
        while (currentN > 1 && maxIter > 0) {
            maxIter--; val last = currentN - 1
            if (abs(a[last][last - 1]) <= 1e-10 * (abs(a[last - 1][last - 1]) + abs(a[last][last]) + 1e-12)) { result.add(Complex(a[last][last])); currentN--; continue }
            if (currentN == 2 || abs(a[last - 1][last - 2]) <= 1e-10 * (abs(a[last - 2][last - 2]) + abs(a[last - 1][last - 1]) + 1e-12)) {
                val tr2 = a[last - 1][last - 1] + a[last][last]; val det2 = a[last - 1][last - 1] * a[last][last] - a[last - 1][last] * a[last][last - 1]
                val disc = tr2 * tr2 - 4.0 * det2; val cleanDisc = if (abs(disc) < 1e-9) 0.0 else disc
                if (cleanDisc >= 0) { val sq = sqrt(cleanDisc); result.add(Complex((tr2 + sq) / 2.0)); result.add(Complex((tr2 - sq) / 2.0)) }
                else { val im = sqrt(-cleanDisc) / 2.0; result.add(Complex(tr2 / 2.0, im)); result.add(Complex(tr2 / 2.0, -im)) }
                currentN -= 2; continue
            }
            val tr = a[last - 1][last - 1] + a[last][last]; val det2 = a[last - 1][last - 1] * a[last][last] - a[last - 1][last] * a[last][last - 1]
            val disc = tr * tr - 4.0 * det2; val shift = if (disc >= 0) { val s1 = (tr + sqrt(disc)) / 2.0; val s2 = (tr - sqrt(disc)) / 2.0; if (abs(s1 - a[last][last]) < abs(s2 - a[last][last])) s1 else s2 } else { tr / 2.0 }
            for (i in 0 until currentN) a[i][i] -= shift
            val cosL = DoubleArray(currentN - 1); val sinL = DoubleArray(currentN - 1)
            for (i in 0 until currentN - 1) {
                val r = hypot(a[i][i], a[i + 1][i])
                if (r > 1e-14) {
                    cosL[i] = a[i][i] / r; sinL[i] = -a[i + 1][i] / r
                    for (j in i until currentN) { val t1 = a[i][j]; val t2 = a[i + 1][j]; a[i][j] = cosL[i] * t1 - sinL[i] * t2; a[i + 1][j] = sinL[i] * t1 + cosL[i] * t2 }
                } else { cosL[i] = 1.0; sinL[i] = 0.0 }
            }
            for (i in 0 until currentN - 1) {
                for (j in 0..min(i + 2, currentN - 1)) { val t1 = a[j][i]; val t2 = a[j][i + 1]; a[j][i] = cosL[i] * t1 - sinL[i] * t2; a[j][i + 1] = sinL[i] * t1 + cosL[i] * t2 }
            }
            for (i in 0 until currentN) a[i][i] += shift
        }
        if (currentN == 1) result.add(Complex(a[0][0]))
        while (result.size < n) { val idx = n - 1 - result.size; result.add(Complex(a[idx][idx])) }
        return result.map { c -> Complex(if (c.re.isNaN() || c.re.isInfinite()) 0.0 else c.re, if (c.im.isNaN() || c.im.isInfinite()) 0.0 else c.im) }.sortedByDescending { it.abs() }
    }

    fun eigenvectors(matrix: Array<DoubleArray>, eigenvalues: List<Complex>): List<List<Complex>> {
        val n = matrix.size; val vectors = mutableListOf<List<Complex>>()
        for (lambda in eigenvalues) {
            val mat = Array(n) { i -> Array(n) { j -> if (i == j) Complex(matrix[i][j]) - lambda else Complex(matrix[i][j]) } }
            for (i in 0 until n - 1) {
                var maxR = i; for (k in i + 1 until n) if (mat[k][i].abs() > mat[maxR][i].abs()) maxR = k
                val tmp = mat[i]; mat[i] = mat[maxR]; mat[maxR] = tmp
                for (k in i + 1 until n) if (mat[i][i].abs() > 1e-10) { val factor = mat[k][i] / mat[i][i]; for (j in i until n) mat[k][j] = mat[k][j] - factor * mat[i][j] }
            }
            val v = Array(n) { Complex(0.0) }; v[n - 1] = Complex(1.0)
            for (i in n - 2 downTo 0) {
                var sum = Complex(0.0); for (j in i + 1 until n) sum = sum + (mat[i][j] * v[j])
                v[i] = if (mat[i][i].abs() > 1e-9) -sum / mat[i][i] else Complex(1.0)
            }
            var normSq = 0.0; for (x in v) normSq += x.re * x.re + x.im * x.im; val norm = sqrt(normSq)
            val unitVector = if (norm > 1e-12 && !norm.isNaN()) v.map { it / Complex(norm) } else v.toList(); vectors.add(unitVector)
        }
        return vectors
    }

    // --- ЖНФ И РЖНФ ---
    private data class EigenCluster(var re: Double, var im: Double, var count: Int)
    private fun clusterEigenvalues(eigs: List<Complex>, tol: Double = 1e-3): List<EigenCluster> {
        val clusters = mutableListOf<EigenCluster>()
        for (e in eigs) {
            var found = false
            for (c in clusters) {
                if (abs(e.re - c.re) < tol && abs(e.im - c.im) < tol) { c.re = (c.re * c.count + e.re) / (c.count + 1); c.im = (c.im * c.count + e.im) / (c.count + 1); c.count++; found = true; break }
            }
            if (!found) clusters.add(EigenCluster(e.re, e.im, 1))
        }
        return clusters
    }
    private fun multiplyComplexMat(aRe: Array<DoubleArray>, aIm: Array<DoubleArray>, bRe: Array<DoubleArray>, bIm: Array<DoubleArray>): Pair<Array<DoubleArray>, Array<DoubleArray>> {
        val n = aRe.size; val resRe = Array(n) { DoubleArray(n) }; val resIm = Array(n) { DoubleArray(n) }
        for (i in 0 until n) for (j in 0 until n) { var sr = 0.0; var si = 0.0; for (k in 0 until n) { sr += aRe[i][k] * bRe[k][j] - aIm[i][k] * bIm[k][j]; si += aRe[i][k] * bIm[k][j] + aIm[i][k] * bRe[k][j] }; resRe[i][j] = sr; resIm[i][j] = si }
        return Pair(resRe, resIm)
    }
    private fun rankWithTolerance(matrix: Array<DoubleArray>, tol: Double = 1e-9): Int {
        val r = matrix.size; if (r == 0) return 0; val c = matrix[0].size; val a = Array(r) { i -> matrix[i].clone() }; var row = 0
        for (col in 0 until c) {
            if (row >= r) break; var maxRow = row; var maxVal = abs(a[row][col])
            for (i in row + 1 until r) { val v = abs(a[i][col]); if (v > maxVal) { maxVal = v; maxRow = i } }
            if (maxVal < tol) continue
            if (maxRow != row) { val tmp = a[row]; a[row] = a[maxRow]; a[maxRow] = tmp }
            for (i in row + 1 until r) { val factor = a[i][col] / a[row][col]; a[i][col] = 0.0; for (j in col + 1 until c) a[i][j] -= factor * a[row][j] }
            row++
        }
        return row
    }
    private fun rankComplexMat(mRe: Array<DoubleArray>, mIm: Array<DoubleArray>): Int {
        val n = mRe.size; val realMat = Array(2 * n) { DoubleArray(2 * n) }
        for (i in 0 until n) for (j in 0 until n) { realMat[i][j] = mRe[i][j]; realMat[i][j + n] = -mIm[i][j]; realMat[i + n][j] = mIm[i][j]; realMat[i + n][j + n] = mRe[i][j] }
        return rankWithTolerance(realMat, 1e-4) / 2
    }
    private fun getJordanBlocks(matrix: Array<DoubleArray>, lambdaRe: Double, lambdaIm: Double, maxAlgMultiplicity: Int): List<Int> {
        val n = matrix.size; val nRe = Array(n) { i -> DoubleArray(n) { j -> if (i == j) matrix[i][j] - lambdaRe else matrix[i][j] } }; val nIm = Array(n) { i -> DoubleArray(n) { j -> if (i == j) -lambdaIm else 0.0 } }
        val ranks = mutableListOf(n); ranks.add(rankComplexMat(nRe, nIm))
        var currentPowerRe = nRe; var currentPowerIm = nIm; var k = 1
        while (ranks[k] < ranks[k - 1] && (n - ranks[k]) < maxAlgMultiplicity) {
            val nextPower = multiplyComplexMat(currentPowerRe, currentPowerIm, nRe, nIm); currentPowerRe = nextPower.first; currentPowerIm = nextPower.second; ranks.add(rankComplexMat(currentPowerRe, currentPowerIm)); k++
        }
        while (ranks.size <= n + 1) ranks.add(ranks.last()); val blocks = mutableListOf<Int>()
        for (i in 1..n) { val count = ranks[i - 1] - 2 * ranks[i] + ranks[i + 1]; for (c in 0 until count) blocks.add(i) }
        var blocksSum = blocks.sum(); while (blocksSum < maxAlgMultiplicity) { blocks.add(1); blocksSum++ }
        if (blocksSum > maxAlgMultiplicity) { var currentSum = 0; val trimmedBlocks = mutableListOf<Int>(); for (b in blocks) { if (currentSum + b <= maxAlgMultiplicity) { trimmedBlocks.add(b); currentSum += b } else { trimmedBlocks.add(maxAlgMultiplicity - currentSum); break } }; return trimmedBlocks.sortedDescending() }
        return blocks.sortedDescending()
    }
    private fun formatJnfNum(v: Double): String { val clean = if (abs(v) < 1e-9) 0.0 else v; val rounded = Math.round(clean * 1000.0) / 1000.0; return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else String.format(Locale.US, "%.3f", rounded).trimEnd('0').trimEnd('.') }
    private fun formatJnfNumC(re: Double, im: Double): String { val reClean = if (abs(re) < 1e-4) 0.0 else re; val imClean = if (abs(im) < 1e-4) 0.0 else im; val reStr = formatJnfNum(reClean); val imStr = formatJnfNum(abs(imClean)); if (imClean == 0.0) return reStr; if (reClean == 0.0) return if (imClean > 0) "${imStr}i" else "-${imStr}i"; return if (imClean > 0) "$reStr + ${imStr}i" else "$reStr - ${imStr}i" }

    fun jordanForm(matrix: Array<DoubleArray>): Pair<String, List<List<String>>> {
        val n = matrix.size; if (n == 0) return Pair("Матрица пуста", emptyList())
        return try {
            val rawEig = eigenvalues(matrix); val clusters = clusterEigenvalues(rawEig); val jnf = Array(n) { Array(n) { "0" } }; var pos = 0; var hasMultipleRoots = false
            for (cluster in clusters) {
                if (cluster.count > 1) hasMultipleRoots = true; val blocks = getJordanBlocks(matrix, cluster.re, cluster.im, cluster.count)
                for (blockSize in blocks) {
                    for (i in 0 until blockSize) { val current = pos + i; if (current < n) { jnf[current][current] = formatJnfNumC(cluster.re, cluster.im); if (i < blockSize - 1 && current + 1 < n) jnf[current][current + 1] = "1" } }
                    pos += blockSize
                }
            }
            while (pos < n) { jnf[pos][pos] = formatJnfNumC(rawEig[pos].re, rawEig[pos].im); pos++ }
            Pair(if (!hasMultipleRoots) "Диагональный вид (Простой спектр):" else "Комплексная ЖНФ:", jnf.map { it.toList() })
        } catch (e: Exception) { Pair("Ошибка вычисления ЖНФ", emptyList()) }
    }

    fun realJordanForm(matrix: Array<DoubleArray>): Pair<String, List<List<String>>> {
        val n = matrix.size; if (n == 0) return Pair("Матрица пуста", emptyList())
        return try {
            val rawEig = eigenvalues(matrix); val clusters = clusterEigenvalues(rawEig); val jnf = Array(n) { Array(n) { "0" } }; var pos = 0; val processed = BooleanArray(clusters.size)
            for (i in clusters.indices) {
                if (processed[i]) continue; val c = clusters[i]
                if (abs(c.im) < 1e-3) {
                    val blocks = getJordanBlocks(matrix, c.re, 0.0, c.count)
                    for (blockSize in blocks) { for (k in 0 until blockSize) { if (pos + k < n) { jnf[pos + k][pos + k] = formatJnfNum(c.re); if (k < blockSize - 1 && pos + k + 1 < n) jnf[pos + k][pos + k + 1] = "1" } }; pos += blockSize }
                    processed[i] = true
                } else {
                    var conjIdx = -1; for (j in i + 1 until clusters.size) { if (!processed[j] && abs(clusters[j].re - c.re) < 1e-3 && abs(clusters[j].im + c.im) < 1e-3) { conjIdx = j; break } }
                    val blocks = getJordanBlocks(matrix, c.re, c.im, c.count); val a = c.re; val b = abs(c.im)
                    for (blockSize in blocks) {
                        for (k in 0 until blockSize) {
                            val cur = pos + 2 * k
                            if (cur + 1 < n) {
                                jnf[cur][cur] = formatJnfNum(a); jnf[cur][cur + 1] = formatJnfNum(b); jnf[cur + 1][cur] = formatJnfNum(-b); jnf[cur + 1][cur + 1] = formatJnfNum(a)
                                if (k < blockSize - 1 && cur + 3 < n) { jnf[cur][cur + 2] = "1"; jnf[cur + 1][cur + 3] = "1" }
                            } else if (cur < n) jnf[cur][cur] = formatJnfNum(a)
                        }
                        pos += 2 * blockSize
                    }
                    processed[i] = true; if (conjIdx != -1) processed[conjIdx] = true
                }
            }
            Pair("Вещественная Жорданова форма (РЖНФ):", jnf.map { it.toList() })
        } catch (e: Exception) { Pair("Ошибка вычисления РЖНФ", emptyList()) }
    }
}