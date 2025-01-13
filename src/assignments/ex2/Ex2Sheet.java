package assignments.ex2;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Add your documentation below:
/**
 * Implementation of a spreadsheet system where cells can contain values or formulas.
 * Supports detection and handling of circular dependencies.
 */
public class Ex2Sheet implements Sheet {
    private Cell[][] table;
    // Add your code here
    private Boolean depthCompuet = false;
    public static int[][] ddCompute;
//    private Map<CellEntry, Set<CellEntry>> cycles = new HashMap<>();
    // ///////////////////

    /**
     * Constructs a spreadsheet with the given dimensions.
     * @param x Number of rows.
     * @param y Number of columns.
     */
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        ddCompute = new int[x][y];
        for(int i=0;i<x;i=i+1) {
            for(int j=0;j<y;j=j+1) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
    }
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        // Add your code here
        eval();
        SCell cell = (SCell) get(x, y);
        if (cell == null) {
            return Ex2Utils.EMPTY_CELL; // Return empty if the cell does not exist
        }

        // If the cell is part of a cycle, return the cycle error
        if (cell.getType() == Ex2Utils.ERR_CYCLE_FORM || cell.getOrder() == Ex2Utils.ERR_CYCLE_FORM) {
            cell.setType(Ex2Utils.ERR_CYCLE_FORM );
            return Ex2Utils.ERR_CYCLE;
        }

