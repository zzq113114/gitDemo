package gtiDemo;

import java.util.Collection;

public class Jd {

	public void name() {
		/* 建立一个Collection */
		 String[] strings = {"A", "B", "C", "D","E"};
		 Collection listS = java.util.Arrays.asList(strings);
     
		 
		 /* 开始遍历 12312s */
		 for (Object str : listS) {
		     System.out.println(str); /* 依次输出“A”、“B”、“C”、“D” */
		 }
	}
}
