package ch.ubervison.mlc.model;


public class Skill {
    
    public String name;   
    
    // Skill id (sql)
    // Skills are sorted alphabetically
    public long id;
    
    // Current skill level
    public int actLevel;
    
    // Skill level at last level up
    public int prevLevel;
    
    // Skill increase that carries on to the next level
    public int overLevel;
    
    /* Governing Attribute id :
     * 0 = Agility
     * 1 = Endurance
     * 2 = Intelligence
     * 3 = Personality
     * 4 = Speed
     * 5 = Strength
     * 6 = Willpower*/
    public int governingAttribute;
    
    public boolean isMajor;
    
    public Skill(int id, int actLevel, int prevLevel, int overLevel, int governingAttribute, boolean isMajor){
        this.id = id;
        this.actLevel = actLevel;
        this.prevLevel = prevLevel;
        this.overLevel = overLevel;
        this.governingAttribute = governingAttribute;
        this.isMajor = isMajor;
    }
    
    public long getId(){
        return this.id;
    }
    
    public String toString(){
        return this.id+": "+this.name+", "+this.governingAttribute+", "+this.isMajor;
    }
    
    
    
}
