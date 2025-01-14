package assignments.ex2;
// Add your documentation below:

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static assignments.ex2.Ex2Sheet.ddCompute;

/**
 * This represents a spreadsheet entry for Ex2:
 * Each spreadsheet entry (aka a Cell) which can be:
 * a number (Double), a String (Text), or a form, the data of each cell is represented as a String (e.g., "abc", "4.2", "=2+3*2", "=A1*(3-A2)".
 */
public class SCell implements Cell {
    private String line;
    private int type;
    // Add your code here
    private int order; // Depth/order for dependency calculation
    private String computedValue; // Computed value of the cell
    private List<CellEntry> dependents; // Cells that depend on this cell



    /**
     * Constructor for SCell. Sets the cell data and determines its type.
     * @param s The input string for the cell.
     */
    public SCell(String s) {
        // Add your code here
        setData(s);
        dependents = new ArrayList<>();
        computedValue = Ex2Utils.EMPTY_CELL;
    }

    @Override
    public int getOrder() {
        // Add your code here
        if (ddCompute != null && this.type == Ex2Utils.FORM){
            int max = 0;
            for (CellEntry dependent : dependents) {
                if (ddCompute[dependent.getX()][dependent.getY()] == Ex2Utils.ERR_CYCLE_FORM) {
                    // If a dependent is part of a cycle, mark this cell as part of the cycle
                    this.type = Ex2Utils.ERR_CYCLE_FORM;
                    this.order = Ex2Utils.ERR_CYCLE_FORM;
                    return this.order;
                }
                if (ddCompute[dependent.getX()][dependent.getY()] > max) {
                    max = ddCompute[dependent.getX()][dependent.getY()];
                }
            }
            order = max +1;
        }
        return order;
        // ///////////////////
    }

    //@Override
    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        // Add your code here
        line = s;
        computedValue = null; // Reset the computed value when data changes
        if (isNumber(s)) {
            type = Ex2Utils.NUMBER;
            computedValue = s;
        } else if (isFormula(s)) {
            type = Ex2Utils.FORM;

        } else if (s.startsWith("=")) {
            type = Ex2Utils.ERR_FORM_FORMAT;
        } else if (isText(s)) {
            type = Ex2Utils.TEXT;
        } else {
            type = Ex2Utils.ERR;
        }
        /////////////////////
    }
    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        // Add your code here
        order = t;
    }

    public String getComputedValue() {
        return computedValue;
    }

    public void setComputedValue(String value) {
        computedValue = value;
    }

    public void addDependent(CellEntry dependent) {
        dependents.add(dependent);
    }

    public List<CellEntry> getDependents() {
        return dependents;
    }
    public void clearDependents() {
        dependents.clear();
    }


    /**
     * Checks if the input string is a valid number.
     * @param s The input string.
     * @return True if the string represents a number, false otherwise.
     */
    public static boolean isNumber(String s) {
        try {
            Double.parseDouble(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    /**
     * Checks if the input string is a formula (starts with "=").
     * @param s The input string.
     * @return True if the string represents a formula, false otherwise.
     */
    public static boolean isFormula(String s) {
        if(s.startsWith("=") && s.length() > 1) {
            s = s.substring(1).trim(); // Remove the "=" symbol
            int openParentheses = 0;

            for (char c : s.toCharArray()) {
                if (c == '(') openParentheses++;
                if (c == ')') openParentheses--;
                if (openParentheses < 0) return false; // Closing parenthesis without an opening one
            }

            return openParentheses == 0; // Ensure all opened parentheses are closed
        }
        return false;
    }


    public Double computeForm(String form) {
        if (form == null || form.isEmpty()) {
            throw new IllegalArgumentException("Invalid formula: empty string");
        }else if (form.contains(Ex2Utils.ERR_FORM) || form.contains(Ex2Utils.ERR_CYCLE)) {
            throw new IllegalArgumentException("Invalid formula: " + form);
        }

        if (form.charAt(0) != '=') {
            throw new IllegalArgumentException("Invalid formula: missing '=' at the start");
        }

        form = form.substring(1).replaceAll("\\s", "");

        if (containsInvalidOperatorPairs(form)) {
            throw new IllegalArgumentException("Invalid formula: contains invalid operator pairs");
        }

        return evaluateExpression(form);
    }

    private boolean containsInvalidOperatorPairs(String expression) {
        String[] invalidPairs = {
                "\\+\\+", "--", "\\+-", "-\\+", "\\*\\*", "//", "\\*/", "/\\*",
                "\\+\\*", "\\+/", "-\\*", "-/", "\\*\\+", "/\\+", "/\\-", "\\*\\-"
        };
        for (String pair : invalidPairs) {
            Pattern pattern = Pattern.compile(pair);
            Matcher matcher = pattern.matcher(expression);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    private double evaluateExpression(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }





    /**
     * Checks if the input string is plain text (not a number, formula, or error).
     *
     * @param s The input string.
     * @return True if the string represents text, false otherwise.
     */
    private boolean isText(String s) {
        return !isNumber(s) && !isFormula(s) && !isError(s);
    }

    /**
     * Checks if the input string represents an error.
     *
     * @param s The input string.
     * @return True if the string represents an error, false otherwise.
     */
    private boolean isError(String s) {
        return type == Ex2Utils.ERR_FORM_FORMAT || type == Ex2Utils.ERR_CYCLE_FORM || type == Ex2Utils.ERR;
    }

}