package com.fourinone;

import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.xml.sax.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
//import com.file.*;
//import com.base.*;
//import com.log.LogUtil;
//import javax.servlet.*;
//import javax.servlet.http.*;
//import com.lang.MulLangBean;
import java.util.List;
import java.io.FileNotFoundException;

public class XmlUtil
{
	public ArrayList getXmlPropsByFile(String filePath)
	{
		return getXmlPropsByFile(filePath, null, null);	
	}
	
	public ArrayList getXmlPropsByFile(String filePath, String PROPSROW_DESC)
	{
		return getXmlPropsByFile(filePath, PROPSROW_DESC, null);	
	}
	
	public ArrayList getXmlPropsByFile(String filePath, String PROPSROW_DESC, String KEY_DESC)
	{
		/*filePath += ".xml";
		
		FileUse fu = new FileUse();
		BaseBean base = new BaseBean();
		if(!fu.checkFile(filePath))
			filePath = base.getClassPath()+fu.getSeparator()+filePath;
		*/
		ArrayList al = new ArrayList();
		try
		{
			XmlCallback handler = 	new XmlCallback();
			SAXParserFactory factory = SAXParserFactory.newInstance(); 
			SAXParser saxParser = factory.newSAXParser();
			InputSource src = new InputSource(new FileInputStream(filePath));
			//LogUtil.fine(filePath);
			if(PROPSROW_DESC!=null)
				handler.setPROPSROW_DESC(PROPSROW_DESC);
			if(KEY_DESC!=null)
				handler.setKEY_DESC(KEY_DESC);
			saxParser.parse(src ,handler);
			al = handler.getPropsAl();
		}
		catch(Throwable t) 
		{
			//LogUtil.fine("[XmlUtil]", "[getXmlPropsByFile]", "[Error Exception:"+filePath+"]", t);
			System.err.println("[XmlConfig][Error:get XmlProps From File]"+t);
		}
		return al;	
	}
	
	public ArrayList getXmlPropsByTable()
	{
		ArrayList al = new ArrayList();
		return al;
	}
	
	public ArrayList getXmlPropsByObject()
	{
		ArrayList al = new ArrayList();
		return al;		
	}
	
	public void getXmlFileByTable()
	{
	}
	
	public ArrayList getXmlObjectByFile(String filePath)
	{
		return getXmlObjectByFile(filePath, null, null);	
	}
	
	public ArrayList getXmlObjectByFile(String filePath, String PROPSROW_DESC)
	{
		return getXmlObjectByFile(filePath, PROPSROW_DESC, null);	
	}
	
	public ArrayList getXmlObjectByFile(String filePath, String PROPSROW_DESC, String KEY_DESC)
	{
		/*filePath += ".xml";
		
		FileUse fu = new FileUse();
		BaseBean base = new BaseBean();
		if(!fu.checkFile(filePath))
			filePath = base.getClassPath()+fu.getSeparator()+filePath;
		*/
		ArrayList al = new ArrayList();
		try
		{
			XmlObjectCallback handler = new XmlObjectCallback();
			SAXParserFactory factory = SAXParserFactory.newInstance(); 
			SAXParser saxParser = factory.newSAXParser();
			InputSource src = new InputSource(new FileInputStream(filePath));
			if(PROPSROW_DESC!=null)
				handler.setPROPSROW_DESC(PROPSROW_DESC);
			if(KEY_DESC!=null)
				handler.setKEY_DESC(KEY_DESC);
			saxParser.parse(src ,handler);
			al = handler.getObjAl();
		}
		catch(FileNotFoundException fex){
			System.err.println("[XmlConfig][Can't find config file:"+filePath+",and create default config.xml in the path.]");
			FileAdapter fa = new FileAdapter(filePath);
			fa.getWriter().write(getDefaultConfig().getBytes());
			fa.close();
			return getXmlObjectByFile(filePath, PROPSROW_DESC, KEY_DESC);
		}
		catch(Throwable t) 
		{
			//LogUtil.fine("[XmlUtil]", "[getXmlObjectByFile]", "[Error Exception:"+filePath+"]", t);
			System.err.println("[XmlConfig][Error:get XmlObject From File]"+t);
		}
		return al;	
	}
	
