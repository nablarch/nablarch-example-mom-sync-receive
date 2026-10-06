/*
 * ExcelDump — NTF Excel テストデータ（.xls / .xlsx）をプレーンテキストにダンプするツール。
 *
 * 目的:
 *   Task #3 のサンプリング比較で、変換元 Excel の各シート・各行・各セルの値を人間が読める形で
 *   出力し、converter が生成した YAML と内容が一致するかを突き合わせるために使う。
 *   NTF 実行時と同じ Apache POI（poi/poi-ooxml 3.8、プロジェクトの test クラスパスに含まれる）で
 *   読み取るため、NTF の PoiXlsReader と同じセル値解釈に近い結果が得られる。
 *
 * 使い方:
 *   javac -cp "<test-classpath>" -d <outdir> ExcelDump.java
 *   java  -cp "<outdir>:<test-classpath>" ExcelDump <excelFile>
 *
 * 出力:
 *   === SHEET: <シート名> === に続き、各行を "r<行>: | c0 | c1 | ..." 形式で出力。
 *   セルは文字列化（数値は小数が無ければ整数表記）。空セルは空文字。
 */
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.PrintStream;

public class ExcelDump {
    public static void main(String[] args) throws Exception {
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        if (args.length < 1) {
            out.println("usage: ExcelDump <excelFile>");
            return;
        }
        try (FileInputStream fis = new FileInputStream(args[0])) {
            Workbook wb = WorkbookFactory.create(fis);
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                out.println("=== SHEET: " + sheet.getSheetName() + " ===");
                int lastRow = sheet.getLastRowNum();
                for (int r = 0; r <= lastRow; r++) {
                    Row row = sheet.getRow(r);
                    StringBuilder sb = new StringBuilder("r" + r + ": ");
                    if (row == null) {
                        out.println(sb.append("<empty>").toString());
                        continue;
                    }
                    int lastCell = row.getLastCellNum();
                    for (int c = 0; c < lastCell; c++) {
                        Cell cell = row.getCell(c);
                        sb.append("| ").append(cellToString(cell)).append(" ");
                    }
                    out.println(sb.toString());
                }
            }
        }
    }

    private static String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC: {
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            }
            case Cell.CELL_TYPE_FORMULA:
                return "{formula}" + cell.getCellFormula();
            case Cell.CELL_TYPE_BLANK:
                return "";
            default:
                return "";
        }
    }
}
