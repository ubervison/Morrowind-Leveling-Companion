package ch.ubervison.mlc.model;

public class Character {

    private String name;
    
    // Character id (sql)
    private long id;
    
    // Current skill levels. The array index is the skill id.
    private int[] actSkillLevels;
    
    // Skill levels at last level up. The array index is the skill id.
    private int[] prevSkillLevels;
    
    // Skill increases that carry on to the nest level. The array index is the skill id.
    private int[] overSkillLevels;
    
    // An array containing the id's of the major / minor skills.
    private int[] majorSkills;
    
    // Character level
    private int level;
    
    public Character(long id, String name, int[] actSkillLevels, int[] prevSkillLevels, int[] overSkillLevels, int[] majorSkills, int level){
        this.id = id;
        this.name = name;
        this.actSkillLevels = actSkillLevels;
        this.prevSkillLevels = prevSkillLevels;
        this.overSkillLevels = overSkillLevels;
        this.majorSkills = majorSkills;
        this.level = level;
    }
    
    public String getName(){
        return this.name;
    }

    public long getId(){
        return this.id;
    }
    
    public int[] getActSkillLevels(){
        return this.actSkillLevels;
    }
    
    public int[] getPrevSkillLevels(){
        return this.prevSkillLevels;
    }
    
    public int[] getOverSkillLevels(){
        return this.overSkillLevels;
    }
    
    public int[] getMajorSkills(){
        return this.majorSkills;
    }
    
    public boolean isMajorSkill(int skillId){
        for(int i = 0; i < majorSkills.length; i++){
            if(majorSkills[i] == skillId)
                return true;
        }
        return false;
    }

	public int getLevel() {
		return this.level;
	}
    
}
