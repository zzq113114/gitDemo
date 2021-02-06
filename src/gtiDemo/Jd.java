package gtiDemo;

import java.util.Collection;

public class Jd {

	public void name() {
		/* 建立一个Collection */
		 String[] strings = {"A", "B", "C", "D"};
		 Collection list = java.util.Arrays.asList(strings);
     
		 
		 /* 开始遍历 12312s */
		 for (Object str : list) {
		     System.out.println(str); /* 213123输出“A”、“B”、“C”、“D” */
		 }
	}
}
