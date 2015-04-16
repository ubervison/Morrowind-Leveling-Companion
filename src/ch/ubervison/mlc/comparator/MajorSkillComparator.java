package ch.ubervison.mlc.comparator;

import java.util.Comparator;

import ch.ubervison.mlc.model.Skill;

public class MajorSkillComparator implements Comparator<Skill> {

	@Override
	public int compare(Skill lhs, Skill rhs) {
		if(lhs.isMajor && rhs.isMajor){
			return 0;
		}
		else if(lhs.isMajor && !rhs.isMajor){
			return -1;
		}
		else{
			return 1;
		}
	}

}
