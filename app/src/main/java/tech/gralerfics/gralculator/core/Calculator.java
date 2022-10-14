package tech.gralerfics.gralculator.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Stack;

enum TokenType { Number, Symbol, Identifier, End };

class Token {
    public char val_char;
    public Double val_num;
    public String val_str;
    public TokenType type;

    Token(char val) {
        this.val_char = val;
        this.type = TokenType.Symbol;
    }

    Token(Double val) {
        this.val_num = val;
        this.type = TokenType.Number;
    }

    Token(String val) {
        this.val_str = val;
        this.type = TokenType.Identifier;
    }

    Token() {
        this.type = TokenType.End;
    }
}

public final class Calculator {
    // 计算参数设置
    private static int precision = 8;
    private static boolean removeSuffixZeros = true;

    // 参数 Getters & Setters
    public int getPrecision() { return precision; }
    public void setPrecision(int precision) { this.precision = precision; }
    public boolean isRemoveSuffixZeros() { return removeSuffixZeros; }
    public void setRemoveSuffixZeros(boolean removeSuffixZeros) { this.removeSuffixZeros = removeSuffixZeros; }

    // 寄存器
    private static HashMap<String, Double> vars = new HashMap<>();
    public static HashMap<String, Double> getVars() { return vars; }

    // Tokens 序列
    private static ArrayList<Token> tokens = new ArrayList<>();

