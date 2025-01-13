package assignments.ex2;
// Add your documentation below:
/**
 * This represents a simple 2D cell index (as in a spreadsheet),
 * The index "c12" represents the cell [2][12].
 */
public class CellEntry  implements Index2D {
    private String cellIndex;
    private int x; // column
    private int y; // row

    /**
     * Constructor for creating a CellEntry from a string representation.
     * @param cellIndex A string in the format "XY" (e.g., "A1", "B12").
     * @throws IllegalArgumentException if the string is invalid.
     */
    public CellEntry(String cellIndex) {
        if (!isValid(cellIndex)) {
            throw new IllegalArgumentException("Invalid cell index: " + cellIndex);
        }
        this.cellIndex = cellIndex.toUpperCase();
        this.x = parseX(cellIndex);
        this.y = parseY(cellIndex);
    }

    public  CellEntry(int xx, int yy) {
        int x = xx +'A';
        String result = String.valueOf(x) + String.valueOf(yy);
        new CellEntry(result);
    }


    /**
     * Validates if the cell index is valid according to the interface requirements.
     * @return true if the format and range are valid, false otherwise.
     */
    @Override
    public boolean isValid() {
        return isValid(cellIndex);
    }

    /**
     * Validates if the given string representation is a valid 2D cell index.
     * @param cellIndex The string representation of the cell (e.g., "A1").
     * @return true if the format and range are valid, false otherwise.
     */
    private boolean isValid(String cellIndex) {
        if (cellIndex == null || cellIndex.isEmpty()) {
            return false;
        }
        if (!cellIndex.matches("^[A-Za-z]+\\d{1,2}$")) {
            return false;
        }
        String numberPart = cellIndex.replaceAll("[A-Za-z]", "");
        try {
            int number = Integer.parseInt(numberPart);
            return number >= 0 && number <= 99; // Ensure row is within valid range
        } catch (NumberFormatException e) {
            return false;
        }
    }
    /**
     * @return the x value (integer) of this index
     */
    @Override
    public int getX() {
        return x;
    }
    /**
     * @return the y value (integer) of this index
     */
    @Override
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return Ex2Utils.ABC[x] + y;
    }

    /**
     * Parses the column (X) index from the string representation.
     * Converts column letters ("A", "B", ..., "Z") to a 0-based integer.
     *
     * @param cellIndex The string representation of the cell.
     * @return The column index as an integer.
     */
    private int parseX(String cellIndex) {
        char letter = cellIndex.replaceAll("\\d", "").toUpperCase().charAt(0);// Extract the first character of the column and convert it to uppercase
        return letter - 'A';// Compute the column index: 'A' → 0, 'B' → 1, ..., 'Z' → 25

    }

    /**
     * Parses the row (Y) index from the string representation.
     * Extracts the numeric part of the cell index.
     *
     * @param cellIndex The string representation of the cell.
     * @return The row index as an integer.
     */
    private int parseY(String cellIndex) {
        String digits = cellIndex.replaceAll("\\D", "");// Extract numeric part
        return Integer.parseInt(digits);
    }
}
