package ch.ubervison.mlc.comparator;

import java.util.Comparator;

import ch.ubervison.mlc.model.Skill;

public class GoverningAttributeComparator implements Comparator<Skill>{

	@Override
	public int compare(Skill lhs, Skill rhs) {
		if(lhs.governingAttribute == rhs.governingAttribute){
			return 0;
		}
		else if(lhs.governingAttribute > rhs.governingAttribute){
			return 1;
		}
		else{
			return -1;
		}
	}

}
