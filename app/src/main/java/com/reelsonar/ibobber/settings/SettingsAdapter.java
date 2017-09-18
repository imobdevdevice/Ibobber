// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.reelsonar.ibobber.BLEScanActivity;
import com.reelsonar.ibobber.HTMLViewActivity;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.bluetooth.BTConstants;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.form.ButtonField;
import com.reelsonar.ibobber.form.FormGroup;
import com.reelsonar.ibobber.form.IdName;
import com.reelsonar.ibobber.form.LabelField;
import com.reelsonar.ibobber.form.NumberField;
import com.reelsonar.ibobber.form.SpinnerField;
import com.reelsonar.ibobber.form.SwitchField;
import com.reelsonar.ibobber.onboarding.AppDemoActivity;
import com.reelsonar.ibobber.onboarding.RegisterActivity;
import com.reelsonar.ibobber.service.DemoSonarService;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.util.AppUtils;
import com.reelsonar.ibobber.util.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.reelsonar.ibobber.util.RestConstants.NETFISH_MODE;

public class SettingsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    public static final int SPEED_MIN = 1;
    public static final int SPEED_MAX = 10;

    public static class DemoModeEnabled {
    }

    ;

    public static class DemoModeDisabled {
    }

    ;

    private static enum Measurement implements IdName {
        METRIC {
            @Override
            public int getName() {
                return R.string.settings_metric;
            }
        },

        IMPERIAL {
            @Override
            public int getName() {
                return R.string.settings_imperial;
            }
        };

        @Override
        public int getId() {
            return ordinal();
        }
    }

    private Context _context;
    private List<FormGroup> _formGroups;
    private List<Integer> _formGroupViewIds;

    public SettingsAdapter(final Context context) {
        _context = context;
        updateFormGroups();
    }

    @Override
    public void notifyDataSetChanged() {
        updateFormGroups();
        super.notifyDataSetChanged();
    }

    View.OnClickListener firmwareUpdateClick = new View.OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(_context, FirmwareUpdateActivity.class);
            _context.startActivity(intent);
        }
    };


    int testFirmwareToggleCount = 0;

    private FormGroup getFirmwareField() {

        if ((BTService.getSingleInstance().getConnectedToDevice() && (BTService.getSingleInstance().getFirmwareUpdateProfile().isFirmwareUpdateAvailable() ||
                BTConstants.ALLOW_A_TO_B_FW_SWAP_ON_SAME_FW_VERSION))) {

            return new ButtonField(_context, _context.getResources().getString(R.string.settings_firmware_version)
                    + " (" + BTService.getSingleInstance().getFirmwareRev() + ")",
                    _context.getResources().getString(R.string.settings_firmware_update) +
                            " " + BTService.getSingleInstance().getFirmwareUpdateProfile().getAvailableFirmwareRev(), true, firmwareUpdateClick);
        } else {

            return new ButtonField(_context, (BTService.getSingleInstance().getConnectedToDevice()) ?
                    _context.getResources().getString(R.string.settings_firmware_version) : _context.getResources().getString(R.string.settings_firmware_bobber_required),
                    (BTService.getSingleInstance().getConnectedToDevice()) ?
                            BTService.getSingleInstance().getFirmwareRev() : null, false, null) {
                @Override
                public void onGroupClick(final View view, final boolean isExpanded) {
                    testFirmwareToggleCount++;
                    if (testFirmwareToggleCount == 10) {
                        BTService.getSingleInstance().getFirmwareUpdateProfile().resetLastFirmwareQueryTime();
                        BTService.getSingleInstance().getFirmwareUpdateProfile().toggleUseTestFirmwareLocation();
                        BTService.getSingleInstance().getFirmwareUpdateProfile().requestLatestFirmwareVersion();
                        testFirmwareToggleCount = 0;
                    }
                }
            };
        }

    }

    private void updateFormGroups() {
        final UserService userService = UserService.getInstance(_context);
        String versionName = userService.getVersionName();

        _formGroups = Arrays.asList(
                new LabelField(_context, R.string.settings_app_version, versionName, false),
                getFirmwareField(), null,
                new LabelField(_context, R.string.settings_personal, null, true) {
                    @Override
                    public void onGroupClick(final View view, final boolean isExpanded) {
                        Intent intent = new Intent(_context, RegisterActivity.class);
                        intent.putExtra(RegisterActivity.UPDATE_USER_KEY, RegisterActivity.UPDATING_USER_IS_TRUE);
                        _context.startActivity(intent);

                    }
                },
                new LabelField(_context, R.string.settings_bluetooth_sync, null, true) {
                    @Override
                    public void onGroupClick(final View view, final boolean isExpanded) {
                        Intent intent = new Intent(_context, BLEScanActivity.class);
                        _context.startActivity(intent);
                    }
                },
                new SpinnerField<Language>(_context, R.string.settings_language, Language.class, Language.forCode(userService.getLanguageCode())) {
                    @Override
                    public void onConstantChanged(final Language constant) {
                        if (constant != null) {
                            userService.setLanguageCode(constant.getCode());
                            notifyDataSetChanged();
                        }
                    }
                },
                new SpinnerField<Measurement>(_context, R.string.settings_measurement, Measurement.class, userService.isMetric() ? Measurement.METRIC : Measurement.IMPERIAL) {
                    @Override
                    public void onConstantChanged(final Measurement measurement) {
                        userService.setMetric(measurement == Measurement.METRIC);
                    }
                },
                new NumberField(_context, R.string.settings_speed, "fps", userService.getSpeedFeetPerSecond(), SPEED_MIN, SPEED_MAX) {
                    @Override
                    public void onValueChanged(final int value) {
                        userService.setSpeedFeetPerSecond(value);
                    }
                },
                new SwitchField(_context, R.string.settings_antiglare, userService.getAntiGlare()) {
                    @Override
                    public void onValueChanged(final boolean checked) {
                        userService.setAntiGlare(checked);
                    }
                },

                null,

                new SwitchField(_context, R.string.settings_sonar_demo, DemoSonarService.getSingleInstance(_context).getDemoRunning()) {
                    @Override
                    public void onValueChanged(final boolean checked) {
                        if (checked) {
                            DemoSonarService.getSingleInstance(_context).startSendingData();
                            EventBus.getDefault().post(new DemoModeEnabled());
                        } else {
                            DemoSonarService.getSingleInstance(_context).stopSendingData();
                            EventBus.getDefault().post(new DemoModeDisabled());
                        }
                    }
                },
                new SwitchField(_context, R.string.settings_slow_mode, BTService.getSingleInstance().getSlowModeStatus() == 1) {
                    @Override
                    public void onValueChanged(final boolean checked) {
                        if (checked) {
                            BTService.getSingleInstance().setSlowMode(1);
                        } else {
                            BTService.getSingleInstance().setSlowMode(0);
                        }
                    }
                }, new SwitchField(_context, R.string.netfish_mode, AppUtils.getIntegerSharedpreference(_context, NETFISH_MODE) == 1) {
                    @Override
                    public void onValueChanged(final boolean checked) {
                        if (checked) {
                            AppUtils.storeSharedPreference(_context, NETFISH_MODE, 1);
                        } else {
                            AppUtils.storeSharedPreference(_context, NETFISH_MODE, 0);
                        }
                    }
                },
                new LabelField(_context, R.string.settings_app_tour, null, true) {
                    @Override
                    public void onGroupClick(final View view, final boolean isExpanded) {
                        Intent intent = new Intent(_context, AppDemoActivity.class);
                        _context.startActivity(intent);
                    }
                },
                new LabelField(_context, R.string.settings_buy, null, true) {
                    @Override
                    public void onGroupClick(final View view, final boolean isExpanded) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(_context.getResources().getString(R.string.settings_buy_url)));
                        _context.startActivity(intent);
                    }
                },
                null,
                new LabelField(_context, R.string.settings_terms, null, true) {
                    @Override
                    public void onGroupClick(final View view, final boolean isExpanded) {
                        Intent intent = new Intent(_context, HTMLViewActivity.class);
                        intent.putExtra("term", true);
                        _context.startActivity(intent);
                    }
                },
                new LabelField(_context, R.string.settings_privacy, null, true) {
                    @Override
                    public void onGroupClick(final View view, final boolean isExpanded) {
                        Intent intent = new Intent(_context, HTMLViewActivity.class);
                        intent.putExtra("term", false);
                        _context.startActivity(intent);
                    }
                },
                new LabelField(_context, R.string.logout, null, true) {
                    @Override
                    public void onGroupClick(final View view, final boolean isExpanded) {
                        AppUtils.logout(_context);
                    }
                },
                new LabelField(_context, R.string.fcc, null, false),
                new LabelField(_context, R.string.ic, null, false)
        );

        if (BTService.getSingleInstance().getConnectedToDevice()) {
            String deviceAddress = BTService.getSingleInstance().getDeviceAddress();
            if (deviceAddress != null) {
                if (deviceAddress.length() > 4) {
                    deviceAddress = deviceAddress.substring(deviceAddress.length() - 4, deviceAddress.length());
                    _formGroups = new ArrayList<>(_formGroups);
                    _formGroups.add(new LabelField(_context, "ID: " + deviceAddress, null, false));
                }
            }
        }


        _formGroupViewIds = new ArrayList<>(4);
        Typeface tf = Style.formTypeface(_context);
        for (FormGroup formGroup : _formGroups) {
            if (formGroup != null) {
                formGroup.setTypeface(tf);

                if (!_formGroupViewIds.contains(formGroup.getViewWrapperId())) {
                    _formGroupViewIds.add(formGroup.getViewWrapperId());
                }
            }
        }
    }

    @Override
    public int getCount() {
        return _formGroups.size();
    }

    @Override
    public Object getItem(final int position) {
        return null;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return _formGroupViewIds.size() + 1;
    }

    @Override
    public int getItemViewType(final int position) {
        FormGroup formGroup = _formGroups.get(position);
        return formGroup == null ? 0 : _formGroupViewIds.indexOf(formGroup.getViewWrapperId()) + 1;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        FormGroup group = _formGroups.get(position);
        if (group != null) {
            return group.getGroupView(false, convertView, parent);
        } else {
            View spacer = convertView;
            if (spacer == null) {
                spacer = new View(_context);
                float scale = _context.getResources().getDisplayMetrics().density;
                spacer.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, (int) (10 * scale + 0.5f)));
            }
            return spacer;
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        FormGroup group = _formGroups.get(position);
        if (group != null) {
            group.onGroupClick(view, false);
        }

    }
}
