package cn.elva.wcfp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import java.io.File;

import cn.elva.wcfp.R;
import cn.elva.wcfp.VersionInfo;
import cn.elva.wcfp.WCFPXSharedPreferencesUtil;
import cn.elva.wcfp.utils.AESHelper;

import static cn.elva.wcfp.VersionInfo.PKG_NAME;

/**
 * The app setting UI
 */
public class PaySettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Display the fragment as the main content
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(android.R.id.content, new PaySettingFragment());
            fragmentTransaction.commit();
        }
    }

    public static class PaySettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private SharedPreferences sharedPreferences;
        private Preference prefEnable;
        private Preference prefPwd;
        private SharedPreferences.Editor spEditor;
        private Context mContext;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mContext = getActivity();
            //TODO Fix preference issues in Nougat
            //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            //getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);

            // Load PaySettingFragment from xml resource
            addPreferencesFromResource(R.xml.fragment_pay_setting);
            sharedPreferences = getPreferenceScreen().getSharedPreferences();
            prefEnable = findPreference(VersionInfo.PREF_KEY_ENABLE);
            prefPwd = findPreference(VersionInfo.PREF_KEY_PWD);
            try {
                int versionCode = mContext.getPackageManager().getPackageInfo(PKG_NAME, 0).versionCode;
                if (!VersionInfo.checkVersion(versionCode)) {
                    onError(getString(R.string.hint_not_support, versionCode));
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                onError(getString(R.string.hint_wc_not_install));
                Toast.makeText(mContext, getString(R.string.hint_wc_not_install), Toast.LENGTH_LONG).show();
                return;
            }

            // Add preference change listener
            prefEnable.setOnPreferenceChangeListener(this);
            prefPwd.setOnPreferenceChangeListener(this);
            prefPwd.setOnPreferenceClickListener(this);
        }

        private void onError(String msg) {
            Spannable summary = new SpannableString(msg);
            summary.setSpan(new ForegroundColorSpan(mContext.getColor(R.color.colorError)), 0, summary.length(), 0);
            prefEnable.setSummary(summary);
            prefEnable.setEnabled(false);
            prefEnable.setShouldDisableView(true);
        }

        @Override
        public void onPause() {
            super.onPause();
            final File dataDir = new File(getActivity().getApplicationInfo().dataDir);
            final File prefsDir = new File(dataDir, "shared_prefs");
            final File prefsFile = new File(prefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
            if (prefsFile.exists()) {
                Toast.makeText(mContext, "xxx", Toast.LENGTH_LONG).show();
                dataDir.setReadable(true, false);
                dataDir.setExecutable(true, false);

                prefsDir.setReadable(true, false);
                prefsDir.setExecutable(true, false);

                prefsFile.setReadable(true, false);
                prefsFile.setExecutable(true, false);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            WCFPXSharedPreferencesUtil.notifyReload();
            switch (preference.getKey()) {
                case VersionInfo.PREF_KEY_ENABLE:
                    break;
                case VersionInfo.PREF_KEY_PWD:
                    if (!((String) newValue).matches("\\d{6}")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.title_error))
                                .setPositiveButton(getString(R.string.hint_confirm), null)
                                .setMessage(getString(R.string.error_six_digit))
                                .show();
                        return false;
                    }
                    spEditor = sharedPreferences.edit();
                    final String android_id = WCFPXSharedPreferencesUtil.getID(getContext());
                    final String key = AESHelper.encrypt(android_id, VersionInfo.DU_EN_KEY);
                    spEditor.putString(VersionInfo.PREF_KEY_PWD, AESHelper.encrypt((String) newValue, key));
                    spEditor.apply();
                    return false;
                default:
                    break;
            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals(VersionInfo.PREF_KEY_PWD)) {
                ((EditTextPreference) preference).setText("");
            }
            return true;
        }
    }
}


