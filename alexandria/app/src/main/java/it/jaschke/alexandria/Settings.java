package it.jaschke.alexandria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class Settings extends Fragment {
    private FrameLayout frame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings_fragment, container, false);
        frame = (FrameLayout) v.findViewById(R.id.set_root);
        getActivity().getFragmentManager().beginTransaction().replace(frame.getId(), new SettingsActivity()).commit();
        getActivity().setTitle(R.string.action_settings);
        Tools.hideKey(getActivity());
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.action_settings);
    }
}
