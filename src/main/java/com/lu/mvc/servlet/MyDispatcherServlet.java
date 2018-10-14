package com.lu.mvc.servlet;

import com.lu.mvc.annotation.MyController;
import com.lu.mvc.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class MyDispatcherServlet extends HttpServlet {

    private Logger log = Logger.getLogger(MyDispatcherServlet.class.getName());
    // 读取配置
    private Properties properties = new Properties();

    // 类的全路径名集合
    private List<String> classNameList = new ArrayList<>();

    /**
     * 加载的类及类势力
     */
    private Map<String, Class> classMap = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.print("加载");
        // 1.加载配置文件
        try {
            doLoadConfig(config.getInitParameter("contextConfigLocation"));
            scanPackage(properties.get("scanPackage").toString().replaceAll("\\.", "/"));//解析配置文件
            doInstance();//反射类
            //注入
            ico();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void ico() {
        for (Map.Entry<String, Class> entry : classMap.entrySet()) {
            Class classInstance = entry.getValue();
            Field[] fields = classInstance.getFields();
            for (Field field : fields) {

            }

        }


    }

    private void doInstance() throws ClassNotFoundException {
        if (classNameList == null || classNameList.size() == 0) {
            return;
        }
        for (String className : classNameList) {
            Class classInstance = Class.forName(className.replace(".class", ""));
            if (classInstance.isAnnotationPresent(MyController.class)) {
                MyController myController = (MyController) classInstance.getAnnotation(MyController.class);
                String controllerValue = myController.value();
                if (controllerValue == null || controllerValue.equals("")) {
                    classMap.put(className.substring(0, 1).toLowerCase() + className.substring(1), classInstance);
                } else {
                    classMap.put(controllerValue, classInstance);
                }
            } else if (classInstance.isAnnotationPresent(MyService.class)) {
                MyService myService = (MyService) classInstance.getAnnotation(MyService.class);
                String serviceValue = myService.value();
                if (serviceValue == null || serviceValue.equals("")) {
                    classMap.put(className.substring(0, 1).toLowerCase() + className.substring(1), classInstance);
                } else {
                    classMap.put(serviceValue, classInstance);
                }
            }
        }


    }

    private void scanPackage(String scanPackage) {
        if (scanPackage == null) {
            return;
        }
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage);
        File file = new File(url.getFile());
        if (file.exists()) {
            if (file.isDirectory()) {
                String[] list = file.list();
                //是文件夹
                for (String fileName : list) {
                    scanPackage(scanPackage + "/" + fileName);
                }
            } else {
                //是文件
                String className = file.getName().replace(".class", "");
                log.info("class name is " + className);
                classNameList.add((scanPackage).replace("/", "."));
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) throws IOException {
        InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        properties.load(resourceStream);
    }
}
