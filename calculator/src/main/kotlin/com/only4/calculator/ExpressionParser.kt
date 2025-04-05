package com.only4.calculator

fun main() {
    val expression = "1+15*(9+4+(1+5)) + 6"
    val parser = ExpressionParser(expression)
    println(parser.parse().getValue())  // 计算解析表达式的值
    println(1 + 15 * (9 + 4 + (1 + 5)) + 6) // 直接计算对比
}

class ExpressionParser(private val infixExpression: String) {
    private var index = 0

    fun parse(): Expression {
        val postfix = convertToPostfix()
        return buildExpressionTree(postfix)
    }

    private fun convertToPostfix(): List<String> {
        val output = mutableListOf<String>()
        val operators = ArrayDeque<String>()

        while (index < infixExpression.length) {
            when (val char = infixExpression[index]) {
                '(' -> operators.addLast(char.toString())
                ')' -> processClosingBracket(output, operators)
                in "+-*/" -> processOperator(char.toString(), output, operators)
                in '0'..'9' -> output.add(parseNumber())
            }
            index++
        }
        while (operators.isNotEmpty()) output.add(operators.removeLast())
        return output
    }

    private fun processClosingBracket(output: MutableList<String>, operators: ArrayDeque<String>) {
        while (operators.isNotEmpty() && operators.last() != "(") {
            output.add(operators.removeLast())
        }
        operators.removeLast() // 移除左括号
    }

    private fun processOperator(op: String, output: MutableList<String>, operators: ArrayDeque<String>) {
        while (operators.isNotEmpty() && precedence(operators.last()) >= precedence(op)) {
            output.add(operators.removeLast())
        }
        operators.addLast(op)
    }

    private fun parseNumber(): String {
        val number = StringBuilder()
        while (index < infixExpression.length && infixExpression[index].isDigit()) {
            number.append(infixExpression[index])
            index++
        }
        index-- // 退回最后一次读取的非数字字符
        return number.toString()
    }

    private fun precedence(op: String) = when (op) {
        "+", "-" -> 1
        "*", "/" -> 2
        else -> 0
    }

    private fun buildExpressionTree(postfix: List<String>): Expression {
        val stack = ArrayDeque<Expression>()
        for (token in postfix) {
            when (token) {
                "+" -> stack.pushBinaryOp(::AddExpression)
                "-" -> stack.pushBinaryOp(::SubExpression)
                "*" -> stack.pushBinaryOp(::MulExpression)
                "/" -> stack.pushBinaryOp(::DivExpression)
                else -> stack.addLast(NumberExpression(token.toInt()))
            }
        }
        return stack.last()
    }

    private fun ArrayDeque<Expression>.pushBinaryOp(factory: (Expression, Expression) -> Expression) {
        val right = removeLast()
        val left = removeLast()
        addLast(factory(left, right))
    }
}

interface Expression {
    fun getValue(): Int
}

abstract class BinaryOperatorExpression(
    protected val left: Expression,
    protected val right: Expression
) : Expression

class NumberExpression(private val value: Int) : Expression {
    override fun getValue() = value
}

class AddExpression(left: Expression, right: Expression) : BinaryOperatorExpression(left, right) {
    override fun getValue() = left.getValue() + right.getValue()
}

class SubExpression(left: Expression, right: Expression) : BinaryOperatorExpression(left, right) {
    override fun getValue() = left.getValue() - right.getValue()
}

class MulExpression(left: Expression, right: Expression) : BinaryOperatorExpression(left, right) {
    override fun getValue() = left.getValue() * right.getValue()
}

class DivExpression(left: Expression, right: Expression) : BinaryOperatorExpression(left, right) {
    override fun getValue(): Int {
        val divisor = right.getValue()
        require(divisor != 0) { "Error: Division by zero" }
        return left.getValue() / divisor
    }
}
