package com.zjh.mySpringMVC.controller;

import com.zjh.mySpringMVC.anno.MyAutowired;
import com.zjh.mySpringMVC.anno.MyController;
import com.zjh.mySpringMVC.anno.MyRequestMapping;
import com.zjh.mySpringMVC.anno.MyRequestParam;
import com.zjh.mySpringMVC.service.ZjhService;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ：ZouJiaHui
 * @date ：Created in 2020/4/17 11:02
 * @description：
 * @modified By：
 * @version: 1.0
 */
@MyController
@MyRequestMapping("/zjh")
public class ZjhController {

  @MyAutowired("zjhServiceImpl")
  private ZjhService zjhService;

  public void query(HttpServletRequest request, HttpServletResponse response,
      @MyRequestParam("name") String name, @MyRequestParam("age") String age) {
    try {
      PrintWriter pw = response.getWriter();
      String result = zjhService.query(name, age);
      pw.write(result);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
