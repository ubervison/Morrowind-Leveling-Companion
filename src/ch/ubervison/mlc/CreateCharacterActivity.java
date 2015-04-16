package ch.ubervison.mlc;

import ch.ubervison.mlc.R;
import ch.ubervison.mlc.helper.DatabaseHelper;
import ch.ubervison.mlc.model.Character;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class CreateCharacterActivity extends ActionBarActivity {


	private final static String TAG = "NewCharScreen";
	private static final int CREATE_CHAR_OK = 0;
	private android.support.v7.app.ActionBar actionBar;

	private CheckBox[] skillIsMajor;
	private EditText[] skillLevels;
	private String[] skillNames;

	private int checkCount = 0;

	TableLayout skillTable;

	// Database Helper
	private DatabaseHelper dbh;


	/* (non-Javadoc)
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_char_screen);

		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater inflater = getLayoutInflater();

		skillIsMajor = new CheckBox[27];
		skillLevels = new EditText[27];
		skillNames = new String[]{"Acrobatics",
				"Alchemy",  
				"Alteration",
				"Armorer",
				"Athletics",
				"Axe",
				"Block",
				"Blunt Weapon",
				"Conjuration",
				"Destruction",
				"Enchant",
				"Hand-to-Hand",
				"Heavy Armor",
				"Illusion",
				"Light Armor",
				"Long Blade",
				"Marksman",
				"Medium Armor",
				"Mercantile",
				"Mysticism",
				"Restoration",
				"Security",
				"Short Blade",
				"Sneak",
				"Spear",
				"Speechcraft",
		"Unarmored"};
		skillTable = (TableLayout) findViewById(R.id.skillTable);
		for(int i = 0; i < skillNames.length; i++){
			TableRow tableRow = (TableRow) inflater.inflate(R.layout.create_character_skill_item, this.skillTable, false);
			TextView skillName = (TextView) tableRow.findViewById(R.id.skill_name);
			skillName.setText(skillNames[i]);
			EditText editText = (EditText) tableRow.findViewById(R.id.skill_level);
			this.skillLevels[i] = editText;
			CheckBox checkBox = (CheckBox)tableRow.findViewById(R.id.skill_is_major);
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						checkCount ++;
						if(checkCount > 10){
							Context context = getApplicationContext();
							Toast toast = Toast.makeText(context, "Only 10 major/minor skills allowed", Toast.LENGTH_SHORT);
							toast.show();
							buttonView.toggle();
							checkCount --;
						}
					}
					else{
						checkCount --;
					}
				}
			});
			checkBox.setChecked(false);
			this.skillIsMajor[i] = checkBox;
			this.skillTable.addView(tableRow);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		menu.findItem(R.id.new_char_button).setVisible(false);
		menu.findItem(R.id.undo_action).setVisible(false);
		menu.findItem(R.id.character_select_popup).setVisible(false);
		menu.findItem(R.id.sort_button).setVisible(false);
		menu.findItem(R.id.delete_char_button).setVisible(false);
		menu.findItem(R.id.about_button).setVisible(false);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.create_char_button :
			saveCharacter();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void saveCharacter(){
		if(checkCount < 10){
			Toast toast = Toast.makeText(getApplicationContext(), "You must select 10 major/minor skills", Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		for(EditText e : skillLevels){
			if(e.getText().toString().equals("")){
				Toast toast = Toast.makeText(getApplicationContext(), "You must set a value for all skills", Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			if(Integer.valueOf(e.getText().toString()) > 100){
				Toast toast = Toast.makeText(getApplicationContext(), "You cannot set skill levels above 100", Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
		}


		dbh = new DatabaseHelper(getApplicationContext());

		SQLiteDatabase db = dbh.getReadableDatabase();

		// get max character_id from database
		int maxId;

		Cursor cur = db.rawQuery("SELECT character_id FROM character WHERE character_id = (SELECT max(character_id) FROM character)", null);
		if(cur.moveToFirst()){
			maxId = cur.getInt(cur.getColumnIndex("character_id"));
		}
		else{
			maxId = 0;
		}
		cur.close();

		int charId = maxId+1;
		String name = ((EditText)findViewById(R.id.new_char_input)).getText().toString();
		if(name.equals("")){
			Toast toast = Toast.makeText(getApplicationContext(), "You must enter a name", Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		// Check if the character name already exists
		String charNamesQuery = "SELECT name FROM character";
		Cursor charNamesCur = db.rawQuery(charNamesQuery, null);

		while(charNamesCur.moveToNext()){
			if(charNamesCur.getString(charNamesCur.getColumnIndex("name")).equals(name)){
				Toast toast = Toast.makeText(getApplicationContext(), "The character "+name+" already exists", Toast.LENGTH_SHORT);
				toast.show();
				charNamesCur.close();
				return;
			}
		}

		charNamesCur.close();
		int[] actSkillValues = new int[27];
		int[] prevSkillValues = new int[27];
		int[] overSkillLevels = new int[27];

		int[] majorSkills = new int[10];
		int majorSkillsIndex = 0;
		for(int i = 0; i < skillIsMajor.length; i++){
			if(skillIsMajor[i].isChecked()){
				majorSkills[majorSkillsIndex] = i;
				majorSkillsIndex ++;
			}
		}

		for(int i = 0; i < 27; i++){
			actSkillValues[i] = Integer.valueOf(((EditText)skillLevels[i]).getText().toString());
			prevSkillValues[i] = actSkillValues[i];
			overSkillLevels[i] = 0;
		}
		String startingLevelText = ((EditText) findViewById(R.id.new_char_start_level)).getText().toString();
		int startingLevel = 1;
		if(!startingLevelText.equals("")){
			startingLevel = Integer.valueOf(((EditText)findViewById(R.id.new_char_start_level)).getText().toString());
			if(startingLevel < 1){
				Toast toast = Toast.makeText(getApplicationContext(), "Sarting level must be at least 1", Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
		}
		
		Character character = new Character(charId, name, actSkillValues, prevSkillValues, overSkillLevels, majorSkills, startingLevel);

		dbh.createCharacter(character);

		finish();
		String charName = ((EditText)findViewById(R.id.new_char_input)).getText().toString();
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, "New character " + charName + " saved.", Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	public void finish(){
		setResult(CREATE_CHAR_OK);
		super.finish();
	}
}
