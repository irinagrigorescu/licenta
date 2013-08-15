package Helpers;

import java.util.Comparator;

public class MyUserComparator implements Comparator<User>{

	@Override
	public int compare(User b1, User b2) {
		// TODO Auto-generated method stub
		Float bb1 = Float.parseFloat(b1.getUserSimilarity());
		Float bb2 = Float.parseFloat(b2.getUserSimilarity());
		return (bb1 > bb2 ? -1 : (bb1 == bb2 ? 0 : 1));
	}

}
