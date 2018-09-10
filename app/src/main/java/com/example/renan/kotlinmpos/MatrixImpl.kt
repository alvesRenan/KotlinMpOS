package com.example.renan.kotlinmpos

import java.util.*

class MatrixImpl : Matrix{

    override fun random(m: Int, n: Int): Array<DoubleArray> {
        val C: Array<DoubleArray> = Array(m) { DoubleArray(n) }

        for (i in 0 until m)
            for (j in 0 until n)
                C[i][j] = Math.random()

        return C
    }

    fun random(m: Int, n: Int, max: Int): Array<DoubleArray> {
        val C: Array<DoubleArray> = Array(m) { DoubleArray(n) }

        for (i in 0 until m)
            for (j in 0 until n)
                C[i][j] = Math.random() * (Random().nextInt(max) + 1)

        return C
    }

    // return C = A^T
    fun transpose(A: Array<DoubleArray>): Array<DoubleArray> {
        val m = A.size
        val n = A[0].size
        val C: Array<DoubleArray> = Array(n) { DoubleArray(m) }

        for (i in 0 until m)
            for (j in 0 until n)
                C[i][j] = A[i][j]

        return C
    }

    // return C = A + B
    override fun add(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray> {
        val m = A.size
        val n = A[0].size
        val C: Array<DoubleArray> = Array(m) { DoubleArray(n) }

        for (i in 0 until m)
            for (j in 0 until n)
                C[i][j] = A[i][j] + B[i][j]

        return C
    }

    // return C = A * B
    override fun multiply(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray> {
        val mA = A.size
        val nA = A[0].size
        val mB = B.size
        val nB = B[0].size

        if (nA != nB)
            throw RuntimeException("Illegal matrixTest dimensions")

        val C: Array<DoubleArray> = Array(mA) { DoubleArray(nB) }

        for (i in 0 until mA)
            for (j in 0 until nB)
                for (k in 0 until nA)
                    C[i][j] += (A[i][k] * B[k][j])

        return C
    }

}