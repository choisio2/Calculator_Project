package com.example.calculator

import android.R.attr.top
import android.os.Bundle
import android.util.Log.i
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import java.util.Stack


class MainActivity : ComponentActivity() {

    private lateinit var solutionTV: TextView
    private lateinit var resultTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        solutionTV = findViewById(R.id.solution_tv)
        resultTV = findViewById(R.id.result_tv)

        // 버튼 초기화
        val buttonsIds = listOf(
            R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3,
            R.id.button_4, R.id.button_5, R.id.button_6,
            R.id.button_7, R.id.button_8, R.id.button_9,
            R.id.button_plus, R.id.button_minus,
            R.id.button_multiply, R.id.button_divide,
            R.id.button_dot, R.id.button_openBracket, R.id.button_closeBracket
        )

        for (id in buttonsIds) {
            val button = findViewById<Button>(id)
            button.setOnClickListener {
                val text = button.text.toString()
                appendToExpression(text)
            }
        }

        // AC 버튼 - 입력값, 결과값 모두 삭제
        val acButton = findViewById<Button>(R.id.button_ac)
        acButton.setOnClickListener {
            solutionTV.text = ""
            resultTV.text = "0"
        }

        // CE 버튼 - 입력 마지막 글자 삭제
        val ceButton = findViewById<Button>(R.id.button_ce)
        ceButton.setOnClickListener {
            val text = solutionTV.text.toString()
            if(text.isNotEmpty()){
                solutionTV.text = text.substring(0, text.length - 1)
            }
        }

        // = 버튼
        val equalsButton = findViewById<Button>(R.id.button_equals)
        equalsButton.setOnClickListener {
            val expression = solutionTV.text.toString()
            try {
                val postfix = infixToPostfix(expression)
                val result = evaluatePostfix(postfix)
                resultTV.text = if (result % 1 == 0.0) result.toInt().toString() else result.toString()
            } catch (e: Exception) {
                resultTV.text = "Error"
            }
        }
    }

    private fun appendToExpression(value: String) {
        solutionTV.append(value)
    }

    // infix -> postfix (unary 처리 포함)
    private fun infixToPostfix(expression: String): List<String> {
        val output = mutableListOf<String>()
        val stack = _root_ide_package_.java.util.Stack<String>()
        val tokens = tokenize(expression)

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token) // 숫자면 출력
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isNotEmpty() && stack.peek() == "(") stack.pop()
                }
                else -> {
                    while (stack.isNotEmpty() && precedence(stack.peek()) >= precedence(token)) {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
            }
        }
        while (stack.isNotEmpty()) output.add(stack.pop())
        return output
    }

    // postfix 연산 처리
    private fun evaluatePostfix(postfix: List<String>): Double {
        val stack = Stack<Double>()
        for (token in postfix) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.push(num)
            } else {
                val b = stack.pop()
                val a = stack.pop()
                val res = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> a / b
                    else -> 0.0
                }
                stack.push(res)
            }
        }
        return stack.pop()
    }

    // 연산자 우선순위
    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            else -> 0
        }
    }

    // 숫자/연산자 토큰화 + unary 처리
    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var number = ""
        var prev: String? = null

        for (c in expr) {
            if (c.isDigit() || c == '.') {
                number += c
            } else {
                if (number.isNotEmpty()) {
                    tokens.add(number)
                    number = ""
                }
                // unary minus 처리 (맨 처음 또는 '(' 뒤에 나오는 '-')
                if (c == '-' && (prev == null || prev in listOf("(", "+", "-", "*", "/"))) {
                    number = "-" // unary라면 숫자에 부호 붙임
                } else {
                    tokens.add(c.toString())
                }
            }
            prev = c.toString()
        }
        if (number.isNotEmpty()) tokens.add(number)
        return tokens
    }
}