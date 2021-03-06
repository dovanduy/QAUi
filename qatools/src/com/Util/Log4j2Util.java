package com.Util;
import java.io.ByteArrayInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Log4j2Util {
	
	public boolean InitLog4j2(String time){
		boolean init=false;
		InputStream inputStream = null;
		try {
			 inputStream=changeXML(time);
			if(inputStream!=null){
				ConfigurationSource source = new ConfigurationSource(inputStream);
				Configurator.initialize(null, source);	
				init=true;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(inputStream!=null){
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return init;
	}
	
	private InputStream changeXML(String time){
		SAXReader reader = new SAXReader();
		InputStream inputStream = null;
		try {
			Document doc = reader.read(getClass().getResourceAsStream("/Resources/log4j2.xml"));
			Element root=doc.getRootElement();
			List<Element> childElements = root.elements();
			  for (Element child : childElements) {
				  //未知属性名情况下
//				   List<Attribute> attributeList = child.attributes();
//				   for (Attribute attr : attributeList) {
//				    System.out.println("attribute-"+attr.getName() + ": " + attr.getValue());
//				   }

				   //已知属性名情况下
				  // System.out.println("id: " + child.attributeValue("fileName"));
				 if( child.getName().equals("appenders")){
					 Element ele_File= child.element("File");
					 Attribute attr=ele_File.attribute("fileName");
					 attr.setValue("Logs/Log_"+time+".txt");
				 }
				   //未知子元素名情况下
//				   List<Element> elementList = child.elements();
//				   for (Element ele : elementList) {
//				    System.out.println("element-"+ele.getName() + ": " + ele.getText());
//				   }
			  }
			
			inputStream= new ByteArrayInputStream(doc.asXML().getBytes("UTF-8"))  ;
//	        XMLWriter output = new XMLWriter(new FileWriter(new File("C:/Users/DELL/Desktop/log4j2.xml")));
//	        output.write(doc);
//	        output.close();
			
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return inputStream;
	}

}
//SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmm_ss"); 
//String time=sdf.format(new Date());
//
//Properties properties = new Properties();
//try {
//	System.out.println(Run.class.getResource("/log4j.properties").getPath().toString());
//	properties.load(Run.class.getResourceAsStream("/log4j.properties"));
//	String name = properties.getProperty("log4j.appender.logfile.File");
//	System.out.println(name);
//	properties.setProperty("log4j.appender.logfile.File", "Log/flazr-" + time + ".log");
//	properties.store(new FileWriter(Run.class.getResource("/log4j.properties").getFile()), "test");
//} catch (IOException e) {
//	e.printStackTrace();
//}