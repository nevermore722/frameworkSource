package com.zjh.mySpringMVC.servlet;

import com.zjh.mySpringMVC.anno.MyAutowired;
import com.zjh.mySpringMVC.anno.MyController;
import com.zjh.mySpringMVC.anno.MyRequestMapping;
import com.zjh.mySpringMVC.anno.MyRequestParam;
import com.zjh.mySpringMVC.anno.MyService;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ：ZouJiaHui
 * @date ：Created in 2020/4/17 11:07
 * @description：
 * @modified By：
 * @version: 1.0
 */
public class DispatcherServlet extends HttpServlet {

  List<String> classNames = new ArrayList<String>();
  Map<String, Object> beans = new HashMap<String, Object>();//beans就是IOC容器
  Map<String, Object> handlerMap = new HashMap<String, Object>();


  //初始化容器，扫描 实例化bean 依赖注入 URLMAPPING
  @Override
  public void init(ServletConfig config) throws ServletException {
    //扫描
    scanPackage("com.zjh.myStringMVC");
    doInstance();

    doAutowired();

    urlMapping(); //http://127.0.0.1:8080/xxx/james/query---->method
  }

  public void urlMapping() {
    for (Map.Entry<String, Object> entry : beans.entrySet()) {
      Object instance = entry.getValue();
      Class<?> clazz = instance.getClass();

      if (clazz.isAnnotationPresent(MyController.class)) {
        MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
        String classPath = requestMapping.value();  //  /zjh  类上面路径

        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
          if (method.isAnnotationPresent(MyRequestMapping.class)) {
            MyRequestMapping mreq = method.getAnnotation(MyRequestMapping.class);
            //   /query--->  /james/query-->method
            String methodPath = mreq.value();
            handlerMap.put(classPath + methodPath, method);
          } else {
            continue;
          }
        }
      }
    }
  }

  public void doAutowired() {
    for (Map.Entry<String, Object> entry : beans.entrySet()) {
      Object instance = entry.getValue();

      Class<?> clazz = instance.getClass();

      if (clazz.isAnnotationPresent(MyController.class)) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
          if (field.isAnnotationPresent(MyAutowired.class)) {
            MyAutowired ma = field.getAnnotation(MyAutowired.class);
            String key = ma.value();
            field.setAccessible(true);
            try {
              field.set(instance, beans.get(key));
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            }
          } else {
            continue;
          }
        }
      } else {
        continue;
      }

    }
  }

  public void doInstance() {
    for (String className : classNames) {
      //第一式： com.zjh.....myController.class
      String cn = className.replace(".class", "");
      try {
        Class<?> clazz = Class.forName(cn);

        if (clazz.isAnnotationPresent(MyController.class)) {
          //就是控制controller类

          //就是controllerl类beans.put("",instance)
          Object instance = clazz.newInstance();
          MyRequestMapping myRequestMapping = clazz.getAnnotation(MyRequestMapping.class);
          String key = myRequestMapping.value(); // /zjh
          beans.put(key, instance);
        } else if (clazz.isAnnotationPresent(MyService.class)) {
          //就是service类beans.put("",instance)
          Object instance1 = clazz.newInstance();
          MyService myRequestMapping = clazz.getAnnotation(MyService.class);
          String key1 = myRequestMapping.value(); // /zjh
          beans.put(key1, instance1);
        } else {
          continue;
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  public void scanPackage(String basePachage) {
    //哪个路径 -->com.zjh com/zjh...
    URL url = this.getClass().getClassLoader()
        .getResource("/" + basePachage.replace("\\.", "/"));
    String fileStr = url.getFile(); //string 实打实的路径
    File file = new File(fileStr);

    String[] filesStr = file.list();
    for (String path : filesStr) {
      File filePath = new File(fileStr + path);
      if (filePath.isDirectory()) {
        scanPackage(basePachage + "." + path);
      } else {
        //list: com.zjh.....myController.class
        classNames.add(basePachage + "." + filePath.getName());
      }
    }

  }

  //业务请求相关的代码
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    this.doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String uri = request.getRequestURI();  //项目名  zjhmvc/james/query

    String context = request.getContextPath();
    String path = uri.replace(context, "");
    Method method = (Method) handlerMap.get(path);

    MyController instance = (MyController) beans.get("/" + path.split("/")[1]);

    //参数处理
    Object args[] = hand(request, response, method);
    try {
      method.invoke(instance, args);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private static Object[] hand(HttpServletRequest request, HttpServletResponse response,
      Method method) {
    //拿到当前待执行的方法有哪些参数
    Class<?>[] paramClazzs = method.getParameterTypes();
    //根据参数的个数，new一个参数的数组，将方法里的所有参数赋值到args来
    Object[] args = new Object[paramClazzs.length];

    int args_i = 0;
    int index = 0;
    for (Class<?> paramClazz : paramClazzs) {
      if (ServletRequest.class.isAssignableFrom(paramClazz)) {
        args[args_i++] = request;
      }
      if (ServletResponse.class.isAssignableFrom(paramClazz)) {
        args[args_i++] = response;
      }
      //从0-3判断有没有RequestParam注解，很明显paramClazz为0和1时，不是
      //当为2和3时为@RequestParam,需要解析
      Annotation[] paramAns = method.getParameterAnnotations()[index];
      if (paramAns.length > 0) {
        for (Annotation paramAn : paramAns) {
          if (MyRequestParam.class.isAssignableFrom(paramAn.getClass())) {
            MyRequestParam rp = (MyRequestParam) paramAn;
            //找到注解里的name和age
            args[args_i++] = request.getParameter(rp.value());
          }
        }
      }
      index++;
    }
    return args;
  }

}
