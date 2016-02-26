package com.image.autoupdate.server;

import java.io.Reader;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 客服端版本解析
 */
public class ClientVerParser
{
    private Reader xml = null;
    /** *//** xml的document*/
    private Document doc = null;
    
    public ClientVerParser(Reader reader)
    {
        xml = reader;
        parse();
    }
    
    private void parse()
    {
        try
        {
           SAXReader reader = new SAXReader();
           doc = reader.read(xml);
        }catch(Exception e)
       {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
	public String getVerstion()
    {
        List lst = doc.selectNodes("Info/Version");
        Element el = (Element)lst.get(0);
        return el.getText();
   }
    @SuppressWarnings("unchecked")
	public UpdFile [] getFiles()
    {
        List file = doc.selectNodes("Info/Files");
        List lst = ((Element)file.get(0)).elements();
        if(lst.size()==0)
        {
            return null;
        }
       UpdFile files[] = new UpdFile[lst.size()];
        for(int i=0;i<lst.size();i++)
        {
            Element el = (Element)lst.get(i);
            List childs = el.elements();
            Element name = (Element)childs.get(0);//Name
            Element path = (Element)childs.get(1);//Path
            Element ver = (Element)childs.get(2);//Version
            files[i] = new UpdFile(name.getText());
           if("File".equals(el.getName()))
            {
               files[i].setType(0);//文件
           }else
            {
                files[i].setType(1);//目录
            }
            files[i].setPath(path.getText());
            files[i].setVersion(ver.getText());
        }
        return files;
    }
    @SuppressWarnings("unchecked")
	public String getServerPort()
    {
       List lst = doc.selectNodes("Info/UpdateServer/Port");
        Element el = (Element)lst.get(0);
        return el.getText();
    }
}