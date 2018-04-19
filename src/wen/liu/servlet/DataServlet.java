package wen.liu.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

import wen.liu.entity.Const;
import wen.liu.entity.Page;
import wen.liu.entity.PageResult;
import wen.liu.spide.ConfigManager;
import wen.liu.spide.DataFetcher;

public class DataServlet extends BasicServlet{
	private static final long serialVersionUID = 558208393950343775L;

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException,ServletException{
		String uri = req.getRequestURI();
		String servletPath = req.getServletPath();
		String subpath = StringUtils.substringAfter(uri, servletPath+"/");
		String[] paths = subpath.split("/");
		if(paths.length>0){
			if(StringUtils.equals(paths[0], "config")){
				if(StringUtils.equalsIgnoreCase(req.getMethod(), "get")){
					IOUtils.write(ConfigManager.getPageEdits().toJSONString(), res.getOutputStream(), Const.charset);
				}else if(StringUtils.equalsIgnoreCase(req.getMethod(), "put")){
					try{
						String value = req.getParameter("value");
//						value = URLDecoder.decode(value, "utf8");
						ConfigManager.updateConfig(req.getParameter("key"), value);
						IOUtils.write("true", res.getOutputStream());
					}catch(Exception e){
						IOUtils.write("false", res.getOutputStream());
					}
				}
			}else{
				String type=paths[0];
				int pageSize=10;
				try{
					pageSize=Integer.parseInt(req.getParameter("pageSize"));
				}catch(Exception e){
				}
				int pageNo=1;
				try{
					pageNo=Integer.parseInt(req.getParameter("pageNo"));
				}catch(Exception e){
				}
				PageResult<String> result=DataFetcher.fetchDatas(type, new Page(pageSize, pageNo));
				IOUtils.write(JSON.toJSONString(result), res.getOutputStream(), Const.charset);
			}
		}
	}
}