        // If the cell has already been computed, return the computed value
        if (cell.getComputedValue() != null) {
            return cell.getComputedValue();
        }
        ans = eval(x, y);
        /////////////////////
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        if (!isIn(x, y)){
            return null;
        }
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;
        // Add your code here
        try {
            CellEntry entry = new CellEntry(cords);
            if (!entry.isValid()) return null;
            return get(entry.getX(), entry.getY());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid cell reference: " + cords);
        }
        /////////////////////
        return ans;
    }

    @Override
    public int width() {
        return table.length;
    }
    @Override
    public int height() {
        return table[0].length;
    }
    /**
     * Sets the value or formula of a cell at the given coordinates.
     * Updates dependencies and recalculates values.
     * @param x Row index.
     * @param y Column index.
     * @param s The value or formula to set.
     */
    @Override
    public void set(int x, int y, String s) {
        Set<CellEntry> dependentsToUpdate = new HashSet<>();
        if (table[x][y].getType() == Ex2Utils.ERR_CYCLE_FORM){
            SCell cell = (SCell) table[x][y];
            dependentsToUpdate.addAll(cell.getDependents());
            cell.setType(Ex2Utils.FORM); // Reset to a formula
            cell.setOrder(0); // Reset order
        }
        Cell c = new SCell(s);
        table[x][y] = c;
        // Add your code here
        depthCompuet = false;
        updateDependencies(x, y, s);
        eval();
        for (CellEntry dependent : dependentsToUpdate) {
            evalCellAndDependents(dependent.getX(), dependent.getY(),new HashSet<>());
        }
        /////////////////////
    }

    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = xx>=0 && yy>=0;
        // Add your code here
        ans= ans && xx < width() && yy < height();
        /////////////////////
        return ans;
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        // Add your code here
        if (!depthCompuet) {
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    SCell cell = (SCell) get(x, y);
                    cell.setOrder(0);
                    ans[x][y] = calculateDepth(x, y, new HashSet<>());
                    ddCompute[x][y] = ans[x][y];
                    cell.setOrder(ans[x][y]);
                    if (ans[x][y]== Ex2Utils.ERR_CYCLE_FORM){
                        cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                        cell.setOrder(Ex2Utils.ERR_CYCLE_FORM);
                    }
                }
            }
            depthCompuet = true;
        }else{
            ans = ddCompute;
        }
        // ///////////////////
        return ans;
    }
    /**
     * Loads a spreadsheet from a file.
     * @param fileName The file to load from.
     * @throws IOException If the file cannot be read.
     */
    @Override
    public void load(String fileName) throws IOException {
        // Add your code here
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                String value = parts[2];
                set(x, y, value);
            }
        }
        /////////////////////
    }
    /**
     * Saves the spreadsheet to a file.
     * @param fileName The file to save to.
     * @throws IOException If the file cannot be written.
     */
    @Override
    public void save(String fileName) throws IOException {
        // Add your code here
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    String value = table[i][j].getData();
                    if (!value.equals(Ex2Utils.EMPTY_CELL)) {
                        bw.write(i + "," + j + "," + value + "\n");
                    }
                }
            }
        }
        /////////////////////
    }
    /**
     * Evaluates all cells in the spreadsheet, recalculating their values.
     */
    @Override
    public void eval() {

        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                SCell cell = (SCell) get(x, y);
                if (cell != null) {
                    cell.setComputedValue(null);
                    if (cell.getType() == Ex2Utils.ERR_CYCLE_FORM) {
                        cell.setType(Ex2Utils.FORM); // Reset any cycle cells
                        cell.setOrder(0); // Reset order
                        cell.setComputedValue(null); // Clear computed value
                    }
                }
            }
        }
        int[][] dd = depth();
        int max = findMaxDepth(dd);
        for (int depthLevel = 0; depthLevel <= max; depthLevel++) {
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    if (dd[x][y] == depthLevel) {
                        SCell cellCheak = (SCell) get(x, y);
                        if (calculateDepth(x, y, new HashSet<>())== Ex2Utils.ERR_CYCLE_FORM){
                            cellCheak.setType(Ex2Utils.ERR_CYCLE_FORM);
                            cellCheak.setOrder(Ex2Utils.ERR_CYCLE_FORM);
                        }
                        SCell cell = (SCell) get(x, y);
                        if (dd[x][y] == Ex2Utils.ERR_CYCLE_FORM){
                            cell.setComputedValue(Ex2Utils.ERR_CYCLE);
                            continue;
                        }
                        cell.setComputedValue(eval(x, y)); // Evaluate cells at the current depth level
                    }
                }
            }
        }
    }
    /**
     * Evaluates a single cell at the given coordinates.
     * @param x Row index.
     * @param y Column index.
     * @return The evaluated value of the cell as a string.
     */
    @Override
    public String eval(int x, int y) {
        SCell cell = (SCell) get(x, y);
        if (cell == null) {
            return Ex2Utils.EMPTY_CELL;
        }

        String data = cell.getData();
        if (cell.getType() == Ex2Utils.TEXT) {
            cell.setComputedValue(data);
        } else if (cell.getType() == Ex2Utils.NUMBER) {
            cell.setComputedValue(String.valueOf(Double.parseDouble(data)));
        } else if (cell.getType() == Ex2Utils.ERR_CYCLE_FORM) {
            cell.setComputedValue(Ex2Utils.ERR_CYCLE);
        } else if (cell.getType() == Ex2Utils.FORM) {
            String formula = data.substring(1); // Remove '='
            try {
                formula = resolveReferences(formula);
                cell.setComputedValue(String.valueOf(((SCell) cell).computeForm(formula)));
            } catch (IllegalArgumentException e) {
                cell.setComputedValue(Ex2Utils.ERR_FORM); // Return error if formula is invalid
            }
        }
        return cell.getComputedValue();
    }

    /**
     * Updates the dependencies of a cell based on its formula.
     * @param x Row index.
     * @param y Column index.
     * @param formula The formula of the cell.
     */
    private void updateDependencies(int x, int y, String formula) {
        SCell cell = (SCell) get(x, y);
        cell.clearDependents();

        if (formula.startsWith("=")) {
            List<CellEntry> references = parseFormulaTokens(formula.substring(1)); // Skip the '='
            for (CellEntry entry : references) {
                if (entry.isValid()) {
                    SCell dependentCell = (SCell) get(entry.getX(), entry.getY());
                    if (dependentCell != null) {
                        dependentCell.addDependent(new CellEntry(Ex2Utils.ABC[x] + y));
                    }
                }
            }
        }
    }

    /**
     * Resolves all references within a formula by replacing them with their computed values.
     * @param formula The formula to resolve.
     * @return The formula with references resolved.
     */
    public String resolveReferences(String formula) {
        List<CellEntry> references = parseFormulaTokens(formula);
        for (CellEntry entry : references) {
            Cell referencedCell = get(entry.getX(), entry.getY());
            if (referencedCell instanceof SCell) {
                String value = ((SCell) referencedCell).getComputedValue();
                if (value == null || value.isEmpty()) {
                    return Ex2Utils.ERR_FORM;
                }
                if (SCell.isNumber(value) || SCell.isFormula(value)) {
                    if (isNegativeNumber(value)) {
                        double numericValue = Double.parseDouble(value);
                        if (numericValue < 0 && formula.contains("-" + entry)) {
                            formula = formula.replace("-" + entry, "0-" + value);
                        } else {
                            formula = formula.replace(entry.toString(), value);
                        }
                    } else {
                        formula = formula.replace(entry.toString(), value);
                    }
                }
            }
        }
        return formula;
    }
    /**
     * Parses a formula into a list of cell references.
     * @param formula The formula to parse.
     * @return A list of cell entries representing references in the formula.
     */
    private List<CellEntry> parseFormulaTokens(String formula) {
        List<CellEntry> cellEntries = new ArrayList<>();
        // Match cell references like A1, B12, etc.
        Matcher matcher = Pattern.compile("[A-Za-z]+\\d+").matcher(formula);

        while (matcher.find()) {
            String token = matcher.group();
            try {
                CellEntry entry = new CellEntry(token);
                if (entry.isValid()) {
                    cellEntries.add(entry);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid cell reference: " + token);
            }
        }

        return cellEntries; // Return the list of parsed cell entries
    }
    private boolean isNegativeNumber(String value) {
        if (value.startsWith("-") && value.length() > 1) {
            String numericPart = value.substring(1);
            return numericPart.chars().allMatch(Character::isDigit);
        }
        return false;
    }

    /**
     * Finds the maximum depth in the 2D depth array.
     * @param depth The 2D array containing depths of each cell.
     * @return The maximum depth value.
     */
    private int findMaxDepth(int[][] depth) {
        int max = 0;
        for (int x = 0; x < depth.length; x++) {
            for (int y = 0; y < depth[x].length; y++) {
                if (depth[x][y] > max) {
                    max = depth[x][y];
                }
            }
        }
        return max;
    }
    /**
     * Evaluates a cell and all its dependents recursively.
     * @param x Row index.
     * @param y Column index.
     * @param visited A set of visited cells to prevent infinite recursion.
     */
    private void evalCellAndDependents(int x, int y, Set<String> visited) {
        String cellName = Ex2Utils.ABC[x] + y;
        // Check if this cell is already being evaluated to prevent infinite recursion
        if (visited.contains(cellName)) {
            return; // Stop recursion
        }
        // Add the current cell to the set of visited cells
        visited.add(cellName);

        SCell cell = (SCell) get(x, y);
        if (cell == null) {
            visited.remove(cellName);
            return;
        }
        // Detect and mark cycles
        int depth = calculateDepth(x, y, new HashSet<>());
        if (depth == Ex2Utils.ERR_CYCLE_FORM) {
            cell.setType(Ex2Utils.ERR_CYCLE_FORM);
            cell.setOrder(Ex2Utils.ERR_CYCLE_FORM);
            cell.setComputedValue(Ex2Utils.ERR_CYCLE);
            visited.remove(cellName);
            return;
        }
        // Update the cell type if necessary
        if (cell.getType() == Ex2Utils.FORM) {
            cell.setOrder(depth);
        }
        // Update the current cell's value
        cell.setComputedValue(eval(x, y));
        // Update all dependent cells
        for (CellEntry dependent : cell.getDependents()) {
            evalCellAndDependents(dependent.getX(), dependent.getY(), visited);
        }
        // Remove the cell from the visited set to allow future evaluations
        visited.remove(cellName);
    }
    /**
     * Calculates the depth of a cell, marking cycles when detected.
     * @param x Row index.
     * @param y Column index.
     * @param visited A set of visited cells to detect cycles.
     * @return The depth of the cell or an error code for cycles.
     */
    private int calculateDepth(int x, int y, Set<String> visited) {
        String cellName = Ex2Utils.ABC[x] + y;

        if (visited.contains(cellName) ) {
            for (String cellE : visited) {
                CellEntry visit = new CellEntry(cellE);
                SCell cell1 = (SCell) get(visit.getX(),visit.getY());
                cell1.setType(Ex2Utils.ERR_CYCLE_FORM);
                cell1.setOrder(Ex2Utils.ERR_CYCLE_FORM); // Set order to indicate a cycle
            }
            return Ex2Utils.ERR_CYCLE_FORM; // Circular dependency
        }
        visited.add(cellName);

        Cell cell = get(x, y);
        if (cell == null || cell.getType() != Ex2Utils.FORM) {
            return 0;
        }

        String formula = cell.getData().substring(1);
        String[] tokens = formula.split("[+\\-*/()]");
        int maxDepth = 0;
        for (String token : tokens) {

            if (SCell.isNumber(token)) {
                continue;
            }
            try {
                CellEntry entry = new CellEntry(token);
                if (entry.isValid()) {
                    int depth = calculateDepth(entry.getX(), entry.getY(), visited) ;

                    if (depth == Ex2Utils.ERR_CYCLE_FORM) {
                        SCell dependentCell = (SCell) get(entry.getX(), entry.getY());
                        dependentCell.setType(Ex2Utils.ERR_CYCLE_FORM);
                        dependentCell.setOrder(Ex2Utils.ERR_CYCLE_FORM);
                        return Ex2Utils.ERR_CYCLE_FORM;
                    }
                    maxDepth = Math.max(maxDepth, depth+1);
                }
            } catch (IllegalArgumentException e) {
                return Ex2Utils.ERR_FORM_FORMAT; // Invalid reference
            }
        }
        visited.remove(cellName);

        return maxDepth;
    }

}