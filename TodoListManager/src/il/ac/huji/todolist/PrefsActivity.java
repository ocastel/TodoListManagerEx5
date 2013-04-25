package il.ac.huji.todolist;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

public class PrefsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   addPreferencesFromResource(R.layout.prefs);
	   // Hide first-run preference (it's containing category)
       PreferenceCategory category = (PreferenceCategory) findPreference("Usage information");
       Preference firstRunPreference = findPreference("first_run");
       category.removePreference(firstRunPreference);

	   
	}
	 
}