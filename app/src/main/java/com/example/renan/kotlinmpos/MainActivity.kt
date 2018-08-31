package com.example.renan.kotlinmpos

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

import br.ufc.mdcc.mpos.MposFramework
import br.ufc.mdcc.mpos.config.Inject
import br.ufc.mdcc.mpos.config.MposConfig
import br.ufc.mdcc.mpos.config.ProfileNetwork

import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat", "SetTextI18n")
@SuppressWarnings("depreciation")
@MposConfig(endpointSecondary = "210.0.2.2", profile = ProfileNetwork.LIGHT)
class MainActivity : AppCompatActivity() {

    private var extraSize: Int = 0
    private lateinit var extraOperation: String

    private var numberOfErrors = 0

    @Inject(MatrixImpl::class) private lateinit var matrix: Matrix

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

            if (extras.containsKey("size") && extras.containsKey("operation")) {
                extraSize = extras.getInt("size")
                extraOperation = extras.getString("operation")

                if (extraOperation.equals("mul", ignoreCase = true)) setMulOperation()
                if (extraOperation.equals("add", ignoreCase = true)) setAddOperation()

                computeBtn.performClick()

                Log.d(this.packageName, "Extras: size = $extraSize, operation = $extraOperation")
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

    private fun setExecTime(totalTime: Long, operation: String?) {
        operation?.let {
            val date = Date(totalTime)
            val df = SimpleDateFormat("mm:ss.SSS")
            execTimeTxt.text = "Execution Time: ${df.format(date)}"
        } ?: computeBtn.performClick()
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

    private fun enableButton() {
        computeBtn.text = "Compute"
        computeBtn.isClickable = true
    }

    /*
     * Radio Button options
     */
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

    private fun unblockClickOnRadioButtons() {
        addRadioBtn.isClickable = true
        mulRadioBtn.isClickable = true
    }

    /*
     * On Click event button
     */

    fun calc(view: View) {
        // Configure matrix dimension
        val sizeSelected = if (extraSize == 0) {
            numbers[numPicker.value - 1].toInt()
        } else {
            extraSize
        }

        // Get operation type
        val mustAdd = addRadioBtn.isChecked

        disableButton()
        blockClickOnRadioButtons()
        resetExecTime()

        doAsync {
            val initialTime = System.currentTimeMillis()

            uiThread {
                toast("Creating random matrix and calculating...")
            }
            val mat = matrix.random(sizeSelected, sizeSelected)

            val init = System.nanoTime()
            val operation = try {
                if (mustAdd) {
                    matrix.add(mat, mat)
                    "Add"
                } else {
                    matrix.multiply(mat, mat)
                    "Mul"
                }
            } catch (e: NullPointerException) {
                Log.e("MATRIX_CRASH", "${e.printStackTrace()}")
                numberOfErrors += 1
                null
            }

            val execTime = System.nanoTime() - init
            val totalTime = System.currentTimeMillis() - initialTime

            Log.d("Result", "Operation = $operation, Dimension = $sizeSelected, Time = $execTime")

            // if operation is null, then log the number of errors
            operation ?: Log.e("An Error Occurred!", "Total number so  far is: $numberOfErrors")

            uiThread {
                enableButton()
                unblockClickOnRadioButtons()
                setExecTime(totalTime, operation)
            }
        }
    }
}
