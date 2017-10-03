// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.onboarding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.reelsonar.ibobber.BobberApp;
import com.reelsonar.ibobber.LanguageActivity;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.form.FormGroup;
import com.reelsonar.ibobber.form.LabelField;
import com.reelsonar.ibobber.form.TextField;
import com.reelsonar.ibobber.model.FavoriteFish;
import com.reelsonar.ibobber.onboarding.fish.SelectFishActivity;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.util.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class RegisterActivity extends Activity {

    private static final int REQUEST_FISH = 0;

    public final static String UPDATE_USER_KEY = "updateUser";
    public final static int UPDATING_USER_IS_TRUE = 1;

    private ArrayList<FavoriteFish> _fish;
    private String _nickName = "";
    private String _email = "";
    private Boolean _allowSubmit = false;
    private Boolean _updatingUser = false;

    private Button _registerButton;
    private RegisterAdapter _registerAdapter;
    private ListView _registerForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Bundle bundle = getIntent().getExtras();

        _registerButton = (Button) findViewById(R.id.btnRegister);
        _registerForm = (ListView) findViewById(R.id.registerForm);

        findViewById(R.id.deviceDrawer).setVisibility(View.INVISIBLE);
        findViewById(R.id.homeDrawer).setVisibility(View.INVISIBLE);

        if (bundle != null) {
            if (bundle.getInt(UPDATE_USER_KEY) == UPDATING_USER_IS_TRUE) {
                _updatingUser = true;

                Tracker tracker = ((BobberApp) getApplication()).getGaTracker();
                tracker.setScreenName("Settings Personal");
                tracker.send(new HitBuilders.AppViewBuilder().build());

                layoutForUpdate();
            }
        }

        _registerAdapter = new RegisterAdapter();
        _registerForm.setAdapter(_registerAdapter);
        _registerForm.setOnItemClickListener(_registerAdapter);

        setSubmitButtonEnableState();

        EventBus.getDefault().register(this);
    }

    private void layoutForUpdate() {
        _registerButton.setText(R.string.button_update_user);

        findViewById(R.id.onboardingSplashImage).setVisibility(View.INVISIBLE);
        findViewById(R.id.registerText).setVisibility(View.INVISIBLE);

        findViewById(R.id.deviceDrawer).setVisibility(View.VISIBLE);
        findViewById(R.id.homeDrawer).setVisibility(View.VISIBLE);

        findViewById(R.id.settingsImage).setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams registerFormParams = (RelativeLayout.LayoutParams) _registerForm.getLayoutParams();
        registerFormParams.removeRule(RelativeLayout.ABOVE);
        registerFormParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        int dpValue = 120;
        float d = this.getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d);
        registerFormParams.setMargins(0, margin, 0, 0);

        RelativeLayout.LayoutParams registerButtonParams = (RelativeLayout.LayoutParams) _registerButton.getLayoutParams();
        registerButtonParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        registerButtonParams.addRule(RelativeLayout.BELOW, R.id.registerForm);

        _nickName = UserService.getInstance(this).getNickname();
        _email = UserService.getInstance(this).getEmail();

        List<FavoriteFish> fishList = UserService.getInstance(this).getFish();
        if (fishList != null) _fish = new ArrayList<>(UserService.getInstance(this).getFish());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(final UserService.LocalizationChangedNotification notification) {
        _registerAdapter.notifyDataSetChanged();
        _registerButton.setText(R.string.button_register);
        ((TextView) findViewById(R.id.registerText)).setText(R.string.onboarding_get_started);
    }

    public class RegisterAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        private List<FormGroup> _formGroups;

        public RegisterAdapter() {
            createFormGroups();
        }

        @Override
        public void notifyDataSetChanged() {
            createFormGroups();
            super.notifyDataSetChanged();
        }

        private void createFormGroups() {

            TextField nickNameForm = new TextField(RegisterActivity.this, R.string.settings_nickname, _nickName, R.string.settings_required, true, false, true) {
                @Override
                public void onValueChange(final String value) {
                    _nickName = value;
                    setSubmitButtonEnableState();
                }
            };

            TextField emailForm = new TextField(RegisterActivity.this, R.string.settings_email, _email, R.string.settings_required, true, false, true) {
                @Override
                public void onValueChange(final String value) {
                    _email = value;
                    setSubmitButtonEnableState();
                }
            };

            LabelField fishForm = new LabelField(RegisterActivity.this, R.string.settings_favorite_fish, null, true) {
                @Override
                public void onGroupClick(final View view, final boolean isExpanded) {

                    Tracker tracker = ((BobberApp) getApplication()).getGaTracker();

                    if (_updatingUser) {
                        tracker.setScreenName("Settings Favorite Fish");
                    } else {
                        tracker.setScreenName("On-boarding Favorite Fish");
                    }
                    tracker.send(new HitBuilders.AppViewBuilder().build());

                    Intent selectFish = new Intent(RegisterActivity.this, SelectFishActivity.class);
                    selectFish.putParcelableArrayListExtra("fish", _fish);
                    startActivityForResult(selectFish, REQUEST_FISH);
                }
            };

            LabelField languageForm = new LabelField(RegisterActivity.this, R.string.settings_language, null, true) {
                @Override
                public void onGroupClick(final View view, final boolean isExpanded) {

                    Tracker tracker = ((BobberApp) getApplication()).getGaTracker();
                    tracker.setScreenName("On-boarding Language");
                    tracker.send(new HitBuilders.AppViewBuilder().build());

                    Intent intent = new Intent(RegisterActivity.this, LanguageActivity.class);
                    startActivity(intent);
                }
            };

            if (!_updatingUser) {
                _formGroups = Arrays.asList(nickNameForm, emailForm, fishForm, languageForm, nickNameForm, nickNameForm, nickNameForm, nickNameForm);
            } else {
                _formGroups = Arrays.asList(nickNameForm, emailForm, fishForm);
            }

            Typeface tf = Style.formTypeface(RegisterActivity.this);
            for (FormGroup formGroup : _formGroups) {
                if (formGroup != null) {
                    formGroup.setTypeface(tf);
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
            return 2;
        }

        @Override
        public int getItemViewType(final int position) {
            return _formGroups.get(position) == null ? 0 : 1;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            FormGroup group = _formGroups.get(position);
            return group.getGroupView(false, convertView, parent);
        }

        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            FormGroup group = _formGroups.get(position);
            if (group != null) {
                group.onGroupClick(view, false);
            }
        }
    }

    private void setSubmitButtonEnableState() {
        if (isValidEmail(_email) && !_nickName.isEmpty()) {
            _allowSubmit = true;
            _registerButton.setBackgroundResource(R.drawable.button_orange);
            _registerButton.setTextColor(Color.WHITE);
        } else {
            _allowSubmit = false;
            _registerButton.setBackgroundResource(R.drawable.button_gray);
            _registerButton.setTextColor(Color.LTGRAY);
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        if (email.matches(".+\\@.+\\..+")) return true;
        return false;
    }


    public void registerClick(View view) {
        if (_allowSubmit) {

            UserService.getInstance(this).persistUserInfo(_nickName, _email, _fish);

            if (!_updatingUser) {
                UserService.getInstance(this).recordVersionFirstRegisteredWith();
                Intent i = new Intent(this, AppDemoActivity.class);
                i.putExtra(AppDemoActivity.INITIAL_DEMO_AFTER_REGISTER_KEY, AppDemoActivity.INITIAL_DEMO_IS_TRUE);
                startActivity(i);
            }

            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FISH && resultCode == RESULT_OK) {
            _fish = data.getParcelableArrayListExtra("fish");
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
