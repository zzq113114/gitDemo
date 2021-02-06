package gtiDemo;

public class De {
	public static void main (String[] args){
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
