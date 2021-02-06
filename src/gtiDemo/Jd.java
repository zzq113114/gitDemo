package gtiDemo;

import java.util.Collection;

public class Jd {

	public void name() {
		/* 建立一个Collection */
		 String[] strings = {"A", "B", "C","234234", "D","E"};
		 System.out.println(strings+"233453432423"); /* 依次输出“A”、“B”、“C”、“D” */
		 Collection listS = java.util.Arrays.asList(strings);
			/* 建立一个Collection */
		 String[] strings = {"A", "B", "C", "D"};
		 Collection list = java.util.Arrays.asList(strings);
     
		 
		 /* 开始遍历 12312s */
		 for (Object str : list) {
		     System.out.println(str); /* 依次输出“A”、“B”、“C”、“D” */
		 }
		 
		 /* 开始遍历 12312s */
		 for (Object str : listS) {
			 System.out.println(str+"Z张"); /* 依次输出“A”、“B”、“C”、“D” */
		     System.out.println(str+"233453432423"); /* 依次输出“A”、“B”、“C”、“D” */
		 }
	}
	}
