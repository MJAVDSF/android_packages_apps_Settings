/*
 * Copyright (C) 2013 The CyanogenMod project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.cyanogenmod.ButtonBacklightBrightness;
import com.android.settings.cyanogenmod.KeyboardBacklightBrightness;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class ButtonSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String KEY_ENABLE_CUSTOM_BINDING = "hardware_keys_enable_custom";
    private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final String KEY_MENU_PRESS = "hardware_keys_menu_press";
    private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String KEY_ASSIST_PRESS = "hardware_keys_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
    private static final String KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";
    private static final String KEY_BUTTON_WAKE = "pref_wakeon_button"; 
    private static final String KEY_VOLUME_WAKE = "pref_volume_wake";
    private static final String KEY_SHOW_OVERFLOW = "hardware_keys_show_overflow";
    private static final String KEY_VOLBTN_MUSIC_CTRL = "volbtn_music_controls";
    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String KEY_KEYBOARD_BACKLIGHT = "keyboard_backlight";

    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_BACKLIGHT = "key_backlight";

    // Available custom actions to perform on a key press.
    // Must match values for KEY_HOME_LONG_PRESS_ACTION in:
    // frameworks/base/core/java/android/provider/Settings.java
    private static final int ACTION_NOTHING = 0;
    private static final int ACTION_MENU = 1;
    private static final int ACTION_APP_SWITCH = 2;
    private static final int ACTION_SEARCH = 3;
    private static final int ACTION_VOICE_SEARCH = 4;
    private static final int ACTION_IN_APP_SEARCH = 5;

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;
    private static final int KEY_MASK_ASSIST = 0x08;
    private static final int KEY_MASK_APP_SWITCH = 0x10;

    private CheckBoxPreference mEnableCustomBindings;
    private ListPreference mHomeLongPressAction;
    private ListPreference mButtonWake;  
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;
    private CheckBoxPreference mShowActionOverflow;
    private CheckBoxPreference mVolumeWake;
    private CheckBoxPreference mVolBtnMusicCtrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.button_settings);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        boolean hasAnyBindableKey = false;
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
        final PreferenceCategory volumeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_VOLUME);
        final PreferenceCategory backlightCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACKLIGHT);

 	mButtonWake = (ListPreference) findPreference(KEY_BUTTON_WAKE);
        if (mButtonWake != null) {  

            if (!res.getBoolean(R.bool.config_show_homeWake)) {
                //no home button, don't allow user to disable power button either
                homeCategory.removePreference(mButtonWake);    
            } else {
                int buttonWakeValue = Settings.System.getInt(resolver,
                        Settings.System.BUTTON_WAKE_SCREEN, 2);
                mButtonWake.setValue(String.valueOf(buttonWakeValue));
                mButtonWake.setSummary(getResources().getString(R.string.pref_wakeon_button_summary, mButtonWake.getEntry()));
                mButtonWake.setOnPreferenceChangeListener(this);  
            }

            int longPressAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION,
                    hasAppSwitchKey ? ACTION_NOTHING : ACTION_APP_SWITCH);
            mHomeLongPressAction = initActionList(KEY_HOME_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(homeCategory);
        }

        if (hasMenuKey) {
            int pressAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_MENU_ACTION, ACTION_MENU);
            mMenuPressAction = initActionList(KEY_MENU_PRESS, pressAction);

            int longPressAction = Settings.System.getInt(resolver,
                        Settings.System.KEY_MENU_LONG_PRESS_ACTION,
                        hasAssistKey ? ACTION_NOTHING : ACTION_SEARCH);
            mMenuLongPressAction = initActionList(KEY_MENU_LONG_PRESS, longPressAction);

            mShowActionOverflow =
                    (CheckBoxPreference) prefScreen.findPreference(KEY_SHOW_OVERFLOW);

            mShowActionOverflow.setChecked(Settings.System.getInt(resolver,
                    Settings.System.UI_FORCE_OVERFLOW_BUTTON, 0) == 1);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(menuCategory);
        }

        if (hasAssistKey) {
            int pressAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_ASSIST_ACTION, ACTION_SEARCH);
            mAssistPressAction = initActionList(KEY_ASSIST_PRESS, pressAction);

            int longPressAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION, ACTION_VOICE_SEARCH);
            mAssistLongPressAction = initActionList(KEY_ASSIST_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(assistCategory);
        }

        if (hasAppSwitchKey) {
            int pressAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_APP_SWITCH_ACTION, ACTION_APP_SWITCH);
            mAppSwitchPressAction = initActionList(KEY_APP_SWITCH_PRESS, pressAction);

            int longPressAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, ACTION_NOTHING);
            mAppSwitchLongPressAction = initActionList(KEY_APP_SWITCH_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(appSwitchCategory);
        }

        mEnableCustomBindings =
                (CheckBoxPreference) prefScreen.findPreference(KEY_ENABLE_CUSTOM_BINDING);

        if (hasAnyBindableKey) {
            mEnableCustomBindings.setChecked(Settings.System.getInt(resolver,
                    Settings.System.HARDWARE_KEY_REBINDING, 0) == 1);
        } else {
            prefScreen.removePreference(mEnableCustomBindings);
        }

        if (Utils.hasVolumeRocker(getActivity())) {
            mVolumeWake = (CheckBoxPreference) findPreference(KEY_VOLUME_WAKE);
            mVolBtnMusicCtrl = (CheckBoxPreference) findPreference(KEY_VOLBTN_MUSIC_CTRL);

            mVolBtnMusicCtrl.setChecked(Settings.System.getInt(resolver,
                    Settings.System.VOLBTN_MUSIC_CONTROLS, 1) != 0);

            if (!res.getBoolean(R.bool.config_show_volumeRockerWake)) {
                volumeCategory.removePreference(mVolumeWake);
            } else {
                mVolumeWake.setChecked(Settings.System.getInt(resolver,
                        Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);
            }
        } else {
            prefScreen.removePreference(volumeCategory);
        }

        if (ButtonBacklightBrightness.isSupported() || KeyboardBacklightBrightness.isSupported()) {
            if (!ButtonBacklightBrightness.isSupported()) {
                removePreference(KEY_BUTTON_BACKLIGHT);
            }

            if (!KeyboardBacklightBrightness.isSupported()) {
                removePreference(KEY_KEYBOARD_BACKLIGHT);
            }
        } else {
            prefScreen.removePreference(backlightCategory);
        }
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);

        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void handleCheckboxClick(CheckBoxPreference pref, String setting) {
        Settings.System.putInt(getContentResolver(), setting, pref.isChecked() ? 1 : 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHomeLongPressAction) {
            handleActionListChange(mHomeLongPressAction, newValue,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mMenuPressAction) {
            handleActionListChange(mMenuPressAction, newValue,
                    Settings.System.KEY_MENU_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleActionListChange(mMenuLongPressAction, newValue,
                    Settings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistPressAction) {
            handleActionListChange(mAssistPressAction, newValue,
                    Settings.System.KEY_ASSIST_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {
            handleActionListChange(mAssistLongPressAction, newValue,
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchPressAction) {
            handleActionListChange(mAppSwitchPressAction, newValue,
                    Settings.System.KEY_APP_SWITCH_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleActionListChange(mAppSwitchLongPressAction, newValue,
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
	} else if (preference == mButtonWake) {
            handleActionListChange(mButtonWake, newValue,
                    Settings.System.BUTTON_WAKE_SCREEN);	
	    //int buttonWakeValue = Integer.valueOf((String) objValue);
            //int index = mButtonWake.findIndexOfValue((String) objValue);
            //Settings.System.putInt(getActivity().getContentResolver(),
            //        Settings.System.BUTTON_WAKE_SCREEN, buttonWakeValue);
            //mButtonWake.setSummary(getResources().getString(R.string.pref_wakeon_button_summary, mButtonWake.getEntries()[index]));
            return true;  
        }

        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mVolumeWake) {
            handleCheckboxClick(mVolumeWake, Settings.System.VOLUME_WAKE_SCREEN);
            return true;
        } else if (preference == mVolBtnMusicCtrl) {
            handleCheckboxClick(mVolBtnMusicCtrl, Settings.System.VOLBTN_MUSIC_CONTROLS);
            return true;
        } else if (preference == mEnableCustomBindings) {
            handleCheckboxClick(mEnableCustomBindings, Settings.System.HARDWARE_KEY_REBINDING);
            return true;
        } else if (preference == mShowActionOverflow) {
            handleCheckboxClick(mShowActionOverflow, Settings.System.UI_FORCE_OVERFLOW_BUTTON);

            int toastResId = mShowActionOverflow.isChecked()
                    ? R.string.hardware_keys_show_overflow_toast_enable
                    : R.string.hardware_keys_show_overflow_toast_disable;

            Toast.makeText(getActivity(), toastResId, Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