    // Utils
    private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private static boolean isSymbol(char c) { return c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '(' || c == ')'; }
    private static boolean isCharacter(char c) { return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'; }
    private static boolean isBracket(char c) { return c == '(' || c == ')'; }
    private static boolean isIdentifier(String s) { // 标识符首位字母，可包含字母数字下划线
        for (int i = 0; i < s.length(); i ++) {
            if (!isCharacter(s.charAt(i)) && !isDigit(s.charAt(i)) && s.charAt(i) != '_' || isDigit(s.charAt(i)) && i == 0) {
                return false;
            }
        }
        return true;
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

    // 词法分析
    private static boolean getTokens(String expr) {
        tokens.clear();
        expr += '$';

        double currentNum = 0, currentFac = 1, currentSign = 1, eps = 1e-12;
        boolean inNumberSign = false;
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

            if (isDigit(peek)) {
                currentNum = currentNum * (currentFac > 1 + eps ? 1 : 10) + (peek - '0') / currentFac;
                inNumberSign = true;
            }
            if (!isDigit(peek) && peek != '.' && inNumberSign) {
                tokens.add(new Token(currentSign * currentNum));
                currentNum = 0;
                currentFac = 1;
                currentSign = 1;
                inNumberSign = false;
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

    // 答案保留位转换
    public static String ansToString(double value, int precision, boolean removeSuffixZeros) {
        StringBuilder strb = new StringBuilder(BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_UP).toPlainString());
        if (removeSuffixZeros) {
            for (int i = strb.length() - 1; i >= 0 && strb.charAt(i) == '0'; i --) strb.deleteCharAt(i);
            if (strb.charAt(strb.length() - 1) == '.') strb.deleteCharAt(strb.length() - 1);
        }
        return strb.toString();
    }

    // 函数值计算
    private static double evalFunc(String funcName, double val) throws Exception {
        double res;
        if (Objects.equals(funcName, "sin")) {
            res = Math.sin(val);
        } else if (Objects.equals(funcName, "cos")) {
            res = Math.cos(val);
        } else if (Objects.equals(funcName, "tan")) {
            res = Math.tan(val);
        } else if (Objects.equals(funcName, "ln")) {
            if (val < 0) throw new Exception("Math Error!");
            res = Math.log(val);
        } else if (Objects.equals(funcName, "sqrt")) {
            if (val < 0) throw new Exception("Math Error!");
            res = Math.sqrt(val);
        } else {
            throw new Exception("Identifier Error!");
        }
        return res;
    }

    // 变量值计算
    private static double evalVar(String varName) throws Exception {
        double res = 0;
        if (!vars.containsKey("Ans") && varName.equals("Ans")) vars.put("Ans", 0.0);
        if (vars.containsKey(varName)) {
            res = vars.get(varName);
        } else {
            throw new Exception("Variable Error!");
        }
        return res;
    }

    // 子式值计算
    private static String evalTokens(ArrayList<Token> _tokens, boolean inSubTokens) throws Exception {
        Stack<Character> symbolStack = new Stack<>();
        Stack<Double> outputNumberStack = new Stack<>();
        ArrayList<Character> outputSymbolQueue = new ArrayList<>();

        Token currentIden = null;
        boolean currentInSubTokens = false;
        ArrayList<Token> subTokens = new ArrayList<>();
        int cntLeftBracket = 0;

        if (_tokens.isEmpty()) {
            if (inSubTokens) {
                throw new Exception("Parameter Error!");
            } else {
                return "0";
            }
        }

        _tokens.add(new Token()); // 尾标记
        for (Token token : _tokens) {
            // 括号子式
            if (currentInSubTokens) {
                if (token.type == TokenType.Symbol && token.val_char == ')' && cntLeftBracket == 0) { // 子式终止
                    String res = evalTokens(subTokens, true); // 递归计算子式
                    token = new Token(evalFunc(currentIden.val_str, Double.parseDouble(res))); // 计算函数值作为 new Token()
                    currentInSubTokens = false; // 消除子式标志位
                    currentIden = null; // 消除子式核函数标志
                } else {
                    if (token.type == TokenType.Symbol && token.val_char == '(') cntLeftBracket ++;
                    if (token.type == TokenType.Symbol && token.val_char == ')' && cntLeftBracket > 0) cntLeftBracket --;
                    if (token.val_char != ')' && token == _tokens.get(_tokens.size() - 1)) throw new Exception("Bracket Error!");
                    subTokens.add(token);
                    continue;
                }
            }

            // 标识符挂起标记
            if (token.type == TokenType.Identifier && currentIden == null) {
                currentIden = token;
                continue;
            }

            // 数字直接加入后缀表达式
            if (token.type == TokenType.Number) {
                outputNumberStack.push(token.val_num);
            }

            // 前有标识符
            if (currentIden != null) {
                if (token.val_char == '(') { // 紧跟左括号
                    currentInSubTokens = true;
                    subTokens = new ArrayList<>();
                    cntLeftBracket = 0;
                    continue;
                } else { // 不接左括号则作为变量名
                    outputNumberStack.push(evalVar(currentIden.val_str));
                }
            }

            // 符号
            if (token.type == TokenType.Symbol) {
                while (!symbolStack.isEmpty() && !isBracket(symbolStack.peek()) && !isBracket(token.val_char) && laterThanTop(symbolStack.peek(), token.val_char)) {
                    outputSymbolQueue.add(symbolStack.pop()); // 非空且优先级小于栈顶，加入后缀表达式
                }
                if (token.val_char == ')') {
                    if (symbolStack.isEmpty()) throw new Exception("Bracket Error!"); // 无匹配的左括号
                    for (char sym = symbolStack.pop(); sym != '('; sym = symbolStack.pop()) { // 右括号出现，到左括号内所有符号加入后缀表达式
                        outputSymbolQueue.add(sym);
                        if (symbolStack.isEmpty()) throw new Exception("Bracket Error!");
                    }
                } else {
                    symbolStack.add(token.val_char); // 除去右括号的符号加入符号栈
                }
            }

            // 遇尾标记把符号栈剩余符号全部加入后缀表达式
            if (token.type == TokenType.End) {
                while (!symbolStack.isEmpty()) outputSymbolQueue.add(symbolStack.pop());
            }

            // 间隔超过一 Token 则标识符标记失效
            currentIden = null;

            // 同步计算表达式值
            for (char sym : outputSymbolQueue) {
                if (outputNumberStack.size() < 2) throw new Exception("Symbol Error!");
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

        // 栈中剩余不止一个数字
        if (outputNumberStack.size() > 1) throw new Exception("Symbol Error!");

        // 取栈中最后留下的数字为结果
        double ans = outputNumberStack.peek();

        // 保存上次结果
        vars.put("Ans", ans);

        // 应用数字格式并返回
        return ansToString(ans, precision, removeSuffixZeros);
    }

    // 计算原始表达式
    public static String evalResult(String expr) throws Exception {
        String exprc = expr;

        // 赋值表达式
        String iden = null;
        if (expr.contains("->")) {
            String[] exprsp = expr.split("->");
            if (exprsp.length > 2) throw new Exception("Assignment Error!");
            if (!isIdentifier(exprsp[1])) throw new Exception("Identifier Error!");
            exprc = exprsp[0];
            iden = exprsp[1];
        }

        // 词法分析与表达式计算
        if (!getTokens(exprc)) throw new Exception("Token Error!");
        String ans = evalTokens(tokens, false);

        // 赋值
        if (iden != null) vars.put(iden, Double.parseDouble(ans));

        // 返回结果
        return ans;
    }
}
