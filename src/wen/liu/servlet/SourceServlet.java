package wen.liu.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import wen.liu.entity.Const;
import wen.liu.spide.ConfigManager;

public class SourceServlet extends BasicServlet{
	private static final long serialVersionUID = -3156959182079924644L;
	private static volatile String suffix = "";

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException,ServletException{
		String subPath = StringUtils.substringAfter(req.getRequestURI(), req.getServletPath()+"/");
		if(StringUtils.isBlank(subPath)){
			IOUtils.copy(new FileInputStream(projectRoot+webRoot+"/index.html"), res.getOutputStream());
		}else if(subPath.indexOf(".")>0){
			if(StringUtils.isBlank(suffix)){
				suffix = ConfigManager.getConfig(Const.configSupportFile);
			}
			String currSuffix = StringUtils.substringAfterLast(subPath, ".");
			if(currSuffix.matches(suffix)){
				try{
					IOUtils.copy(new FileInputStream(projectRoot+webRoot+"/"+subPath), res.getOutputStream());
				}catch(FileNotFoundException e){
					res.setStatus(404);
				}
			}else{
				res.getOutputStream().write(("不支持的资源类型："+currSuffix).getBytes());
			}
		}else{
			req.getRequestDispatcher("/data").forward(req, res);
		}
	}
}