	private String getDefaultConfig(){
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<PROPSTABLE DESC=\"TABLENAME\">\n");
			sb.append("\t<PROPSROW DESC=\"PARK\">\n");
				sb.append("\t\t<SERVICE>ParkService</SERVICE>\n");
				sb.append("\t\t<SERVERS>localhost:1888,localhost:1889</SERVERS>\n");
				sb.append("\t\t<SAFEMEMORYPER>0.95</SAFEMEMORYPER>\n");
				sb.append("\t\t<HEARTBEAT>3000</HEARTBEAT>\n");
				sb.append("\t\t<MAXDELAY>30000</MAXDELAY>\n");
				sb.append("\t\t<EXPIRATION>24</EXPIRATION>\n");
				sb.append("\t\t<CLEARPERIOD>0</CLEARPERIOD>\n");
				sb.append("\t\t<ALWAYSTRYLEADER>false</ALWAYSTRYLEADER>\n");
				sb.append("\t\t<STARTWEBAPP>true</STARTWEBAPP>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"COOLHASH\">\n");
				sb.append("\t\t<DATAROOT>data</DATAROOT>\n");
				sb.append("\t\t<KEYLENTH DESC=\"B\">256</KEYLENTH>\n");
				sb.append("\t\t<VALUELENGTH DESC=\"M\">2</VALUELENGTH>\n");
				sb.append("\t\t<REGIONLENGTH DESC=\"M\">2</REGIONLENGTH>\n");
				sb.append("\t\t<LOADLENGTH DESC=\"M\">64</LOADLENGTH>\n");
				sb.append("\t\t<HASHCAPACITY>1000000</HASHCAPACITY>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"CACHE\">\n");
				sb.append("\t\t<SERVICE>CacheService</SERVICE>\n");
				sb.append("\t\t<SERVERS>localhost:2000,localhost:2001</SERVERS>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"CACHEGROUP\">\n");
				sb.append("\t\t<STARTTIME>2010-01-01</STARTTIME>\n");
				sb.append("\t\t<GROUP>localhost:2000,localhost:2001@2010-01-01;localhost:2002,localhost:2003@2010-05-01;localhost:2004,localhost:2005@2010-05-01</GROUP>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"CACHEGROUP\">\n");
				sb.append("\t\t<STARTTIME>2018-05-01</STARTTIME>\n");
				sb.append("\t\t<GROUP>localhost:2008,localhost:2009@2018-05-01;localhost:2010,localhost:2011@2018-05-01</GROUP>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"CACHEFACADE\">\n");
				sb.append("\t\t<SERVICE>CacheFacadeService</SERVICE>\n");
				sb.append("\t\t<SERVERS>localhost:1998</SERVERS>\n");
				sb.append("\t\t<TRYKEYSNUM>100</TRYKEYSNUM>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"WORKER\">\n");
				sb.append("\t\t<TIMEOUT DESC=\"FALSE\">2</TIMEOUT>\n");
				sb.append("\t\t<SERVERS>localhost:2088</SERVERS>\n");
				sb.append("\t\t<SERVICE>false</SERVICE>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"CTOR\">\n");
				sb.append("\t\t<!-- <CTORSERVERS>localhost:1988</CTORSERVERS> -->\n");
				sb.append("\t\t<INITSERVICES>20</INITSERVICES>\n");
				sb.append("\t\t<MAXSERVICES>100</MAXSERVICES>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"COMPUTEMODE\">\n");
				sb.append("\t\t<MODE DESC=\"DEFAULT\">0</MODE>\n");
				sb.append("\t\t<MODE>1</MODE>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"FTTP\">\n");
				sb.append("\t\t<SERVERS>localhost:2121</SERVERS>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"WEBAPP\">\n");
				sb.append("\t\t<SERVERS>localhost:9080</SERVERS>\n");
				sb.append("\t\t<USERS>admin:admin,guest:123456,test:test</USERS>\n");
			sb.append("\t</PROPSROW>\n");
			sb.append("\t<PROPSROW DESC=\"LOG\">\n");
				sb.append("\t\t<LEVELNAME>ALL</LEVELNAME>\n");
				sb.append("\t\t<LEVELNAME>SEVERE</LEVELNAME>\n");
				sb.append("\t\t<LEVELNAME>WARNING</LEVELNAME>\n");
				sb.append("\t\t<LEVELNAME>INFO</LEVELNAME>\n");
				sb.append("\t\t<LEVELNAME>CONFIG</LEVELNAME>\n");
				sb.append("\t\t<LEVELNAME DESC=\"LOGLEVEL\">FINE</LEVELNAME>\n");
				sb.append("\t\t<LEVELNAME>FINER</LEVELNAME>\n");
				sb.append("\t\t<LEVELNAME>FINEST</LEVELNAME>\n");
				sb.append("\t\t<LEVELNAME>OFF</LEVELNAME>\n");
				sb.append("\t\t<INFO>true</INFO>\n");
				sb.append("\t\t<FINE>false</FINE>\n");
			sb.append("\t</PROPSROW>\n");
		sb.append("</PROPSTABLE>");
		return sb.toString();
	}	
	
	/*
	public void getXmlFileByObject(ArrayList objArray, String filePath) throws Exception
	{
		getXmlOutByObject(objArray, new FileOutputStream(filePath));//FileWriter bad code
	}
	
	
	public void getExportByObject(List objArray, HttpServletResponse response, MulLangBean mull, int exportType) throws Exception
	{
		if(exportType==1)//excel
		{
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-disposition","attachment; filename=data.xls");
			response.getWriter().println(getTableStrByObject(objArray, mull));
		}
		else if(exportType==2)//word
		{
			response.setContentType("application/msword");
			response.setHeader("Content-disposition","attachment; filename=data.doc");
			response.getWriter().println(getTableStrByObject(objArray, mull));
		}
		else if(exportType==3)//html
		{
			response.setContentType("text/html");
			response.setHeader("Content-disposition","attachment; filename=data.html");
			response.getWriter().println(getTableStrByObject(objArray, mull));
		}
		else if(exportType==4)//xml
		{
			getXmlExportByObject((ArrayList)objArray, response, mull);
		}
	}
	
	public String getTableStrByObject(List objArray, MulLangBean mull)
	{
		StringBuffer tableSb = new StringBuffer();
		BaseBean base = new BaseBean();
		tableSb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		tableSb.append("<table border=1>");
		for(int i=0;i<objArray.size();i++)
	    {
	    	ObjValue obj = (ObjValue)objArray.get(i);
	    	ArrayList objNames = obj.getObjNames();
	    	
	    	if(i==0)
	    	{
	    		tableSb.append("<tr>");
		    	for(int j=0;j<objNames.size();j++)
			    {
			    	String tagName = (String)objNames.get(j);
				    tableSb.append("<td>");
				    tableSb.append(mull!=null?mull.getString(tagName):tagName);
				    tableSb.append("</td>");
				}
				tableSb.append("</tr>");
	    	}
	    	
	    	tableSb.append("<tr>");
	    	for(int j=0;j<objNames.size();j++)
		    {
		    	String tagName = (String)objNames.get(j);
			    String tagValue = base.getString(obj.get(tagName));
			    if(tagName!=null&&(tagName.equals("DATE_CREATE")||tagName.equals("DATE_UPDATE")))
			    	tagValue = base.getDateViewDesc(obj.get(tagName));
			    tableSb.append("<td>");
			    tableSb.append(tagValue);
			    tableSb.append("</td>");
			}
			tableSb.append("</tr>");
		}
		tableSb.append("</table>");
		
		return tableSb.toString();
	}
	
	public void getXmlOutByObject(ArrayList objArray, OutputStream out)//Writer
	{
		try
		{
			Document doc = getXmlDocByObject(objArray);
			xmlTrans(doc, out);
		}
		catch(Exception e)
		{
			LogUtil.fine("[XmlUtil]", "[getXmlOutByObject]", "[Error Exception:]", e);
		}
	}
	
	public void getXmlOutByObject(ArrayList objArray, HttpServletResponse response)
	{
		try
		{
			//for return xml doc
		    response.setHeader("Cache-Control", "no-store");
		    response.setDateHeader("Expires", 0);
		    response.setContentType("text/xml; charset=UTF-8");
			
			//send
			Document doc = getXmlDocByObject(objArray);
			xmlTrans(doc, response.getWriter());//getOutputStream() has already been called for
		}
		catch(Exception e)
		{
			LogUtil.fine("[XmlUtil]", "[getXmlOutByObject]", "[Error Exception:]", e);
		}
	}
	
	public void getXmlExportByObject(ArrayList objArray, HttpServletResponse response, MulLangBean mull)
	{
		try
		{
			response.setContentType("application");
			response.setHeader("Content-disposition","attachment; filename=data.xml");
			Document doc = getXmlDocByObject(objArray, mull);
			xmlTrans(doc, response.getWriter());
		}
		catch(Exception e)
		{
			LogUtil.fine("[XmlUtil]", "[getXmlExportByObject]", "[Error Exception:]", e);
		}
	}
	
	public Document getXmlDocByObject(ArrayList objArray) throws Exception
	{
		return getXmlDocByObject(objArray, null);
	}
	
	public Document getXmlDocByObject(ArrayList objArray, MulLangBean mull) throws Exception
	{
	    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	    Element PROPSTABLE = doc.createElement(mull!=null?mull.getString("PROPSTABLE"):"PROPSTABLE");//MUST HAVA A ROOT ELEMT
	    BaseBean base = new BaseBean();
	    
	    for(int i=0;i<objArray.size();i++)
	    {
	    	Element PROPSROW = doc.createElement(mull!=null?mull.getString("PROPSROW"):"PROPSROW");
	    	
	    	ObjValue obj = (ObjValue)objArray.get(i);
	    	ArrayList objNames = obj.getObjNames();
	    	for(int j=0;j<objNames.size();j++)
		    {
			    String tagName = (String)objNames.get(j);
			    String tagValue = base.getString(obj.get(tagName));
			    Element tagNameElem = doc.createElement(mull!=null?mull.getString(tagName,""):tagName);
			    tagNameElem.appendChild(doc.createTextNode(tagValue));
			    PROPSROW.appendChild(tagNameElem);
			}
			
			PROPSTABLE.appendChild(PROPSROW);
		}
		
		doc.appendChild(PROPSTABLE);
		return doc;
	}
	
	public void xmlTrans(Document doc, Writer out) throws Exception
	{
		DOMSource doms = new DOMSource(doc);
	   	StreamResult sr = new StreamResult(out);//response.getOutputStream()
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer t = tf.newTransformer();
	    t.setOutputProperty("encoding", "UTF-8");
	    t.transform(doms, sr);
	}
	
	public void xmlTrans(Document doc, OutputStream out) throws Exception
	{
		DOMSource doms = new DOMSource(doc);
	   	StreamResult sr = new StreamResult(out);//response.getOutputStream()
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer t = tf.newTransformer();
	    t.setOutputProperty("encoding", "UTF-8");
	    t.transform(doms, sr);
	}
	*/
	public static void main(String args[])
	{
		XmlUtil xu = new XmlUtil();
		ArrayList al = xu.getXmlPropsByFile("db","SQLSERVER");//
		System.out.println(al);
	}
}