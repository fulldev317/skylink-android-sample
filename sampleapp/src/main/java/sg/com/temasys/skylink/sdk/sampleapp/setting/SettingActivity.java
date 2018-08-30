package sg.com.temasys.skylink.sdk.sampleapp.setting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import sg.com.temasys.skylink.sdk.sampleapp.R;

public class SettingActivity extends AppCompatActivity {

    private final String SETTING_FRAGMENT_TAG = "SETTING_FRAGMENT_TAG";

    private SettingPresenter mSettingPresenter;
    private SettingFragment mSettingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //check previous state in case of screen rotation
        if (savedInstanceState == null) {
            mSettingFragment = SettingFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.contentFrameSetting, mSettingFragment, SETTING_FRAGMENT_TAG)
                    .commit();
        } else {
            mSettingFragment = (SettingFragment) getSupportFragmentManager()
                    .findFragmentByTag(SETTING_FRAGMENT_TAG);
        }

        //link between view and presenter
        mSettingPresenter = new SettingPresenter(mSettingFragment, this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, SETTING_FRAGMENT_TAG, mSettingFragment);
    }
}