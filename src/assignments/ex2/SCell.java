package assignments.ex2;
// Add your documentation below:

import java.util.ArrayList;
import java.util.List;

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
     * Computes the value of a formula string.
     * This function handles only arithmetic operations and parentheses.
     * @param form The formula string (e.g., "3+4*(5-2)").
     * @return The result as a double.
     */
    public Double computeForm(String form) {
//			form = Ex2Sheet.resolveReferences(form);
        form = form.replaceAll("\\s", ""); // Remove whitespace
        if (form.contains(Ex2Utils.ERR_FORM) || form.contains(Ex2Utils.ERR_CYCLE)) {
            throw new IllegalArgumentException("Invalid formula: " + form);
        }
        return evaluate(form);

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

    /**
     * Evaluates a mathematical expression recursively.
     * @param expression The expression to evaluate.
     * @return The computed result as a double.
     */
    private double evaluate(String expression) {
        if (expression.contains("(")) {
            int start = expression.lastIndexOf('(');
            int end = expression.indexOf(')', start);
            if (end == -1) {
                throw new IllegalArgumentException("Mismatched parentheses in expression: " + expression);
            }
            String subExpression = expression.substring(start + 1, end);
            double result = evaluate(subExpression);
            expression = expression.substring(0, start) + result + expression.substring(end + 1);
            return evaluate(expression);
        } else {
            return calculateSimpleExpression(expression);
        }
    }

    /**
     * Calculates a simple expression without parentheses.
     *
     * @param expression The expression to calculate.
     * @return The computed result as a double.
     */
    private double calculateSimpleExpression(String expression) {
        String[] addSub = expression.split("(?=[+-])|(?<=[+-])");
        double result = multiplyDivide(addSub[0]);
        for (int i = 1; i < addSub.length; i += 2) {
            String operator = addSub[i];
            double value = multiplyDivide(addSub[i + 1]);
            if (operator.equals("+")) {
                result += value;
            } else if (operator.equals("-")) {
                result -= value;
            }
        }
        return result;
    }

    /**
     * Handles multiplication and division in an expression.
     *
     * @param expression The expression to calculate.
     * @return The computed result as a double.
     */
    private double multiplyDivide(String expression) {
        String[] mulDiv = expression.split("(?=[*/])|(?<=[*/])");
        double result = Double.parseDouble(mulDiv[0]);
        for (int i = 1; i < mulDiv.length; i += 2) {
            String operator = mulDiv[i];
            double value = Double.parseDouble(mulDiv[i + 1]);
            if (operator.equals("*")) {
                result *= value;
            } else if (operator.equals("/")) {
                result /= value;
            }
        }
        return result;
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