package ch.ubervison.mlc.helper;

import java.util.ArrayList;
import java.util.List;

import ch.ubervison.mlc.model.Character;
import ch.ubervison.mlc.model.Skill;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	// debugging cursors
	int curCount = 0;

	// Logcat tag
	private static final String TAG = "DatabaseHelper";

	// Database ver§ion
	private static final int DATABASE_VERSION = 1;

	// Database name
	private static final String DATABASE_NAME = "mlc_beta";

	// Table names
	private static final String TABLE_CHARACTER = "character";
	private static final String TABLE_SKILL_GAIN = "skill_gain";
	private static final String TABLE_MAJOR_SKILLS = "major_skills";
	private static final String TABLE_HISTORY = "history";
	// Common column names
	private static final String KEY_CHARACTER_ID = "character_id";
	private static final String KEY_CHARACTER_NAME = "name";
	private static final String KEY_SKILL_ID = "skill_id";
	private static final String KEY_CHARACTER_LEVEL = "level";
	
	// skill_gain column names
	private static final String KEY_SKILL_GAIN = "gained";

	// history column names
	private static final String KEY_EVENT_ID = "event_id";
	private static final String KEY_ACTION_ID = "action_id";

	// Table create statements
	// character table create statement
	private static final String CREATE_TABLE_CHARACTER = "CREATE TABLE "+TABLE_CHARACTER+"("
			+KEY_CHARACTER_ID+" INTEGER PRIMARY KEY, "
			+KEY_CHARACTER_LEVEL+" INTEGER, "
			+KEY_CHARACTER_NAME+" TEXT"+
			")";

	// skill_gain table create statement
	private static final String CREATE_TABLE_SKILL_GAIN = "CREATE TABLE "+TABLE_SKILL_GAIN+"("
			+KEY_EVENT_ID+" INTEGER,"
			+KEY_CHARACTER_ID+" INTEGER,"
			+KEY_SKILL_ID+" INTEGER,"
			+KEY_CHARACTER_LEVEL+" INTEGER,"
			+KEY_SKILL_GAIN+" INTEGER,PRIMARY KEY("+KEY_EVENT_ID+"))";

	// major_skills table create statement
	private static final String CREATE_TABLE_MAJOR_SKILLS = "CREATE TABLE "+TABLE_MAJOR_SKILLS+"("
			+KEY_CHARACTER_ID+" INTEGER,"
			+KEY_SKILL_ID+" INTEGER,PRIMARY KEY("+KEY_CHARACTER_ID+","+KEY_SKILL_ID+"))";

	/* history table create statement
	 * 
	 * HISTORY ACTION CODES:
	 * 0 : skill increase
	 * 1 : level up
	 */
	private static final String CREATE_TABLE_HISTORY = "CREATE TABLE "+TABLE_HISTORY+"("
			+KEY_EVENT_ID+" INTEGER,"
			+KEY_ACTION_ID+" INTEGER,"
			+KEY_CHARACTER_ID+" INTEGER,"
			+KEY_SKILL_ID+" INTEGER,"
			+KEY_CHARACTER_LEVEL+" INTEGER,PRIMARY KEY("+KEY_EVENT_ID+"))";


	private Context context;

	public DatabaseHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_CHARACTER);
		db.execSQL(CREATE_TABLE_SKILL_GAIN);
		db.execSQL(CREATE_TABLE_MAJOR_SKILLS);
		db.execSQL(CREATE_TABLE_HISTORY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_CHARACTER);
		onCreate(db);
	}

	public long createCharacter(Character character){
		Log.d(TAG, CREATE_TABLE_CHARACTER);
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues characterValues = new ContentValues();
		characterValues.put(KEY_CHARACTER_ID, Long.toString(character.getId()));
		characterValues.put(KEY_CHARACTER_LEVEL, Integer.toString(character.getLevel()));
		characterValues.put(KEY_CHARACTER_NAME, character.getName());
		long character_id = db.insert(TABLE_CHARACTER, null, characterValues);

		int[] actSkillLevels = character.getActSkillLevels();
		ContentValues skillValues = new ContentValues();
		for(int i = 0; i < actSkillLevels.length; i++){
			skillValues.put(KEY_CHARACTER_ID, Long.toString(character.getId()));
			skillValues.put(KEY_SKILL_ID, i);
			skillValues.put(KEY_CHARACTER_LEVEL, 0);
			skillValues.put(KEY_SKILL_GAIN, actSkillLevels[i]);
			db.insert(TABLE_SKILL_GAIN, null, skillValues);
		}

		// Array containing skill id's
		int majorSkills[] = character.getMajorSkills();
		ContentValues majorSkillsValues = new ContentValues();
		for(int i = 0; i < majorSkills.length; i++){
			majorSkillsValues.put(KEY_CHARACTER_ID, Long.toString(character.getId()));
			majorSkillsValues.put(KEY_SKILL_ID, majorSkills[i]);
			db.insert(TABLE_MAJOR_SKILLS, null, majorSkillsValues);
		}
		return character_id;
	}

	public List<Skill> getSkillsForCharacter(String character){
		SQLiteDatabase db = this.getReadableDatabase();
		int charId = getCharacterId(character);
		Log.d(TAG, "charId = "+charId);
		int maxLevel = 0;
		Cursor curJustLeveledUp = db.rawQuery("SELECT MAX(level) FROM skill_gain WHERE character_id = "+charId, null);
		if(curJustLeveledUp.moveToFirst())
			maxLevel = curJustLeveledUp.getInt(0);
		curJustLeveledUp.close();

		List<Skill> skills = new ArrayList<Skill>(27);
		for(int i = 0; i < 27; i++){
			int skillId = i;

			int actSkillLevel = 0;
			int prevSkillLevel = 0;
			int overLevel = 0;

			if(maxLevel < getCharacterLevel(character)){
				actSkillLevel = getActSkillLevel(charId, skillId);
				prevSkillLevel = actSkillLevel;
				overLevel = 0;
			}

			else{
				actSkillLevel = getActSkillLevel(charId, skillId);
				prevSkillLevel = getPrevSkillLevel(charId, skillId);
				if(prevSkillLevel == 0){
					prevSkillLevel = actSkillLevel;
				}
				overLevel = actSkillLevel - prevSkillLevel;
			}

			int governingAttribute;
			boolean isMajor = false;
			switch(skillId){
			case 6: case 14: case 16: case 23:
				// Agility
				governingAttribute = 0;
				break;
			case 12: case 17: case 24:
				// Endurance
				governingAttribute = 1;
				break;
			case 1: case 8: case 10: case 21:
				// Intelligence
				governingAttribute = 2;
				break;
			case 13: case 18: case 25:
				// Personality
				governingAttribute = 3;
				break;
			case 4: case 11: case 22: case 26:
				// Speed
				governingAttribute = 4;
				break;
			case 0: case 3: case 5: case 7: case 15:
				// Strength
				governingAttribute = 5;
				break;
			case 2: case 9: case 19: case 20:
				// Willpower
				governingAttribute = 6;
				break;
			default:
				throw new IllegalArgumentException("Wrong skill id");    
			}
			Cursor curIsMajor = db.rawQuery("SELECT skill_id from major_skills WHERE character_id = "+charId+" AND skill_id = "+i, null);
			if(curIsMajor.getCount() > 0){
				isMajor = true;
			}
			curIsMajor.close();
			skills.add(new Skill(skillId, actSkillLevel, prevSkillLevel, overLevel, governingAttribute, isMajor));
		}
		db.close();
		return skills;
	}

	// get current skill level
	public int getActSkillLevel(int charId, int skillId){
		SQLiteDatabase db = this.getReadableDatabase();
		int actSkillLevel = 0;
		String query = "SELECT gained FROM skill_gain WHERE character_id = "+charId+" AND skill_id = "+skillId;
		Cursor curActSkillLevel = db.rawQuery(query, null);
		try{
			while(curActSkillLevel.moveToNext()){
				int gained = curActSkillLevel.getInt(curActSkillLevel.getColumnIndex("gained"));
				actSkillLevel += gained;
			}
		} finally{
			curActSkillLevel.close(); 
		}


		return actSkillLevel;
	}

	// get skill level before levelup
	public int getPrevSkillLevel(int charId, int skillId){
		SQLiteDatabase db = this.getReadableDatabase();
		int prevSKillLevel = 0;
		String query = "SELECT gained FROM skill_gain WHERE character_id = "+charId+" AND skill_id = "+skillId+" AND level < (SELECT max(level) FROM skill_gain)";
		Cursor curPrevSkillLevel = db.rawQuery(query, null);
		try{
			while(curPrevSkillLevel.moveToNext()){
				int gained = curPrevSkillLevel.getInt(curPrevSkillLevel.getColumnIndex("gained"));
				prevSKillLevel += gained;
			}
		} finally{
			curPrevSkillLevel.close();
		}

		if(prevSKillLevel == 0){
			prevSKillLevel = getActSkillLevel(charId, skillId);
		}
		return prevSKillLevel;
	}

	public int getCharacterId(String character){
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT character_id FROM character WHERE name = '"+character+"'";
		Log.d(TAG, query);
		int charId = 0;
		Cursor curCharId = db.rawQuery(query, null);
		if(curCharId.moveToFirst()){
			charId = curCharId.getInt(curCharId.getColumnIndex("character_id"));
		}
		curCharId.close();
		return charId;
	}

	public int getCharacterLevel(String character){
		int charId = getCharacterId(character);
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT level from character WHERE character_id = "+charId;
		Cursor curLevel = db.rawQuery(query, null);
		int level = 0;
		if(curLevel.moveToFirst()){
			level = curLevel.getInt(curLevel.getColumnIndex("level"));
		}
		curLevel.close();
		return level;
	}

	public void increaseSkill(long id, String character) {
		Log.d(TAG, "increase skill, id = "+id);
		SQLiteDatabase db = this.getWritableDatabase();
		int charId = getCharacterId(character);
		int charLevel = getCharacterLevel(character);
		// Get character level for this skill. If 0, must insert a new row.
		String getCHarLevelForThisSkillQuery = "SELECT level FROM skill_gain WHERE character_id = "+charId+" AND skill_id = "+id;
		Cursor curLevelForThisSkill = db.rawQuery(getCHarLevelForThisSkillQuery, null);
		int levelForThisSkill = 0;
		try{
			int max = 0;
			while(curLevelForThisSkill.moveToNext()){
				int current = curLevelForThisSkill.getInt(0);
				if(current > max){
					max = current;
				}
				levelForThisSkill = max;
			}
		}
		finally{
			curLevelForThisSkill.close();
		}
		Log.d(TAG, "Char level for this skill : "+levelForThisSkill);
		Log.d(TAG, "Char level : "+charLevel);

		// When the character was just created
		if (levelForThisSkill == 0){
			Log.d(TAG, "Case 1");
			ContentValues newValues = new ContentValues();
			newValues.put("character_id", charId);
			newValues.put("skill_id", id);
			newValues.put("level", charLevel);
			newValues.put("gained", 1);
			db.insert(TABLE_SKILL_GAIN, null, newValues);
		}
		// Check char level for this skill against actual character level
		// if char level is same as char level for this skill, update value
		else if (charLevel == levelForThisSkill){
			Log.d(TAG, "Case 2");
			String formerGainQuery = "SELECT gained FROM skill_gain WHERE character_id = "+charId+" AND skill_id = "+id;
			Cursor curFormerGain = db.rawQuery(formerGainQuery, null);
			curFormerGain.moveToLast();
			int formerGain = curFormerGain.getInt(curFormerGain.getColumnIndex("gained"));
			curFormerGain.close();
			int gained = formerGain + 1;
			String updateQuery = "UPDATE skill_gain SET gained = "+gained+" WHERE character_id = "+charId+" AND skill_id = "+id+" AND level = "+charLevel;
			Cursor cur = db.rawQuery(updateQuery, null);
			cur.moveToFirst();
			cur.close();
		}
		// if char level is different, insert a new row
		else if(charLevel > levelForThisSkill){
			Log.d(TAG, "Case 3");
			ContentValues newValues = new ContentValues();
			newValues.put("character_id", charId);
			newValues.put("skill_id", id);
			newValues.put("level", charLevel);
			newValues.put("gained", 1);
			db.insert(TABLE_SKILL_GAIN, null, newValues);
		}

		// record operation in history
		ContentValues skillHistoryValues = new ContentValues();
		skillHistoryValues.put("action_id", 0); // action_id 0 is skill increase
		skillHistoryValues.put("character_id", charId);
		skillHistoryValues.put("skill_id", id);
		skillHistoryValues.put("level", charLevel);
		db.insert(TABLE_HISTORY, null, skillHistoryValues);
	}



	public int getMajorSkillIncreases(String character){

		SQLiteDatabase db = this.getReadableDatabase();
		int charId = getCharacterId(character);
		int increase = 0;
		String query = "SELECT * FROM skill_gain WHERE character_id = "+charId+" AND level > 0";
		Cursor curMajorIncreases = db.rawQuery(query, null);
		if(curMajorIncreases.getCount() == 0){
			return 0;
		}
		try{
			while(curMajorIncreases.moveToNext()){
				int skillId = curMajorIncreases.getInt(curMajorIncreases.getColumnIndex("skill_id"));
				if(isSkillMajor(charId, skillId)){
					int temp = curMajorIncreases.getInt(curMajorIncreases.getColumnIndex("gained"));
					increase += temp;
				}
			}
			
			String startingCharLevelQuery = "SELECT MIN(level) FROM skill_gain WHERE character_id = "+charId+" AND level > 0";
			Cursor curStartingLevel = db.rawQuery(startingCharLevelQuery, null);
			int startingLevel = 1;
			if(curStartingLevel.moveToFirst()){
				startingLevel = curStartingLevel.getInt(0);
			}
			int charLevel = getCharacterLevel(character) - startingLevel + 1;
			increase -= 10*(charLevel - 1);
		}
		finally{
			curMajorIncreases.close();
		}
		return increase;
	} 

	public boolean isSkillMajor(int charId, int skillId){
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT * FROM major_skills WHERE character_id = "+charId+" AND skill_id = "+skillId;
		Cursor cur = db.rawQuery(query, null);
		boolean isSkillMajor = cur.moveToFirst();
		cur.close();
		return isSkillMajor;
	}

	public int getMultiplierForAttribute(String character, int attrId) {
		int charId = getCharacterId(character);
		int sumForAttr = 0;
		int charLevel = getCharacterLevel(character);
		// get highest character level for skills
		SQLiteDatabase db = getReadableDatabase();
		String query = "SELECT MAX(level) FROM skill_gain WHERE character_id = "+charId;
		Log.d(TAG, query);
		Cursor cur = db.rawQuery(query, null);
		int maxLevel = 0;
		if(cur.moveToFirst())
			maxLevel = cur.getInt(0);
		cur.close();

		if(charLevel > maxLevel)
			return 0;

		int[] governedSkills = null;
		switch(attrId){
		case 0:
			governedSkills = new int[]{6, 14, 16, 23};
			break;
		case 1:
			governedSkills = new int[]{12, 17, 24};
			break;
		case 2:
			governedSkills = new int[]{1, 8, 10, 21};
			break;
		case 3:
			governedSkills = new int[]{13, 18, 25};
			break;
		case 4:
			governedSkills = new int[]{4, 11, 22, 26};
			break;
		case 5:
			governedSkills = new int[]{0, 3, 5, 7, 15};
			break;
		case 6:
			governedSkills = new int[]{2, 9, 19, 20};
			break;
		default:
			throw new Error();
		}
		for(int i = 0; i < governedSkills.length; i++){
			sumForAttr += getActSkillLevel(charId, governedSkills[i]) - getPrevSkillLevel(charId, governedSkills[i]);
		}
		return sumForAttr;
	}

	public void levelup(String character){
		int charId = getCharacterId(character);
		int level = getCharacterLevel(character) + 1;
		// update level in character table
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "UPDATE character SET level = "+level+" WHERE character_id = "+charId;
		Cursor cur = db.rawQuery(query, null);
		cur.moveToFirst();
		cur.close();
		// record operation in history
		ContentValues levelHistoryValues = new ContentValues();
		levelHistoryValues.put("action_id", 1);
		levelHistoryValues.put("character_id", charId);
		levelHistoryValues.put("skill_id", 666);
		levelHistoryValues.put("level", level-1); // the value that is recorded is the level before leveling up
		db.insert(TABLE_HISTORY, null, levelHistoryValues);
	}

	public void undo(boolean charListIsEmpty, String character){
		if(charListIsEmpty){
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		String undoQuery = "SELECT * FROM history WHERE event_id = (SELECT MAX(event_id) FROM history)";
		Cursor lastActionCursor = db.rawQuery(undoQuery, null);
		// abort if history is empty
		if(!lastActionCursor.moveToFirst()){
			return;
		}
		int charId = getCharacterId(character);
		int character_id = lastActionCursor.getInt(lastActionCursor.getColumnIndex("character_id"));
		if(charId != character_id){
			return;
		}
		int skill_id = lastActionCursor.getInt(lastActionCursor.getColumnIndex("skill_id"));
		int history_id = lastActionCursor.getInt(lastActionCursor.getColumnIndex("event_id"));
		Log.d(TAG, "HISTORY_ID MOTHAFUCKA = "+history_id);
		switch(lastActionCursor.getInt(1)){
		case 0: // skill gain
			// need to know if newly created row or updated value
			String skillGainQuery = "SELECT * FROM skill_gain WHERE event_id = (SELECT MAX(event_id) FROM skill_gain WHERE skill_id = "+skill_id+" AND character_id = "+character_id+")";
			Cursor skillGainCursor = db.rawQuery(skillGainQuery, null);
			skillGainCursor.moveToFirst();
			int gained = skillGainCursor.getInt(skillGainCursor.getColumnIndex("gained"));
			int event_id = skillGainCursor.getInt(skillGainCursor.getColumnIndex("event_id"));
			Log.d(TAG, "EVENT_ID MOTHAFUCKA = "+event_id);
			Log.d(TAG, "history gained = "+ gained);
			// newly created row
			if(gained == 1){
				db.delete("skill_gain", "event_id = "+event_id, null);
			}
			// updated value
			else{
				gained--;
				String updateValueQuery = "UPDATE skill_gain SET gained = "+gained+" WHERE event_id = "+event_id;
				Cursor cur = db.rawQuery(updateValueQuery, null);
				cur.moveToFirst();
				cur.close();
			}
			skillGainCursor.close();
			break;

		case 1: // level up
			int prevLevel = lastActionCursor.getInt(lastActionCursor.getColumnIndex("level"));
			String updateLevelQuery = "UPDATE character SET level = "+prevLevel+" WHERE character_id = "+character_id;
			Cursor cur = db.rawQuery(updateLevelQuery, null);
			cur.moveToFirst();
			cur.close();
			break;

		default: 
			throw new UnsupportedOperationException("action_id not valid");
		}

		db.delete("history", "event_id = "+history_id, null);
		Log.d(TAG, "deleted row with id = "+history_id);
		lastActionCursor.close();
	}

	public void deleteCharacter(String character){
		int charId = getCharacterId(character);
		SQLiteDatabase db = getWritableDatabase();
		db.delete("character", "character_id = "+charId, null);
		db.delete("skill_gain", "character_id = "+charId, null);
		db.delete("history", "character_id = "+charId, null);
		db.delete("major_skills", "character_id = "+charId, null);
	}
}
