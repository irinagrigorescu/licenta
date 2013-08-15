package Helpers;

import java.util.Comparator;

public class MyTagComparator implements Comparator<Tag>{

	@Override
	public int compare(Tag t1, Tag t2) {
		String tt1 = t1.getTagName();
		String tt2 = t2.getTagName();
		return tt1.compareTo(tt2);
	}
}
