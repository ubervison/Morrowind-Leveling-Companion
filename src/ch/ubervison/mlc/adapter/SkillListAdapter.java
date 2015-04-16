package ch.ubervison.mlc.adapter;

import java.util.Comparator;
import java.util.List;

import ch.ubervison.mlc.R;
import ch.ubervison.mlc.model.Skill;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SkillListAdapter extends ArrayAdapter<Skill> {

	private static final String TAG = "SkillListAdapter";
	private List<Skill> skills;
	private String[] skillNames;
	private Context context;
	public String[] govAttributes = {"AGL", "END", "INT", "PER", "SPD", "STR", "WIL"};
	private LayoutInflater inflater;

	public SkillListAdapter(Context context, List<Skill> skills, String[] skillNames) {
		super(context, 0, skills);
		this.context = context;
		this.skills = skills;
		this.skillNames = skillNames;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null){
			convertView = inflater.inflate(R.layout.skill_item, parent, false);
		}
		Skill skill = skills.get(position);
		TextView skillName = (TextView) convertView.findViewById(R.id.skill_name);
		skillName.setText(skillNames[(int)skill.getId()]);
		skillName.setTag(skill.getId());
		

		TextView govAttr = (TextView) convertView.findViewById(R.id.attribute_name_short);
		govAttr.setText(govAttributes[skill.governingAttribute]);

		TextView prevLevel = (TextView) convertView.findViewById(R.id.skill_previous_level);
		prevLevel.setText(String.valueOf(skill.prevLevel));

		TextView actLevel = (TextView) convertView.findViewById(R.id.skill_current_level);
		actLevel.setText(String.valueOf(skill.actLevel));

		TextView diff = (TextView) convertView.findViewById(R.id.skill_diff);
		int value = skill.overLevel;
		if(value == 0){
			diff.setText("");
		}
		else{
			diff.setText(String.valueOf(value));
		}
		
		int normalColor = Color.parseColor("#d8c7b3");
		int majorColor = Color.parseColor("#ffffff");
		
		if(skill.isMajor){
			skillName.setTextColor(majorColor);
			skillName.setTypeface(null, Typeface.BOLD);
			govAttr.setTextColor(majorColor);
			prevLevel.setTextColor(majorColor);
			actLevel.setTextColor(majorColor);
			diff.setTextColor(majorColor);
		}
		else{
			skillName.setTextColor(normalColor);
			skillName.setTypeface(null, Typeface.NORMAL);
			govAttr.setTextColor(normalColor);
			prevLevel.setTextColor(normalColor);
			actLevel.setTextColor(normalColor);
			diff.setTextColor(normalColor);
		}
		return convertView;
	}

	@Override
	public long getItemId(int position){
		return skills.get(position).getId();
	}

	public void increaseSkill(long id) {
		skills.get((int) id).actLevel ++;
	}
}
