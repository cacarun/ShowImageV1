package com.cjw.showimagev1.util.xml;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cjw.showimagev1.model.RecordInfo;

public class RecodInfoHandler extends DefaultHandler
{

    private List<RecordInfo> infos = null;

    private RecordInfo recordInfo = null;
    private String tagName = null;

    public RecodInfoHandler(List<RecordInfo> infos)
    {
        super();
        this.infos = infos;
    }

    public List<RecordInfo> getInfos()
    {
        return infos;
    }

    public void setInfos(List<RecordInfo> infos)
    {
        this.infos = infos;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String temp = new String(ch, start, length);
        if (tagName.equals("url"))
        {
            recordInfo.setHttpUrl(temp);
        }
        else if (tagName.equals("num"))
        {
            recordInfo.setNum(Integer.parseInt(temp));
        }
        else if (tagName.equals("date"))
        {
            recordInfo.setDate(temp);
        }
    }

    @Override
    public void endDocument() throws SAXException
    {
        super.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equals("resource"))
        {
            infos.add(recordInfo);
        }
        tagName = "";
    }

    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        this.tagName = localName;
        if (tagName.equals("resource"))
        {
            recordInfo = new RecordInfo();
        }
    }

}
