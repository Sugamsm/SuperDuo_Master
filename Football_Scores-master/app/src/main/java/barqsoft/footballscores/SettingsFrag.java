package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import barqsoft.footballscores.service.myFetchService;

/**
 * Created by HP on 08-03-2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFrag extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.api_prefs);

        Preference preference = findPreference(getString(R.string.pref_api_key));
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));

        if (preference.getKey().equals(getString(R.string.pref_api_key))) {
            Object value = PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), "");

            preference.setSummary(value.toString());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String api = newValue.toString();

        preference.setSummary(api);

        if (api != null && !api.equals("")) {
            if (Utilies.netConnect(getActivity())) {
                Intent i = new Intent(getActivity(), myFetchService.class);
                getActivity().startService(i);
            } else {
                Toast.makeText(getActivity(), getString(R.string.net_error), Toast.LENGTH_LONG).show();
            }
        }

        return true;
    }
}
