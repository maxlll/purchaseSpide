package wen.liu.entity;


public interface ExcelEntity {
	String[] getTitleCells();
	String[] getCellValues();
	ExcelLink getLink(int colNum);
	int[] getWidths();
	short getHeight();
	String getSheetName();
	int getSheetIndex();
}
