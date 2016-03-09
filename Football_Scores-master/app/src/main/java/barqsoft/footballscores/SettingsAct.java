package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.FrameLayout;

/**
 * Created by HP on 08-03-2016.
 */
public class SettingsAct extends ActionBarActivity {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        FrameLayout fLayout = (FrameLayout) findViewById(R.id.set_root);
        setTitle(getString(R.string.action_settings));
        getFragmentManager().beginTransaction().replace(fLayout.getId(), new SettingsFrag()).commit();
    }
}
