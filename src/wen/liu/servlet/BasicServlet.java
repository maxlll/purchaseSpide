package wen.liu.servlet;


import javax.servlet.http.HttpServlet;

public class BasicServlet extends HttpServlet{
	private static final long serialVersionUID = -5529020207315738515L;

	protected static final String testRoot="WEB-INF/test";
	protected static final String webRoot="WEB-INF/web";
	public static volatile String projectRoot="";
}
