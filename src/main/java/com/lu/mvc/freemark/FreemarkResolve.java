package com.lu.mvc.freemark;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

public class FreemarkResolve {

    private Configuration configuration = null;

    public FreemarkResolve() {
        configuration = new Configuration();
        configuration.setDefaultEncoding("utf-8");
    }

    public void create(Map<String, Object> dataMap, String path, String fileName, HttpServletResponse response) throws IOException {
        // dataMap 要填入模本的数据文件
        // 设置模本装置方法和路径,FreeMarker支持多种模板装载方法。可以重servlet，classpath，数据库装载，
        configuration.setDirectoryForTemplateLoading(new File(path));
        Template t = null;
        try {
            // test.ftl为要装载的模板
            t = configuration.getTemplate(fileName + ".ftl");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 输出文档路径及名称
        Writer out = null;
        FileOutputStream fos = null;
        try {
            Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
            t.process(dataMap, writer);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }

        try {
            t.process(dataMap, out);
            out.close();
            fos.close();
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
