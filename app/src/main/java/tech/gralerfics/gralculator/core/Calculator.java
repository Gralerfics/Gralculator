package tech.gralerfics.gralculator.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

enum TokenType {
    Number, Symbol, Identifier, End
}

class Token {
    public char val_char;
    public Double val_num;
    public TokenType type;

    Token(char val) {
        this.val_char = val;
        this.type = TokenType.Symbol;
//        System.out.println("Token{'" + val + "'}");
    }

    Token(Double val) {
        this.val_num = val;
        this.type = TokenType.Number;
//        System.out.println("Token{" + Double.toString(val) + "}");
    }

    Token() {
        this.type = TokenType.End;
    }
}

// 先写个只能加减乘除括号的简化版
public final class Calculator {

    private static ArrayList<Token> tokens = new ArrayList<>();

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isSymbol(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')';
    }

    private static boolean isBracket(char c) {
        return c == '(' || c == ')';
    }

    private static boolean getTokens(String expr) {
        expr += '$';

        double currentNum = 0, currentFac = 1, currentSign = 1, eps = 1e-12;
        for (int i = 0; i < expr.length(); i ++) {
            char peek = expr.charAt(i);
            if (peek == ' ') continue;
            if (isDigit(peek)) currentNum = currentNum * (currentFac > 1 + eps ? 1 : 10) + (peek - '0') / currentFac;
            if (!isDigit(peek) && peek != '.' && currentNum > eps) {
                tokens.add(new Token(currentSign * currentNum));
                currentNum = 0;
                currentFac = 1;
                currentSign = 1;
            }

            if (currentFac > 1 + eps) currentFac *= 10;
            if (peek == '.') currentFac = 10;

            if (peek == '-' && (i == 0 || expr.charAt(i - 1) == '('))
                currentSign = -1;
            else if (isSymbol(peek)) {
                tokens.add(new Token(peek));
            }
        }
        return true;
    }

    public static String ansToString(double value, int precision, boolean removeSuffixZeros) {
        StringBuilder strb = new StringBuilder(BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_UP).toPlainString());
        if (removeSuffixZeros) {
            for (int i = strb.length() - 1; i >= 0 && strb.charAt(i) == '0'; i --) strb.deleteCharAt(i);
            if (strb.charAt(strb.length() - 1) == '.') strb.deleteCharAt(strb.length() - 1);
        }
        return strb.toString();

        /* 原方式
            String[] strs = (BigDecimal.valueOf(value).toString() + ".0").split("\\.");
            StringBuilder str_r = new StringBuilder(strs[1].substring(0, Math.min(strs[1].length(), precision)));
            for (int i = str_r.length() - 1; i >= 0 && str_r.charAt(i) == '0'; i --) str_r.deleteCharAt(i);
            return strs[0] + (str_r.length() > 0 ? "." + str_r.toString() : "");
        */
    }

    public static String evalResult(String expr) {
        if (!getTokens(expr)) return "Error!";

        Stack<Character> symbolStack = new Stack<>();
        Stack<Double> outputNumberStack = new Stack<>();
        ArrayList<Character> outputSymbolQueue = new ArrayList<>();

        tokens.add(new Token()); // 尾标记
        for (Token token : tokens) {
            if (token.type == TokenType.Number) { // 数字直接加入后缀表达式
                outputNumberStack.push(token.val_num);
            }
            if (token.type == TokenType.Symbol) {
                while (!symbolStack.isEmpty() && !isBracket(token.val_char) && !isBracket(symbolStack.peek()) && (token.val_char == '+' || token.val_char == '-' || symbolStack.peek() == '*' || symbolStack.peek() == '/')) { // 非空且优先级小于栈顶，加入后缀表达式
                    outputSymbolQueue.add(symbolStack.pop());
                }
                if (token.val_char == ')') {
                    for (char sym = symbolStack.pop(); sym != '('; sym = symbolStack.pop()) { // 右括号出现，到左括号内所有符号加入后缀表达式
                        outputSymbolQueue.add(sym);
                    }
                } else {
                    symbolStack.add(token.val_char); // 除去右括号的符号加入符号栈
                }
            }
            if (token.type == TokenType.End) { // 遇尾标记把符号栈剩余符号全部加入后缀表达式
                while (!symbolStack.isEmpty()) outputSymbolQueue.add(symbolStack.pop());
            }

            // 计算局部表达式
            for (char sym : outputSymbolQueue) {
                double b = outputNumberStack.pop();
                double a = outputNumberStack.pop();
                double c = 0;
                switch (sym) {
                    case '+': c = a + b; break;
                    case '-': c = a - b; break;
                    case '*': c = a * b; break;
                    case '/': c = a / b; break;
                }
                outputNumberStack.push(c);
            }
            outputSymbolQueue.clear();
        }

        return ansToString(outputNumberStack.peek(), 8, true);
    }

}
