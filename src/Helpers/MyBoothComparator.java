package Helpers;

import java.util.Comparator;


public class MyBoothComparator implements Comparator<Booth>{

	@Override
	public int compare(Booth b1, Booth b2) {
		// TODO Auto-generated method stub
		Float bb1 = Float.parseFloat(b1.getSimilarity());
		Float bb2 = Float.parseFloat(b2.getSimilarity());
		return (bb1 > bb2 ? -1 : (bb1 == bb2 ? 0 : 1));
	}
}
