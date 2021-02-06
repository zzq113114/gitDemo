package gtiDemo;

import java.util.Collection;

public class Jd {

	public void name() {
		
		 String[] strings = {"A", "B", "C", "D"};
		 Collection list = java.util.Arrays.asList(strings);
    
		 
		
		 for (Object str : list) {
		     System.out.println(str); 
		 }
	}
	public void name2() {

		
		/* å»ºç«‹ä¸?ä¸ªCollection */
		 String[] strings = {"A", "B", "C", "D"};
		 Collection list = java.util.Arrays.asList(strings);
    
		 
		 /* å¼?å§‹éåŽ? 12312s */
		 for (Object str : list) {
		     System.out.println(str); /* ä¾æ¬¡è¾“å‡ºâ€œAâ€ã?â?œBâ€ã?â?œCâ€ã?â?œDâ€? */
		 }
	}
	public void nam5() {
		
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
	}
}
