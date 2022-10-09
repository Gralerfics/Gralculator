package tech.gralerfics.gralculator.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Stack;

enum TokenType {
    Number, Symbol, Identifier, End
}

class Token {
    public char val_char;
    public Double val_num;
    public String val_str;
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

    Token(String val) {
        this.val_str = val;
        this.type = TokenType.Identifier;
//        System.out.println("Token{\"" + val + "\"}");
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
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '(' || c == ')';
    }

    private static boolean isCharacter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    private static boolean isBracket(char c) {
        return c == '(' || c == ')';
    }

    private static int getPriority(char c) {
        if (c == '^') return 10;
        if (c == '*' || c == '/') return 9;
        if (c == '+' || c == '-') return 8;
        return 0;
    }

    private static boolean laterThanTop(char top, char peek) {
        return getPriority(peek) <= getPriority(top);
    }

    private static boolean getTokens(String expr) {
        tokens.clear();
        expr += '$';

        double currentNum = 0, currentFac = 1, currentSign = 1, eps = 1e-12;
        StringBuilder currentIden = new StringBuilder();

        for (int i = 0; i < expr.length(); i ++) {
            char peek = expr.charAt(i);
            if (peek == ' ') continue;

            if (isCharacter(peek) && currentIden.length() == 0) {
                currentIden.append(peek);
                continue;
            }
            if (currentIden.length() > 0) {
                if (isDigit(peek) || isCharacter(peek) || peek == '_') {
                    currentIden.append(peek);
                    continue;
                } else {
                    tokens.add(new Token(currentIden.toString()));
                    currentIden.delete(0, currentIden.length());
                }
            }

            if (isDigit(peek)) currentNum = currentNum * (currentFac > 1 + eps ? 1 : 10) + (peek - '0') / currentFac;
            if (!isDigit(peek) && peek != '.' && currentNum > eps) {
                tokens.add(new Token(currentSign * currentNum));
                currentNum = 0;
                currentFac = 1;
                currentSign = 1;
            }

            if (currentFac > 1 + eps) currentFac *= 10;
            if (peek == '.') currentFac = 10;

            if (peek == '-' && (i == 0 || expr.charAt(i - 1) == '(')) {
                currentSign = -1;
            } else if (isSymbol(peek)) {
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
    }

    private static String evalFunc(String funcName, double val) {
        double res;
        if (Objects.equals(funcName, "sin")) {
            res = Math.sin(val);
        } else if (Objects.equals(funcName, "cos")) {
            res = Math.cos(val);
        } else if (Objects.equals(funcName, "tan")) {
            res = Math.tan(val);
        } else if (Objects.equals(funcName, "ln")) {
            if (val < 0) return "$Math Error!";
            res = Math.log(val);
        } else if (Objects.equals(funcName, "sqrt")) {
            if (val < 0) return "$Math Error!";
            res = Math.sqrt(val);
        } else if (funcName.contains("sqrt_")) {
            if (val < 0) return "$Math Error!";
            // Sqrt(n, x)
            res = val;
        } else {
            res = val;
        }
        return Double.toString(res);
    }

    private static String evalTokens(ArrayList<Token> _tokens) {
        Stack<Character> symbolStack = new Stack<>();
        Stack<Double> outputNumberStack = new Stack<>();
        ArrayList<Character> outputSymbolQueue = new ArrayList<>();

        Token currentIden = null;
        boolean currentInSubTokens = false;
        ArrayList<Token> subTokens = new ArrayList<>();
        int cntLeftBracket = 0;

        if (_tokens.isEmpty()) return "$Symbol Error!";
        _tokens.add(new Token()); // 尾标记

        for (Token token : _tokens) {
            if (currentInSubTokens) {
                if (token.type == TokenType.Symbol && token.val_char == ')' && cntLeftBracket == 0) {
                    String res = evalTokens(subTokens);
                    if (res.charAt(0) == '$') {
                        return res;
                    } else {
                        String val_str = evalFunc(currentIden.val_str, Double.parseDouble(res));
                        if (val_str.charAt(0) == '$') {
                            return val_str;
                        }
                        token = new Token(Double.parseDouble(val_str));
                        currentInSubTokens = false;
                        currentIden = null;
                    }
                } else {
                    if (token.type == TokenType.Symbol && token.val_char == '(') cntLeftBracket ++;
                    if (token.type == TokenType.Symbol && token.val_char == ')' && cntLeftBracket > 0) cntLeftBracket --;
                    if (token.val_char != ')' && token == _tokens.get(_tokens.size() - 1)) return "$Bracket Error!";
                    subTokens.add(token);
                    continue;
                }
            }
            if (token.type == TokenType.Identifier && currentIden == null) {
                currentIden = token;
                continue;
            }
            if (token.type == TokenType.Number) { // 数字直接加入后缀表达式
                outputNumberStack.push(token.val_num);
            }
            if (token.type == TokenType.Symbol) {
                if (token.val_char == '(' && currentIden != null) {
                    currentInSubTokens = true;
                    subTokens = new ArrayList<>();
                    cntLeftBracket = 0;
                    continue;
                }
                while (!symbolStack.isEmpty() && !isBracket(symbolStack.peek()) && !isBracket(token.val_char) && laterThanTop(symbolStack.peek(), token.val_char)) { // 非空且优先级小于栈顶，加入后缀表达式
                    outputSymbolQueue.add(symbolStack.pop());
                }
                if (token.val_char == ')') {
                    if (symbolStack.isEmpty()) {
                        return "$Bracket Error!";
                    }
                    for (char sym = symbolStack.pop(); sym != '('; sym = symbolStack.pop()) { // 右括号出现，到左括号内所有符号加入后缀表达式
                        outputSymbolQueue.add(sym);
                        if (symbolStack.isEmpty()) return "$Bracket Error!";
                    }
                } else {
                    symbolStack.add(token.val_char); // 除去右括号的符号加入符号栈
                }
            }
            if (token.type == TokenType.End) { // 遇尾标记把符号栈剩余符号全部加入后缀表达式
                while (!symbolStack.isEmpty()) outputSymbolQueue.add(symbolStack.pop());
            }

            currentIden = null;

            // 计算局部表达式
            for (char sym : outputSymbolQueue) {
                if (outputNumberStack.size() < 2) return "$Symbol Error!";
                double b = outputNumberStack.pop();
                double a = outputNumberStack.pop();
                double c = 0;
                switch (sym) {
                    case '+': c = a + b; break;
                    case '-': c = a - b; break;
                    case '*': c = a * b; break;
                    case '/': c = a / b; break;
                    case '^': c = Math.pow(a, b); break;
                }
                outputNumberStack.push(c);
            }
            outputSymbolQueue.clear();
        }

        if (outputNumberStack.size() > 1) return "$Error!";
        return ansToString(outputNumberStack.peek(), 8, true);
    }

    public static String evalResult(String expr) {
        if (!getTokens(expr)) return "Token Error!";

        String res = evalTokens(tokens);
        if (res.charAt(0) == '$') {
            return res.substring(1);
        } else {
            return res;
        }
    }

}
