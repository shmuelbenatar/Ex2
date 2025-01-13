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
    public void testCellEntryValid() {
        CellEntry entry = new CellEntry("A1");
        assertEquals(0, entry.getX());
        assertEquals(1, entry.getY());
    }

    @Test
    public void testCellEntryInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new CellEntry("Z100"));
    }

    @Test
    public void testCellEntryToString() {
        CellEntry entry = new CellEntry("B5");
        assertEquals("B5", entry.toString());
    }

    // Tests for SCell
    @Test
    public void testSCellNumber() {
        SCell cell = new SCell("42");
        Assertions.assertEquals(Ex2Utils.NUMBER, cell.getType());
    }

    @Test
    public void testSCellFormula() {
        SCell cell = new SCell("=3+5");
        assertEquals(Ex2Utils.FORM, cell.getType());
        assertEquals(8.0, cell.computeForm("3+5"));
    }

    @Test
    public void testSCellInvalidFormula() {
        SCell cell = new SCell("=3+5)*2");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, cell.getType());
    }

    @Test
    public void testSCellText() {
        SCell cell = new SCell("Hello");
        assertEquals(Ex2Utils.TEXT, cell.getType());
        assertEquals("Hello", cell.getData());
    }

    // Tests for Ex2Sheet
    @Test
    public void testEx2SheetSetAndGet() {
        Ex2Sheet sheet = new Ex2Sheet(10, 10);
        sheet.set(1, 1, "=2+3");
        assertEquals("=2+3", sheet.get(1, 1).getData());
    }

    @Test
    public void testEx2SheetEvalSimple() {
        Ex2Sheet sheet = new Ex2Sheet(10, 10);
        sheet.set(1, 1, "=2+3");
        sheet.eval();
        assertEquals("5.0", sheet.value(1, 1));
    }

    @Test
    public void testEx2SheetCircularDependency() {
        Ex2Sheet sheet = new Ex2Sheet(10, 10);
        sheet.set(0, 0, "=B1");
        sheet.set(1, 1, "=A0");
        sheet.eval();
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(0, 0));
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(1, 1));
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