package gtiDemo;

import java.util.Collection;
import java.util.List;

public class Jd {

	public void age() {
		/* 建立一个Collection */
		   UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
	        NavbarFormMap navbarFormMap = new NavbarFormMap();
	        navbarFormMap.put("userId", userFormMap.get("id"));
	        navbarFormMap.put("pageLevel", "1");
	        List<NavbarFormMap> mps = navbarMapper.findRes(navbarFormMap);
	        return mps;
	
		 String[] strings = {"A", "ahzngehnqinvg", "C", "D"};
		 Collection list = java.util.Arrays.asList(strings);
     
		 
		
		 for (Object str : list) {
		     System.out.println(str);
		 }
			int beernum =99;
			String word = "bottle";
			while (beernum>0){
			if (beernum == 1){
			word = "bootle";
			}

			System.out.print(beernum+""+word+"of beer on the wall");
			System.out.println(beernum+""+"of beer");
			System.out.println("Take one down.");
			System.out.println("passit around.");
			beernum = beernum -1;

			   if (beernum>0); {
			     System.out.println(beernum+""+"of beer on wall");
			   }
			   {
			     System.out.println("No more bottles of beer on the wall");
			   }
    
		 
			}
			public void name2() {

		 String[] strings = {"A", "B", "C", "D"};
		 Collection list = java.util.Arrays.asList(strings);
     
		 
		 /* �?始遍�? 12312s */
		 for (Object str : list) {
		     System.out.println(str); /* 依次输出“A”�?��?�B”�?��?�C”�?��?�D�? */

		 }
	
	public void higeht() {
		/* 建立一个Collection */
		 String[] strings = {"2123123", "E", "C", "D"};
		 Collection list = java.util.Arrays.asList(strings);
     
		 
		 /* 开始遍历 12312s */
		 for (Object str : list) {
		     System.out.println(str); /* 依次输出“A”、“B”、“C”、“D” */
		 }
	}

}
