package com.example.renan.kotlinmpos

import br.ufc.mdcc.mpos.offload.Remotable

interface Matrix {

    fun random(m :Int, n: Int): Array<DoubleArray>

    @Remotable(status = true)
    fun add(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray>

    @Remotable(status = true)
    fun multiply(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray>

}