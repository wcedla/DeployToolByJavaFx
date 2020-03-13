package fxft.util;

import fxft.data.ModuleData;
import fxft.global.GlobalData;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XmlUtils {

    public static List<ModuleData> parseXmlFile() {
        List<ModuleData> moduleDataList = new ArrayList<>();
        Document document = null;
        try {
            SAXReader saxReader = new SAXReader();
            document = saxReader.read(new File(GlobalData.INSTALLED_XML_FILE_PATH));
            Element rootElement = document.getRootElement();
            Iterator<Element> moduleElementIterator = rootElement.elementIterator();
            while (moduleElementIterator.hasNext()) {
                Element moduleElement = moduleElementIterator.next();
                moduleDataList.add(new ModuleData(moduleElement.element("name").getText(), moduleElement.elementText("server"), moduleElement.element("path").getText(), moduleElement.element("log").getText()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moduleDataList;
    }

    public static void writeXmlFile(ModuleData moduleData) {
        Document document = null;
        Element rootElement = null;
        try {
            SAXReader saxReader = new SAXReader();
            File xmlFile = new File(GlobalData.INSTALLED_XML_FILE_PATH);
            if (xmlFile.exists()) {
                document = saxReader.read(xmlFile);
                rootElement = document.getRootElement();
            } else {
                document = DocumentHelper.createDocument();
                rootElement = DocumentHelper.createElement("deploy");
                document.setRootElement(rootElement);
            }

            Element moduleElement = rootElement.addElement("module");
            Element nameElement = moduleElement.addElement("name");
            nameElement.setText(moduleData.getModuleName());
            Element serverElement = moduleElement.addElement("server");
            serverElement.setText(moduleData.getServerName());
            Element pathElement = moduleElement.addElement("path");
            pathElement.setText(moduleData.getModulePath());
            Element serverLogElement = moduleElement.addElement("log");
            serverLogElement.setText(moduleData.getServerLogPath());
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(GlobalData.INSTALLED_XML_FILE_PATH), outputFormat);
            xmlWriter.write(document);
            xmlWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
