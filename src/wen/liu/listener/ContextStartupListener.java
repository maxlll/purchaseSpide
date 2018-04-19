package wen.liu.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wen.liu.servlet.BasicServlet;
import wen.liu.spide.EntityManager;
import wen.liu.spide.SpiderManager;

public class ContextStartupListener implements ServletContextListener{
	private static Logger logger = null;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		if(StringUtils.isBlank(BasicServlet.projectRoot)){
			BasicServlet.projectRoot = arg0.getServletContext().getRealPath("");
			System.setProperty("projectRoot", BasicServlet.projectRoot);
		}
		
		if(logger==null){
			logger = LoggerFactory.getLogger(ContextStartupListener.class);
			logger.info("项目根目录："+BasicServlet.projectRoot);
		}
		
		SpiderManager.startSpiders();
		EntityManager.loadExcelEntity();
	}
}
