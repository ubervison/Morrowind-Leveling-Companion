package ch.ubervison.mlc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ch.ubervison.mlc.R;
import ch.ubervison.mlc.adapter.SkillListAdapter;
import ch.ubervison.mlc.comparator.GoverningAttributeComparator;
import ch.ubervison.mlc.comparator.MajorSkillComparator;
import ch.ubervison.mlc.comparator.SkillNameComparator;
import ch.ubervison.mlc.helper.DatabaseHelper;
import ch.ubervison.mlc.model.Skill;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity{

	private static final int CREATE_CHARACTER = 0;
	private static final String TAG = "MainActivity";

	// Views
	private ListView skillList;
	private TextView noCharacterView;
	private LinearLayout levelupPane;

	private Menu menu;

	private android.support.v7.app.ActionBar actionBar;

	// The name of the character currently loaded
	String character;

	// Skill list and adapter
	SkillListAdapter adapter;
	List<Skill> skills = new ArrayList<Skill>();

	// Character Selection Popup
	CharSequence[] character_array = new CharSequence[0];
	ArrayList<String> array_spinner = new ArrayList<String>();

	// The skill sorting comnparator currently in use
	Comparator<Skill> skillSortComparator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("MLC");
		actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setLogo(R.drawable.mlc_launcher);
		setContentView(R.layout.activity_main);
		skillList = (ListView) findViewById(R.id.skillList_listView);
		noCharacterView = (TextView) findViewById(R.id.textIfIsEmpty);
		levelupPane = (LinearLayout) findViewById(R.id.levelup_pane);
		levelupPane.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());
				dbh.levelup(character);
				dbh.close();
				levelupPane.setVisibility(View.GONE);
				updateMultipliers();
				updateListView();
				updateCharNameInActionBar();
			}
		});

		// Getting character list from database and loading the first one, if it exists
		updateCharacterList();
		if(!array_spinner.isEmpty()){
			character = character_array[0].toString();
		}

		// Displaying the loaded character and and related multipliers
		loadCharacter();
		updateMultipliers();

		// This list must be in alphabetical order. The index of the String corresponds to the Skill id.
		String[] skillNames = {"Acrobatics",
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

		// Setting up the skill list adapter and skill comparator
		adapter = new SkillListAdapter(getApplicationContext(), skills, skillNames);
		skillSortComparator = new SkillNameComparator();
		adapter.sort(skillSortComparator);
		levelupPane.setVisibility(View.GONE);
		skillList.setAdapter(adapter);
		if(skills.isEmpty()){
			skillList.setVisibility(View.GONE);
			noCharacterView.setVisibility(View.VISIBLE);
		}

		// Set listener on skill item click
		skillList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());
				dbh.increaseSkill(id, character);
				dbh.close();
				adapter.increaseSkill(id);
				updateListView();
				updateMultipliers();
			}

		});

	}

	// Creating the action bar
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		menu.findItem(R.id.create_char_button).setVisible(false);
		updateCharNameInActionBar();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onResume(){
		super.onResume();
		updateCharacterList();
	}

	@Override
	public void onPause(){
		super.onPause();

	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){

		case R.id.new_char_button :
			Intent intent = new Intent(this, CreateCharacterActivity.class);
			startActivityForResult(intent, CREATE_CHARACTER);
			return true;

		case R.id.undo_action :
			DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());
			dbh.undo(array_spinner.isEmpty(), character);
			updateListView();
			updateMultipliers();
			adapter.sort(skillSortComparator);
			dbh.close();
			return false;

		case R.id.character_select_popup : 
			AlertDialog.Builder characterSelectionBuilder = new AlertDialog.Builder(MainActivity.this);
			characterSelectionBuilder.setTitle("Select character")
			.setItems(character_array, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					character = character_array[which].toString();
					updateCharNameInActionBar();
					updateListView();
					updateMultipliers();
				}
			});
			characterSelectionBuilder.show();
			return false;

		case R.id.delete_char_button : 
			AlertDialog.Builder deleteAlertBuilder = new AlertDialog.Builder(MainActivity.this);
			deleteAlertBuilder.setMessage("Do you really want to delete '"+character+"'?")
			.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());
					dbh.deleteCharacter(character);
					updateCharacterList();
					if(!array_spinner.isEmpty()){
						String temp = character_array[character_array.length-1].toString();
						if(temp.equals(character)){
							return;
						}
					}
					updateCharNameInActionBar();
					updateListView();
					updateMultipliers();
					dbh.close();

				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			deleteAlertBuilder.show();
			return false;

		case R.id.sort_button :
			final CharSequence[] sortOptionsArray = {"Alphabetically", "By Attribute", "By Major Skill"};
			AlertDialog.Builder sortAlertBuilder = new AlertDialog.Builder(MainActivity.this);
			sortAlertBuilder.setTitle("Sort")
			.setItems(sortOptionsArray, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which){
					case 0:
						skillSortComparator = new SkillNameComparator();
						break;
					case 1:
						skillSortComparator = new GoverningAttributeComparator();
						break;
					case 2:
						skillSortComparator = new MajorSkillComparator();
						break;
					default:
						throw new Error("Invalid comparator");
					}
					updateListView();
				}
			});
			sortAlertBuilder.show();
			return false;
			
		case R.id.about_button : 
			AlertDialog.Builder aboutAlertBuilder = new AlertDialog.Builder(MainActivity.this);
			aboutAlertBuilder.setTitle("About MLC").setMessage("Morrowind Leveling Companion helps you keep track of your character's progress in TES III : Morrowind \n\n"
					+ "Copyright © 2014 ubervison basile@ubervison.ch \n\n"
					+ "This work is free. You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.")
					.setPositiveButton("Done", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					}).show();
			return false;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == 0 && requestCode == 0){
			updateCharacterList();
			if(!array_spinner.isEmpty()){
				character = character_array[character_array.length-1].toString();
				updateCharNameInActionBar();
				updateListView();
				updateMultipliers();
			}
		}
	}

	// Read character list from database and updates character_array and array_spinner
	public void updateCharacterList(){
		SQLiteOpenHelper dbh = new DatabaseHelper(getApplicationContext());
		SQLiteDatabase db = dbh.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT name FROM character", null);
		array_spinner.clear();
		if(cur.getCount() != 0){
			while(cur.moveToNext()){
				array_spinner.add(cur.getString(cur.getColumnIndex("name")));
			}
		}
		cur.close();
		dbh.close();
		character_array = array_spinner.toArray(new CharSequence[array_spinner.size()]);
	}

	// Updates the skill list view
	public void updateListView() {
		loadCharacter();
		adapter.notifyDataSetChanged();
		adapter.sort(skillSortComparator);
	}

	// Updates the displayed value of the multipliers according to values stored in the database
	public void updateMultipliers() {	// do not display value if 0
		DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());

		TextView agl = (TextView) findViewById(R.id.mult_agl_value);
		int aglValue = dbh.getMultiplierForAttribute(character, 0);
		agl.setText((aglValue == 0) ? "" : Integer.toString(aglValue));

		TextView end = (TextView) findViewById(R.id.mult_end_value);
		int endValue = dbh.getMultiplierForAttribute(character, 1);
		end.setText((endValue == 0) ? "" : Integer.toString(endValue));

		TextView INT = (TextView) findViewById(R.id.mult_int_value);
		int intValue = dbh.getMultiplierForAttribute(character, 2);
		INT.setText((intValue == 0) ? "" : Integer.toString(intValue));

		TextView per = (TextView) findViewById(R.id.mult_per_value);
		int perValue = dbh.getMultiplierForAttribute(character, 3);
		per.setText((perValue == 0) ? "" : Integer.toString(perValue));

		TextView spd = (TextView) findViewById(R.id.mult_spd_value);
		int spdValue = dbh.getMultiplierForAttribute(character, 4);
		spd.setText((spdValue == 0) ? "" : Integer.toString(spdValue));

		TextView str = (TextView) findViewById(R.id.mult_str_value);
		int strValue = dbh.getMultiplierForAttribute(character, 5);
		str.setText((strValue == 0) ? "" : Integer.toString(strValue));

		TextView wil = (TextView) findViewById(R.id.mult_wil_value);
		int wilValue = dbh.getMultiplierForAttribute(character, 6);
		wil.setText((wilValue == 0) ? "" : Integer.toString(wilValue));

		TextView progress = (TextView) findViewById(R.id.progress_value);
		progress.setText(dbh.getMajorSkillIncreases(character)+"/10");
		dbh.close();

	}

	// Load the character in the database based on the value of the String "character"
	private void loadCharacter() {
		DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());
		if(array_spinner.isEmpty()){
			skillList.setVisibility(View.INVISIBLE);
			noCharacterView.setVisibility(View.VISIBLE);
		}
		else{
			skillList.setVisibility(View.VISIBLE);
			noCharacterView.setVisibility(View.GONE );
		}
		if(skills == null){
			skills = dbh.getSkillsForCharacter(character);
		}
		else{
			List<Skill> newSkills = dbh.getSkillsForCharacter(character);
			skills.clear();
			for(int i = 0; i < 27; i++){
				Skill s = newSkills.get(i);
				skills.add(s);
			}
		}
		if(dbh.getMajorSkillIncreases(character) >= 10){
			levelupPane.setVisibility(View.VISIBLE);
		}
		else{
			levelupPane.setVisibility(View.GONE);
		}
		dbh.close();
	}

	// Update the name displayed in the action bar to reflect the name of the currently loaded character
	private void updateCharNameInActionBar(){
		if(array_spinner.isEmpty()){
			menu.findItem(R.id.character_select_popup).setVisible(false);
			return;
		}
		else{
			menu.findItem(R.id.character_select_popup).setVisible(true);
		}
		DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());
		MenuItem characterSelectionItem = menu.findItem(R.id.character_select_popup);
		characterSelectionItem.setTitle(character+" ("+dbh.getCharacterLevel(character)+")");
	}
}
