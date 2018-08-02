package com.example.renan.kotlinmpos

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

import br.ufc.mdcc.mpos.MposFramework
import br.ufc.mdcc.mpos.config.Inject
import br.ufc.mdcc.mpos.config.MposConfig
import br.ufc.mdcc.mpos.config.ProfileNetwork

import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

@SuppressWarnings("depreciation")
@MposConfig(endpointSecondary = "210.0.2.2", profile = ProfileNetwork.LIGHT)
class MainActivity : AppCompatActivity() {

    @Inject(MatrixImpl::class)
    private lateinit var matrix: Matrix

    private val numbers = arrayOf("100", "200", "300", "400", "500", "600", "700", "800", "900", "1000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        matrix = MatrixImpl()

        // numberPicker configuration
        numPicker.minValue = 1
        numPicker.maxValue = numbers.size
        numPicker.displayedValues = numbers
        numPicker.value = 1

        // get matrix dimension and operation through intent
        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("cloudlet")) {
                val cloudlet = extras.getString("cloudlet")
                Log.d(this.packageName, "Extras: cloudlet = $cloudlet")

                MposFramework.getInstance().start(this, cloudlet)
            }

            if (extras.containsKey("size")  && extras.containsKey("operation")) {
                val size = extras.getInt("size")
                val operation = extras.getString("operation")

                if (operation.equals("mul", ignoreCase = true)) setMulOperation()
                if (operation.equals("add", ignoreCase = true)) setAddOperation()

                computeBtn.performClick()

                Log.d(this.packageName, "Extras: size = $size, operation = $operation")
            } else {
                Log.i(this.packageName, "No extras received.")
            }
        }

        // choose operation in the interface
        mulRadioBtn.setOnClickListener { setMulOperation() }
        addRadioBtn.setOnClickListener { setAddOperation() }

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            MposFramework.getInstance().stop()
        } catch (e: NullPointerException) {
            finish()
        }
    }

    /*
     * Execution Time option
     */
    @SuppressLint("SimpleDateFormat")
    fun setExecTime(totalTime: Long) {
        val date = Date(totalTime)
        val df = SimpleDateFormat("mm:ss.SSS")
        execTimeTxt.text = "Execution Time: ${df.format(date)}"
    }

    private fun resetExecTime() {
        execTimeTxt.text = "Execution Time: 00:00.000"
    }

    /*
     * Compute Button options
     */
    private fun disableButton() {
        computeBtn.text = "Calculating"
        computeBtn.isClickable = false
    }

    fun enableButton() {
        computeBtn.text = "Compute"
        computeBtn.isClickable = true
    }

    /*
     * Radio Button options
     */
//    public fun setAddOperation(view: View) {
//        addRadioBtn.isChecked = true
//        mulRadioBtn.isChecked = false
//    }

    private fun setAddOperation() {
        addRadioBtn.isChecked = true
        mulRadioBtn.isChecked = false
    }

    private fun setMulOperation() {
        addRadioBtn.isChecked = false
        mulRadioBtn.isChecked = true
    }

    private fun blockClickOnRadioButtons() {
        addRadioBtn.isClickable = false
        mulRadioBtn.isClickable = false
    }

    fun unblockClickOnRadioButtons() {
        addRadioBtn.isClickable = true
        mulRadioBtn.isClickable = true
    }

    /*
     * On Click event button
     */

    fun calc(view: View) {
        // Configure matrix dimension
        val n = numbers[numPicker.value - 1].toInt()
        Log.d(packageName, "Matrix dimension selected: $n")
        // n = 100 + (n * 100)
        // Log.d("DEBUG", "$n")

        // Get operation type
        val mustAdd = addRadioBtn.isChecked

        disableButton()
        blockClickOnRadioButtons()
        resetExecTime()

        CalcTask(n, mustAdd).execute()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class CalcTask(dimension: Int, b: Boolean) : AsyncTask<Void, String, Void>() {
        internal var dim: Int = 0
        internal var mustAdd: Boolean = false
        internal var initialTime: Long = 0
        internal var totalTime: Long = 0

        init {
            this.dim = dimension
            this.mustAdd = b
        }

        override fun doInBackground(vararg values: Void?): Void? {
            // Compute operation
            lateinit var res: Array<DoubleArray> // needed only for debugging purposes
            initialTime = System.currentTimeMillis()

            publishProgress("Creating random matrix and calculating...")
            val mat: Array<DoubleArray> = matrix.random(dim, dim)

            val init: Long = System.nanoTime()

            val op = if (mustAdd) {
                var res = matrix.add(mat, mat)
                "Add"
            } else {
                var res = matrix.multiply(mat, mat)
                "Mul"
            }
            val execTime = System.nanoTime() - init

            Log.d("Result", "Operation = $op, Dimension = $dim, Time = $execTime")
            totalTime = System.currentTimeMillis() - initialTime
            return null
        }

        override fun onProgressUpdate(vararg progress: String?) {
            Toast.makeText(applicationContext, progress[0], Toast.LENGTH_SHORT).show()
        }

        override fun onPostExecute(no: Void?) {
            enableButton()
            unblockClickOnRadioButtons()
            setExecTime(totalTime)
        }
    }

}
