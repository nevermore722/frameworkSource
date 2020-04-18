package com.zjh.mySpringMVC.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：ZouJiaHui
 * @date ：Created in 2020/4/17 11:19
 * @description：
 * @modified By：
 * @version: 1.0
 */
@Target(ElementType.FIELD) //类的变量上
@Retention(RetentionPolicy.RUNTIME)
@Documented  //javadoc
public @interface MyAutowired {

  String value() default "";

}
