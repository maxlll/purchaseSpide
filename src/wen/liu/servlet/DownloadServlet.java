package wen.liu.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wen.liu.entity.Const;
import wen.liu.entity.ExcelEntity;
import wen.liu.spide.ConfigManager;
import wen.liu.spide.DataFetcher;
import wen.liu.util.CloseUtil;
import wen.liu.util.POIUtil;

public class DownloadServlet extends HttpServlet{
	private static final long serialVersionUID = 3550388865649237159L;
	
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) 
										throws IOException,ServletException{
		String subpath = StringUtils.substringAfter(req.getRequestURI(), req.getServletPath()+"/");
		String[] paths = subpath.split("/");
		String option = req.getParameter("option");
		String pageSizeStr = req.getParameter("pageSize");
		int pageSize=-1;
		if(StringUtils.equals(Const.exportAllPage, option) 
				|| StringUtils.equals(Const.exportTypePage, option)){
			try{
				pageSize=Integer.parseInt(pageSizeStr);
			}catch(Exception e){
				pageSize=10;
			}
		}
		if(StringUtils.equals(Const.exportAll, option)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			String filename = sdf.format(new Date())+Const.fileSuffixExcel;
			String filepath=POIUtil.defaultExcelDir+File.separator+filename;
			String dir = ConfigManager.getConfig(Const.configExcelDir);
			if(StringUtils.isNotBlank(dir)){
				filepath=dir.endsWith(File.separator)?dir+filename:dir+File.separator+filename;
			}
			String headname = new String(filename.getBytes(), "iso-8859-1");
			fillDownloadResHead(res, headname);
			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;
			try{
				bis = new BufferedInputStream(new FileInputStream(filepath));
				bos = new BufferedOutputStream(res.getOutputStream());
				IOUtils.copy(bis, bos);
				bos.flush();
			}catch(Exception e){
				e.printStackTrace();
			} finally{
				CloseUtil.close(bis);
				CloseUtil.close(bos);
			}
		}else{
			XSSFWorkbook wbook = new XSSFWorkbook();
			String headname=paths[0]+Const.fileSuffixExcel;
			if(StringUtils.equals(Const.exportAllPage, option)){
				Map<String, List<ExcelEntity>> dataMap=DataFetcher.getAllPageData(pageSize);
				for(List<ExcelEntity> excelDatas : dataMap.values()){
					POIUtil.addToWorkbook(excelDatas, wbook);
				}
				headname="首页数据"+Const.fileSuffixExcel;
			}else if(StringUtils.equals(Const.exportTypePage, option)){
				List<ExcelEntity> excelDatas = DataFetcher.getPageData(paths[0], pageSize);
				POIUtil.addToWorkbook(excelDatas, wbook);
			}
			fillDownloadResHead(res, headname);
			BufferedOutputStream bos = null;
			try{
				bos = new BufferedOutputStream(res.getOutputStream());
				wbook.write(bos);
				bos.flush();
			}catch(Exception e){
				e.printStackTrace();
			} finally{
				CloseUtil.close(wbook);
				CloseUtil.close(bos);
			}
		}
	}
	
	private static void fillDownloadResHead(HttpServletResponse res, String headname){
		res.setHeader("Content-Type", "text/html; charset=UTF-8");
		try {
			headname=new String(headname.getBytes(), "iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		res.setHeader("Content-Disposition", "attachment;filename="+headname);
		res.setHeader("Pragma","No-cache"); 
		res.setHeader("Cache-Control","No-cache"); 
		res.setDateHeader("Expires",0);
	}
}
