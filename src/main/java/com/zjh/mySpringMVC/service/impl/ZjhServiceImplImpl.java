package com.zjh.mySpringMVC.service.impl;

import com.zjh.mySpringMVC.anno.MyService;
import com.zjh.mySpringMVC.service.ZjhService;

/**
 * @author ：ZouJiaHui
 * @date ：Created in 2020/4/17 11:00
 * @description：
 * @modified By：
 * @version: 1.0
 */
@MyService("ZjhServiceImplImpl") //iocMap.put("ZjhServiceImplImpl",new ZjhServiceImplImpl())
public class ZjhServiceImplImpl implements ZjhService {

  public String query(String name, String age) {
    return "{name=" + name + ",age=" + age + "}";
  }
}
