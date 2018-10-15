package com.lu.mvc.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lu.mvc.annotation.MyAutowired;
import com.lu.mvc.annotation.MyController;
import com.lu.mvc.annotation.MyRequestMapping;
import com.lu.mvc.annotation.MyService;
import com.lu.mvc.core.freemarkMap;
import com.lu.test.service.TestService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class MyDispatcherServlet extends HttpServlet {

    private Logger log = Logger.getLogger(MyDispatcherServlet.class.getName());
    // 读取配置
    private Properties properties = new Properties();

    // 类的全路径名集合
    private List<String> classNameList = new ArrayList<>();

    //路径和方法集合
    private Map<String, Method> methodMap = new HashMap<>();

    /**
     * 加载的类及类实例
     */
    private Map<String, Object> classMap = new HashMap<>();


    /**
     * 请求路径对应class 实例映射
     */
    private Map<String, Object> handlerMappingMap = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            dispatcher(req, resp, "get");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void dispatcher(HttpServletRequest request, HttpServletResponse response, String type) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        String content = request.getContextPath();
        String requestUrl = request.getRequestURI();
        requestUrl.replace(content, "");
        if (methodMap.containsKey(requestUrl)) {
            Method method = methodMap.get(requestUrl);

            Class<?>[] parameterTypes = method.getParameterTypes();


            Object invoke = method.invoke(handlerMappingMap.get(requestUrl), null);
            if (invoke instanceof Map) {
                response.getWriter().println(JSONObject.parseObject(JSON.toJSONString(invoke)));
            } else if (invoke instanceof freemarkMap) {
                //freemark 模板解析
            } else {
                response.getWriter().println(invoke);
            }
            System.out.println("invoke:" + invoke);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            dispatcher(req, resp, "post");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.print("加载");
        // 1.加载配置文件
        try {
            doLoadConfig(config.getInitParameter("contextConfigLocation"));
            scanPackage(properties.get("scanPackage").toString().replaceAll("\\.", "/"));//解析配置文件
            xmlAnalysis(properties.get("beanXml").toString());
            doInstance();//反射类
            ico(); //注入
            initHandlerMapping();//解析路径
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void initHandlerMapping() {
        if (classMap.size() == 0) {
            return;
        }
        for (Map.Entry<String, Object> entry : classMap.entrySet()) {
            Class<? extends Object> classInstance = entry.getValue().getClass();
            if (classInstance.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping myRequestMapping = classInstance.getAnnotation(MyRequestMapping.class);
                String firstUrl = myRequestMapping.value();//项目根url
                Method[] methods = classInstance.getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(MyRequestMapping.class)) {
                        //方法上面加了路径注解的
                        MyRequestMapping methodRequestMapping = method.getAnnotation(MyRequestMapping.class);
                        String secondUrl = methodRequestMapping.value();//二级路径
//                        RequestMethod[] requestMethodArray = methodRequestMapping.method();//接口请求类型
//                        for (RequestMethod requestMethod : requestMethodArray) {
//                            if(ignoreCaseEquals(requestMethod.name(),"POST")){
//
//                            }
//                        }
                        method.setAccessible(true);
                        methodMap.put("/" + firstUrl.replace("/", "") + "/" + secondUrl.replace("/", ""), method);
                        handlerMappingMap.put("/" + firstUrl.replace("/", "") + "/" + secondUrl.replace("/", ""), entry.getValue())
                        ;
                    }
                }
            }
        }
    }

    private void xmlAnalysis(String beanXml) throws DocumentException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Document doc = null;
        SAXReader reader = new SAXReader();
        InputStream in = this.getClass().getResourceAsStream("/" + beanXml);
        doc = reader.read(in);
        List<Element> elementList = doc.selectNodes("//bean");
        for (Element element : elementList) {
            String bean_id = element.attributeValue("id");
            String bean_class = element.attributeValue("class");
            if (bean_class != null && bean_id != null && !"".equals(bean_class) && !"".equals(bean_id)) {
                log.info("bean的id为" + bean_id + "    bean_class：" + bean_class);
                classMap.put(bean_id, Class.forName(bean_class).newInstance());
            }
        }

    }

    /**
     * Ico 注入
     *
     * @throws IllegalAccessException
     */
    private void ico() throws IllegalAccessException {
        for (Map.Entry<String, Object> entry : classMap.entrySet()) {
            Field fields[] = entry.getValue().getClass().getDeclaredFields();
            if (fields == null) {
                continue;
            }
            for (Field field : fields) {
                field.setAccessible(true);
                String key;
//                System.out.println("类名："+entry.getValue().getName()+"方法："+field.getName());
                if (field.isAnnotationPresent(MyAutowired.class)) {
                    MyAutowired myAutowired = field.getAnnotation(MyAutowired.class);
                    String autowired = myAutowired.value();
                    if (autowired != null && !"".equals(autowired)) {
                        key = autowired;
                    } else {
                        key = field.getName();
                    }
                    field.setAccessible(true);
                    field.set(entry.getValue(), classMap.get(key));
                }
                log.info(field.getName());
            }
        }
    }

    private void doInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (classNameList == null || classNameList.size() == 0) {
            return;
        }
        for (String className : classNameList) {
            className = className.replace(".class", "");
            Class classInstance = Class.forName(className.replace(".class", ""));
            className = className.substring(className.lastIndexOf(".") + 1);
            if (classInstance.isAnnotationPresent(MyController.class)) {
                MyController myController = (MyController) classInstance.getAnnotation(MyController.class);
                String controllerValue = myController.value();
                if (controllerValue == null || controllerValue.equals("")) {
                    classMap.put(toLowerFirstWord(className), classInstance.newInstance());
                } else {
                    classMap.put(controllerValue, classInstance.newInstance());
                }
            } else if (classInstance.isAnnotationPresent(MyService.class)) {
                MyService myService = (MyService) classInstance.getAnnotation(MyService.class);
                String serviceValue = myService.value();
                if (serviceValue == null || serviceValue.equals("")) {
                    classMap.put(toLowerFirstWord(className), classInstance.newInstance());
                } else {
                    classMap.put(serviceValue, classInstance.newInstance());
                }
            }
        }
        TestService testService = (TestService) classMap.get("testService");
        System.out.println(testService);
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

    /**
     * <b>Summary: 忽略大小写比较两个字符串</b>
     * ignoreCaseEquals()
     *
     * @param str1
     * @param str2
     * @return
     */
    public static boolean ignoreCaseEquals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    private String toLowerFirstWord(String name) {
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }
}
