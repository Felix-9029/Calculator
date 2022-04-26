package de.felix.calculator

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt
import java.text.DecimalFormat

/**
 * @author <p>Felix Reichert</p>
 * <p>Matrikelnummer: 19019</p>
 * <p>Package: de.felix.calculator</p>
 * <p>Datei: MainActivity.kt</p>
 * <p>Datum: 20.04.2022</p>
 * <p>Version: 1</p>
 */

class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    // states
    private var isPolish = false
    private var isError = false
    private var hasResult = false

    private var bracesOpen = 0
    private var bracesClosed = 0

    // inits
    private lateinit var result: CharSequence
    private lateinit var input: CharSequence
    private lateinit var textViewCalculation: TextView
    private lateinit var textViewSubtotal: TextView
    private lateinit var button: Button

    /**
     * <p>Initialize on startup</p>
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewCalculation = findViewById(R.id.textViewCalculation)
        textViewSubtotal = findViewById(R.id.textViewSubtotal)
    }

    /**
     * Popup-Menu of special operators in normal view</p>
     */
    fun showOperatorPopup(view: View) {
        val popup = PopupMenu(this, view)
        popup.setOnMenuItemClickListener(this)
        popup.inflate(R.menu.operator_popup_menu)
        popup.show()
    }

    /**
     * <p>Click-Handler for popup-items</p>
     */
    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.itemSin -> {
                input = "sin"
                specialOperatorInput()
                true
            }
            R.id.itemCos -> {
                input = "cos"
                specialOperatorInput()
                true
            }
            R.id.itemTan -> {
                input = "tan"
                specialOperatorInput()
                true
            }
            R.id.itemSqrt -> {
                input = "√"
                specialOperatorInput()
                true
            }
            else -> {
                false
            }
        }
    }

    // handler -------------------------------------------------------------

    /**
     * <p>Click-Handler for numeric buttons</p>
     */
    fun numericButtonClickHandler(view: View) {
        input = getInput(view)
        numericInput()
    }

    /**
     * <p>Logic for appending clicked numbers to textview</p>
     */
    private fun numericInput() {
        errorHandler()

        // Replace Result after number input
        if (hasResult) {
            textViewCalculation.text = input
            hasResult = false
            isError = false
        }
        // Replace initial zero with number after input
        else if (textViewCalculation.text.toString() == "0") {
            textViewCalculation.text = input
        } else {
            if (orientationCheckLandscape()) {
                if (textViewCalculationLastChar() == "e" || textViewCalculationLastChar() == "π") {
                    textViewCalculation.append("×")
                }
                textViewCalculation.append(input)
                textViewSubtotal.text = calculation()
            }
        }
    }

    /**
     * <p>Click-Handler for dot button</p>
     */
    fun dotButtonClickHandler(view: View) {
        dotButtonInput()
    }

    /**
     * <p>Method to append a dot to textview</p>
     */
    private fun dotButtonInput() {
        errorHandler()

        if (orientationCheckLandscape()) {
            if (!isNumeric(textViewCalculationLastChar()) && !textViewCalculationLastNumber().contains(".")) {
                if (textViewCalculationLastChar() == "e" || textViewCalculationLastChar() == "π") {
                    textViewCalculation.append("×")
                }
                textViewCalculation.append("0.")
            } else if (!textViewCalculationLastNumber().contains(".") && isNumeric(textViewCalculationLastChar()) && textViewCalculationLastChar() != "e" && textViewCalculationLastChar() != "π") {
                textViewCalculation.append(".")
                hasResult = false
            }
            textViewSubtotal.text = calculation()
        }
    }

    /**
     * <p>Click-Handler for brace button</p>
     */
    fun braceButtonClickHandler(view: View) {
        braceButtonInput()
    }

    /**
     * <p>Method to append a brace to textview</p>
     */
    private fun braceButtonInput() {
        errorHandler()

        if (orientationCheckLandscape() && textViewCalculationLastChar() != "(") {
            textViewCalculation.append("(")
            bracesOpen++
        } else if (bracesOpen - 1 > bracesClosed && (isNumeric(textViewCalculationSecondLastChar()) || textViewCalculationSecondLastChar() == ")")) {
            textViewCalculation.text = textViewCalculation.text.dropLast(1)
            bracesOpen--
            textViewCalculation.append(")")
            bracesClosed++
        }
        hasResult = false
    }

    /**
     * <p>Click-Handler for operator buttons</p>
     */
    fun operatorButtonClickHandler(view: View) {
        input = getInput(view)
        operatorButtonInput()
    }

    /**
     * <p>Method to append value of an operator button to textview</p>
     */
    private fun operatorButtonInput() {
        errorHandler()

        if (hasResult) {
            textViewCalculation.text = "ANS"
            textViewCalculation.append(input)
        }
        if (orientationCheckLandscape()) {
            if (textViewCalculation.text.toString() == "0" && input == "-") {
                textViewCalculation.text = input
            }
            else if (isNumeric(textViewCalculationLastChar()) || textViewCalculationLastChar() == ")" || textViewCalculationLastChar() == "n" || textViewCalculationLastChar() == "s") {
                textViewCalculation.append(input)
            }
            else if (textViewCalculationLastChar() == "(" && input == "-") {
                textViewCalculation.append(input)
            }
            hasResult = false
        }
    }

    /**
     * <p>Click-Handler for special operator buttons</p>
     */
    fun specialOperatorButtonClickHandler(view: View) {
        input = getInput(view)
        specialOperatorInput()
    }

    /**
     * <p>Method to append value of a special operator button to textview</p>
     */
    private fun specialOperatorInput() {
        errorHandler()

        if (hasResult) {
            textViewCalculation.text = "ANS×"
            if (input == "x²" || input == "xⁿ") {
                textViewCalculation.text = textViewCalculation.text.dropLast(1)
            }
            textViewCalculation.append(specialOperatorButtonValueToString())
        } else {
            if (textViewCalculation.text.toString() == "0" && input != "x²" && input != "xⁿ") {
                textViewCalculation.text = specialOperatorButtonValueToString()
            } else {
                if (orientationCheckLandscape()) {
                    if (!(input == "x²" || input == "xⁿ")) {
                        if (isNumeric(textViewCalculationLastChar())) {
                            textViewCalculation.append("×")
                            textViewCalculation.append(specialOperatorButtonValueToString())
                        } else if (!isNumeric(textViewCalculationLastChar()) && textViewCalculationLastChar() != ".") {
                            textViewCalculation.append(specialOperatorButtonValueToString())
                        }
                    }
                    else if ((input == "x²" || input == "xⁿ") && (isNumeric(textViewCalculationLastChar()) || textViewCalculationLastChar() == ")")) {
                        textViewCalculation.append(specialOperatorButtonValueToString())
                    }
                }
            }
        }
        if (input == "x²") {
            textViewSubtotal.text = calculation()
        }
        hasResult = false

    }

    /**
     * <p>Method to convert text of special operator buttons to string with correct expression-syntax</p>
     */
    private fun specialOperatorButtonValueToString(): CharSequence {
        when (input) {
            "sin" -> {
                bracesOpen++
                return "sin("
            }
            "cos" -> {
                bracesOpen++
                return "cos("
            }
            "tan" -> {
                bracesOpen++
                return "tan("
            }
            "√" -> {
                bracesOpen++
                return "sqrt("
            }
            "x²" -> {
                bracesOpen++
                bracesClosed++
                return "^(2)"
            }
            "xⁿ" -> {
                bracesOpen++
                return "^("
            }
        }
        return ""
    }

    /**
     * <p>Click-Handler for special buttons</p>
     */
    fun specialButtonClickHandler(view: View) {
        input = getInput(view)
        specialButtonInput()
    }

    // TODO
    /**
     * <p>Method to append value of a special button to textview</p>
     */
    private fun specialButtonInput() {
        /*
        errorHandler()

        when (input) {
            " " -> textViewCalculation.append(input)
        }

        isPolish = textViewCalculation.text.contains(" ")
        */
    }

    /**
     * <p>Click-Handler for function buttons</p>
     */
    fun functionButtonClickHandler(view: View) {
        input = getInput(view)
        functionButtonInput()
    }

    /**
     * <p>Method to execute special functions</p>
     */
    private fun functionButtonInput() {
        when (input) {
            // Special Keys
            "AC" -> {
                textViewCalculation.text = "0"
                result = "0"
                bracesOpen = 0
                bracesClosed = 0
                textViewSubtotal.text = calculation()
                hasResult = false
                isError = false
            }
            "⌫" -> {
                if (isError ||
                    (textViewCalculation.text.length == 3 && textViewCalculationLastChar() == "S") ||
                    (textViewCalculation.text.length == 4 && textViewCalculationLastChar() == "(" && (textViewCalculationSecondLastChar() == "s" || textViewCalculationSecondLastChar() == "n")) ||
                    (textViewCalculation.text.length == 5 && textViewCalculationLastChar() == "(" && textViewCalculationSecondLastChar() == "t")
                ) {

                    bracesOpen = 0
                    bracesClosed = 0
                    textViewCalculation.text = "0"
                    isError = false
                } else if (textViewCalculation.text.length > 1) {
                    when (textViewCalculationLastChar()) {
                        "(" -> {
                            if (textViewCalculationSecondLastChar() == "^") {
                                textViewCalculation.text = textViewCalculation.text.dropLast(1)
                            } else if (textViewCalculationSecondLastChar() == "n" || textViewCalculationSecondLastChar() == "s") {
                                textViewCalculation.text = textViewCalculation.text.dropLast(3)
                            } else if (textViewCalculationSecondLastChar() == "t") {
                                textViewCalculation.text = textViewCalculation.text.dropLast(4)
                            }
                            bracesOpen--
                        }
                        ")" -> bracesClosed--
                    }
                    textViewCalculation.text = textViewCalculation.text.dropLast(1)
                } else {
                    bracesOpen = 0
                    bracesClosed = 0
                    textViewCalculation.text = "0"
                    isError = false
                }

                if (isNumeric(textViewCalculationLastChar())) {
                    textViewSubtotal.text = calculation()
                }
                hasResult = false
            }
            "=" -> {
                if (isNumeric(textViewCalculationLastChar()) || textViewCalculationLastChar() == "." || textViewCalculationLastChar() == ")") {
                    result = if (isPolish) {
                        calculationPolish()
                    } else {
                        calculation()
                    }
                    if (result == "Syntax-Error") {
                        isError = true
                    }
                    textViewCalculation.text = result

                    hasResult = true
                    bracesOpen = 0
                    bracesClosed = 0
                }
            }
        }
    }

    /**
     * <p>Method to reset textview after Syntax-Error</p>
     */
    private fun errorHandler() {
        if (isError) {
            textViewCalculation.text = "0"
            textViewSubtotal.text = calculation()
        }
    }

    /**
     * <p>Method to chack phone orientation</p>
     */
    private fun orientationCheckLandscape(): Boolean {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (textViewCalculation.text.length >= 13) {
                throwToast("You aren't allowed to enter more than 13 characters!")
                return false
            }
        }
        return true
    }

    /**
     * <p>Method to check if value is numeric</p>
     */
    private fun isNumeric(char: CharSequence): Boolean {
        return try {
            if (!(char == "π" || char == "e")) {
                parseInt(char as String)
            }
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * <p>Method to get the text from the clicked button</p>
     */
    private fun getInput(view: View): CharSequence {
        button = findViewById(view.id)
        return button.text
    }

    private fun textViewToString(textView: TextView): String {
        return textView.text.toString().replace("×", "*").replace("÷", "/")
    }

    private fun textViewCalculationLastChar(): String {
        return textViewCalculation.text.substring(textViewCalculation.text.length - 1)
    }

    private fun textViewCalculationSecondLastChar(): String {
        return textViewCalculation.text.dropLast(1).substring(textViewCalculation.text.dropLast(1).length - 1)
    }

    private fun textViewCalculationLastNumber(): String {
        var i = 1
        while (isNumeric(textViewCalculation.text.substring(textViewCalculation.text.length - i)) && textViewCalculation.text.length != i) {
            i++
        }
        return textViewCalculation.text.substring(textViewCalculation.text.length - i)
    }

    /**
     * <p>Method to calculate the equation from the textview</p>
     */
    private fun calculation(): CharSequence {
        return try {
            val stringBuilder = StringBuilder()
            stringBuilder.append(textViewToString(textViewCalculation))
            var tmpBracesClosed = bracesClosed
            while (bracesOpen != tmpBracesClosed) {
                stringBuilder.append(")")
                tmpBracesClosed++
            }
            var equation = stringBuilder.toString()
            val expression: Expression = if (equation.contains("ANS")) {
                equation = equation.replace("ANS", "x")
                ExpressionBuilder(equation).variables("x", "e", "π").build().setVariable("x", parseDouble(result.toString())).setVariable("e", Math.E).setVariable("π", Math.PI)
            } else {
                ExpressionBuilder(equation).build()
            }
            val format = DecimalFormat("0.#")
            format.format(expression.evaluate()).toString().replace(",", ".")
        } catch (e: Exception) {
            "Syntax-Error"
        }
    }

    // TODO
    /**
     * <p>Method to calculate the equation from the textview with polish syntax</p>
     */
    private fun calculationPolish(): CharSequence {
        val calculation: String = textViewToString(textViewCalculation)
        ExpressionBuilder(calculation).build()

        return "asd"
    }

    /**
     * <p>Method to throw a toast with custom message</p>
     */
    private fun throwToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}