package de.felix.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.operator.Operator
import java.lang.Integer.parseInt
import java.text.DecimalFormat
import java.util.IllegalFormatException

class MainActivity : AppCompatActivity() {

    // states
    private var isPolish = false
    private var isError = false
    private var hasResult = false

    // flags
    private var operatorUsed = false

    private var dotUsed = false
    private var dotLast = false

    private var specialTypeBrace = false
    private var specialTypeMult = false

    private var bracesOpen = 0
    private var bracesClosed = 0
    private var braceUsed = false
    private var bracesOpenUsed = false

    // inits
    private lateinit var expression: Expression
    private lateinit var input: CharSequence
    private lateinit var textViewCalculation: TextView
    private lateinit var textViewSubtotal: TextView
    private lateinit var button: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewCalculation = findViewById(R.id.textViewCalculation)
        textViewSubtotal = findViewById(R.id.textViewSubtotal)
    }


    // handler -------------------------------------------------------------
    fun numericButtonClickHandler(view: android.view.View) {
        input = getInput(view)

        // Replace initial zero with number after input
        if (textViewCalculation.text.toString() == "0") {
            textViewCalculation.text = input
        }
        // Replace Result after number input
        else if (hasResult || isError) {
            textViewCalculation.text = input
            hasResult = false
            isError = false
        } else {
            textViewCalculation.append(input)
        }

        operatorUsed = false
        dotLast = false
        braceUsed = false
        bracesOpenUsed = false
    }

    fun dotButtonClickHandler(view: android.view.View) {
        input = getInput(view)
        if (!(dotUsed || hasResult || isError)) {
            if (operatorUsed || bracesOpenUsed) {
                textViewCalculation.append("0.")
            } else {
                textViewCalculation.append(".")
            }

            dotLast = true
            operatorUsed = false
            braceUsed = false
            bracesOpenUsed = false
        }
    }

    fun braceButtonClickHandler(view: android.view.View) {
        if (!isError) {
            if (!bracesOpenUsed) {
                textViewCalculation.append("(")
                bracesOpen++
                braceUsed = true
                bracesOpenUsed = true
            } else if (bracesOpen - 1 > bracesClosed) {
                textViewCalculation.text = textViewCalculation.text.dropLast(1)
                bracesOpen--
                textViewCalculation.append(")")
                bracesClosed++
                braceUsed = true
                bracesOpenUsed = false
            }

            hasResult = false
        }
    }

    fun operatorButtonClickHandler(view: android.view.View) {
        if (!(dotLast || braceUsed || isError)) {
            input = getInput(view)

            if (!operatorUsed) {
                textViewCalculation.append(input)
                operatorUsed = true
            }

            hasResult = false
        }
    }

    fun functionButtonClickHandler(view: android.view.View) {
        lateinit var result: CharSequence
        input = getInput(view)

        when (input) {
            // Special Keys
            "AC" -> {
                textViewCalculation.text = "0"
                hasResult = false
            }
            "⌫" -> {
                if (textViewCalculation.text.length > 1) {
                    textViewCalculation.text = textViewCalculation.text.dropLast(1)
                } else {
                    textViewCalculation.text = "0"
                }
                hasResult = false
            }
            "=" -> {
                if (!(dotLast || operatorUsed || bracesOpenUsed)) {
                    result = if (isPolish) {
                        calculationPolish()
                    } else {
                        while (bracesOpen != bracesClosed) {
                            textViewCalculation.append(")")
                            bracesClosed++
                        }
                        calculation()
                    }
                    textViewCalculation.text = result

                    hasResult = true
                    specialTypeBrace = false
                    specialTypeMult = false
                    bracesOpen = 0
                    bracesClosed = 0
                    braceUsed = false
                    bracesOpenUsed = false
                }
            }
        }
    }

    fun specialButtonClickHandler(view: android.view.View) {
        lateinit var result: CharSequence
        input = getInput(view)


        isPolish = textViewCalculation.text.contains(" ")

        if (isNumeric(input)) {
            result = if (isPolish) {
                calculationPolish()
            } else {
                calculation()
            }
            textViewSubtotal.text = result
        }
    }














    private fun isNumeric(char: CharSequence): Boolean {
        return try {
            parseInt(char as String)
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun getInput(view: android.view.View): CharSequence {
        button = findViewById(view.id)
        return button.text
    }

    private fun textViewCalculationToString(): String {
        return textViewCalculation.text.toString().replace("×", "*").replace("÷", "/")
    }

    private fun syntaxCorrect(): Boolean {
        try {
            ExpressionBuilder(textViewCalculationToString()).build()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun calculation(): CharSequence {
        return try {
            val calculation: String = textViewCalculationToString()
            val expression: Expression = ExpressionBuilder(calculation).build()
            val format = DecimalFormat("0.#")
            format.format(expression.evaluate()).toString().replace(",", ".")
        } catch (e: Exception) {
            "Syntax-Error"
        }
    }

    private fun calculationPolish(): CharSequence {
        val calculation: String = textViewCalculationToString()
        val expression: Expression = ExpressionBuilder(calculation).build()

        return "asd"
    }
}