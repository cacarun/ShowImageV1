package com.cjw.showimagev1.util.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.cjw.showimagev1.model.RecordInfo;

public class XMLHelper
{
    public static List<RecordInfo> parseXML(String xmlStr)
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        List<RecordInfo> infos = new ArrayList<RecordInfo>();
        try
        {
            XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
            RecodInfoHandler reHandler = new RecodInfoHandler(infos);
            xmlReader.setContentHandler(reHandler);

            xmlReader.parse(new InputSource(new StringReader(xmlStr)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return infos;
    }

}
