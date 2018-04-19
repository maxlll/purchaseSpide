package wen.liu.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wen.liu.entity.Const;
import wen.liu.entity.ExcelEntity;
import wen.liu.entity.ExcelLink;

public class POIUtil {
	public static final String defaultExcelDir="F:\\excel";
	private static Map<Integer, String> formatMap=new HashMap<Integer, String>();
	static{
		formatMap.put(Const.periodHour, "yyyy-MM-dd HH");
		formatMap.put(Const.periodDay, "yyyy-MM-dd");
		formatMap.put(Const.periodMonth, "yyyy-MM");
	}
	
	public static <E extends ExcelEntity> void outExcelEntity(List<E> datas, String parentpath, int period, boolean append) throws Exception{
		if(datas==null||datas.size()==0){
			return;
		}
		String format = formatMap.get(period);
		if(format==null){
			format=formatMap.get(Const.periodMonth);
		}
		parentpath = StringUtils.isBlank(parentpath)?defaultExcelDir:parentpath;
		if(!parentpath.endsWith(File.separator)){
			parentpath+=File.separator;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String filepath = parentpath+sdf.format(new Date())+".xlsx";
		synchronized(filepath.intern()){
			InputStream in = null;
			BufferedOutputStream bos = null;
			XSSFWorkbook wbook = null;
			try{
				if(append){
					File file = new File(filepath);
					if(file.exists()){
						in = new BufferedInputStream(new FileInputStream(file));
					}else if(!file.getParentFile().exists()){
						file.getParentFile().mkdirs();
					}
				}
				if(in!=null){
					wbook = new XSSFWorkbook(in);
				}else{
					wbook = new XSSFWorkbook();
				}
				addToWorkbook(datas, wbook);
				sortSheet(wbook);
				bos = new BufferedOutputStream(new FileOutputStream(filepath));
				wbook.write(bos);  
				bos.flush();
			}finally{
				 CloseUtil.close(in);
				 CloseUtil.close(bos);
				 CloseUtil.close(wbook);
			}
		}
	}
	
	public static <E extends ExcelEntity> XSSFWorkbook generateWorkbook(List<E> datas, InputStream in) throws IOException{
		XSSFWorkbook wbook = null;
		if(datas!=null&&datas.size()>0){
			if(in!=null){
				wbook = new XSSFWorkbook(in);
			}else{
				wbook = new XSSFWorkbook();
			}
			
			addToWorkbook(datas, wbook);
		}
		return wbook;
	}
	
	public static <E extends ExcelEntity> void addToWorkbook(List<E> datas, XSSFWorkbook wbook){
		if(datas==null||datas.size()==0||wbook==null){
			return;
		}
		ExcelEntity any = datas.get(0);
		String sheetName = any.getSheetName();
		XSSFSheet xsheet = wbook.getSheet(sheetName);
		if(xsheet==null){
			xsheet = wbook.createSheet(sheetName);
			sheetIndexMap.put(sheetName, any.getSheetIndex());
			XSSFRow xrow = xsheet.createRow(0);
			String[] titles = any.getTitleCells();
			for(int i=0;i<titles.length;i++){
				xrow.createCell(i, CellType.STRING).setCellValue(titles[i]);
			}
		}

		XSSFCreationHelper cHelper = wbook.getCreationHelper();
		int lastNum = xsheet.getLastRowNum();
		for(int j=0;j<datas.size();j++){
			ExcelEntity data = datas.get(j);
			XSSFRow row = null;
			String[] values = data.getCellValues();
			row = xsheet.createRow(lastNum+j+1);
			for(int i=0;i<values.length;i++){
				XSSFCell cell = row.createCell(i, CellType.STRING);
				cell.setCellValue(values[i]);
				ExcelLink link = data.getLink(i);
				if(link!=null){
					XSSFHyperlink hyperlink = cHelper.createHyperlink(link.getType());
					hyperlink.setAddress(link.getAddress());
					cell.setHyperlink(hyperlink);
				}
			}
		}
		
		setStyle(wbook, xsheet, any.getWidths(), any.getHeight(), lastNum+1);
	}
	
	public static void setStyle(XSSFWorkbook wbook, XSSFSheet xsheet, int[] widths, short height, int start){
		if(wbook==null||xsheet==null){
			return;
		}
		
		xsheet.createFreezePane(0, 1);
		int maxColNum=0;
		if(start==1){
			XSSFRow title=xsheet.getRow(0);
			maxColNum=title.getLastCellNum();
			for(int i=0;i<maxColNum;i++){
				xsheet.setColumnWidth(i, widths[i]);
			}
			XSSFFont font = wbook.createFont();
			font.setFontName("微软雅黑");
			font.setFontHeight((short)250);
			title.setHeight((short)500);
			XSSFCellStyle style0 = wbook.createCellStyle();
			style0.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style0.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			style0.setAlignment(HorizontalAlignment.CENTER);
			style0.setVerticalAlignment(VerticalAlignment.CENTER);
			style0.setFont(font);
			for(int i=0;i<maxColNum;i++){
				title.getCell(i).setCellStyle(style0);
			}
		}
		
		XSSFFont font1 = wbook.createFont();
		font1.setFontName("微软雅黑");
		XSSFCellStyle style1 = wbook.createCellStyle();
		style1.setAlignment(HorizontalAlignment.CENTER);
		style1.setVerticalAlignment(VerticalAlignment.CENTER);
		style1.setWrapText(true);
		style1.setFont(font1);
		for(int i=start;i<=xsheet.getLastRowNum();i++){
			XSSFRow row=xsheet.getRow(i);
			row.setHeight(height);
			for(int j=0;j<row.getLastCellNum();j++){
				row.getCell(j).setCellStyle(style1);
				if(i==xsheet.getLastRowNum() && j==row.getLastCellNum()-1)
					row.getCell(j).setAsActiveCell();
			}
		}
		xsheet.setActiveCell(new CellAddress(xsheet.getLastRowNum(), maxColNum-1));
	}
	
	public static boolean checkOfficeFileUsing(File file){
		boolean using = false;
		if(file!=null && file.exists()){
			String filename = file.getName();
			String[] names = file.getParentFile().list();
			for(String name:names){
				if(name.startsWith("~") && name.endsWith(filename)){
					using = true;
					break;
				}
			}
		}
		return using;
	}

	private static Map<String, Integer> sheetIndexMap=new HashMap<String, Integer>();
	private static void sortSheet(XSSFWorkbook wbook){
		if(wbook==null){
			return;
		}
		for(String sheetname : sheetIndexMap.keySet()){
			if(wbook.getSheet(sheetname)!=null){
				int sheetNum = wbook.getNumberOfSheets();
				int index = sheetIndexMap.get(sheetname);
				index = index>sheetNum-1?sheetNum-1:index;
				wbook.setSheetOrder(sheetname, index);
				if(index==1){
					wbook.setActiveSheet(index);
				}
			}
		}
	}
}
