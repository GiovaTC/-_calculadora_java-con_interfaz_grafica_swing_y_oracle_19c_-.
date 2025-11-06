package com.miempresa.calculadora;

import java.util.Stack;

public class ExpressionEvaluator {

    public static double evaluate(String expr) {
        if (expr == null || expr.trim().isEmpty()) throw new IllegalArgumentException("Expresión vacía");
        return evaluateExpression(expr);
    }

    private static double evaluateExpression(String s) {
        s = s.replaceAll("\\s+", "");
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
                    sb.append(s.charAt(i++));
                }
                i--;
                values.push(Double.parseDouble(sb.toString()));
            } else if (c == '(') ops.push(c);
            else if (c == ')') {
                while (!ops.isEmpty() && ops.peek() != '(') applyOp(values, ops.pop());
                ops.pop();
            } else if (isOperator(c)) {
                if ((i == 0 || s.charAt(i-1) == '(' || isOperator(s.charAt(i-1))) && (c == '+' || c == '-')) values.push(0.0);
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(c)) applyOp(values, ops.pop());
                ops.push(c);
            } else throw new IllegalArgumentException("Caracter inválido: " + c);
        }

        while (!ops.isEmpty()) applyOp(values, ops.pop());
        return values.pop();
    }

    private static boolean isOperator(char c) { return c == '+' || c == '-' || c == '*' || c == '/'; }

    private static int precedence(char op) { return (op == '+' || op == '-') ? 1 : 2; }

    private static void applyOp(Stack<Double> values, char op) {
        double b = values.pop(), a = values.pop();
        switch (op) {
            case '+': values.push(a + b); break;
            case '-': values.push(a - b); break;
            case '*': values.push(a * b); break;
            case '/': if (Math.abs(b) < 1e-15) throw new IllegalArgumentException("División por cero");
                values.push(a / b); break;
        }
    }
}