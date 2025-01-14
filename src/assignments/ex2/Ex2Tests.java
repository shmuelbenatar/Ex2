package assignments.ex2;

import assignments.ex2.CellEntry;
import assignments.ex2.Ex2Sheet;
import assignments.ex2.Ex2Utils;
import assignments.ex2.SCell;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Ex2Tests {


    // Tests for CellEntry
    @Test
    public void testValidCellEntry() {
        CellEntry entry = new CellEntry("A1");
        assertEquals(0, entry.getX());
        assertEquals(1, entry.getY());
        assertEquals("A1", entry.toString());
    }

    @Test
    public void testInvalidCellEntry() {
        assertThrows(IllegalArgumentException.class, () -> new CellEntry("AA"));
        assertThrows(IllegalArgumentException.class, () -> new CellEntry("1A"));
        assertThrows(IllegalArgumentException.class, () -> new CellEntry("A-1"));
    }

    @Test
    public void testCellEntryBounds() {
        assertDoesNotThrow(() -> new CellEntry("Z99"));
        assertThrows(IllegalArgumentException.class, () -> new CellEntry("A100"));
    }

    // Tests for SCell
    @Test
    public void testNumberCell() {
        SCell cell = new SCell("123");
        assertEquals(Ex2Utils.NUMBER, cell.getType());
        assertEquals("123", cell.getData());
    }

    @Test
    public void testFormulaCell() {
        SCell cell = new SCell("=1+2");
        assertEquals(Ex2Utils.FORM, cell.getType());
        // Assume computeForm method correctly evaluates the formula
        assertEquals(3.0, cell.computeForm("=1+2"));
    }

    @Test
    public void testTextCell() {
        SCell cell = new SCell("Hello World");
        assertEquals(Ex2Utils.TEXT, cell.getType());
        assertEquals("Hello World", cell.getData());
    }

    @Test
    public void testSCellInvalidFormula() {
        SCell cell = new SCell("=3+5)*2");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, cell.getType());
    }


    // Tests for Ex2Sheet
    @Test
    public void testSetAndGetValues() {
        Ex2Sheet sheet = new Ex2Sheet(10, 10);
        sheet.set(0, 0, "Text");
        sheet.set(1, 1, "123");
        sheet.set(2, 2, "=1+2");
        assertEquals("Text", sheet.get(0, 0).getData());
        assertEquals("123", sheet.get(1, 1).getData());
        assertEquals("=1+2", sheet.get(2, 2).getData());
    }

    @Test
    public void testEvaluateFormulas() {
        Ex2Sheet sheet = new Ex2Sheet(10, 10);
        sheet.set(0, 0, "=2+3");
        sheet.set(1, 1, "=A1*2");
        sheet.eval();
        assertEquals("5.0", sheet.value(0, 0));
    }

    @Test
    public void testEx2SheetSaveAndLoad() throws Exception {
        Ex2Sheet sheet = new Ex2Sheet(10, 10);
        sheet.set(0, 0, "10");
        sheet.set(1, 1, "=A0*2");
        String fileName = "testSheet.txt";

        sheet.save(fileName);
        Ex2Sheet loadedSheet = new Ex2Sheet(10, 10);
        loadedSheet.load(fileName);

        assertEquals("10.0", loadedSheet.value(0, 0));
        assertEquals("20.0", loadedSheet.value(1, 1));
    }

    @Test
    public void testEx2SheetDepthCalculation() {
        Ex2Sheet sheet = new Ex2Sheet(10, 10);
        sheet.set(0, 0, "=1+1");
        sheet.set(0, 1, "=A0+2");
        sheet.set(0, 2, "=A1*2");

        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]);
        assertEquals(1, depths[0][1]);
        assertEquals(2, depths[0][2]);
    }
}