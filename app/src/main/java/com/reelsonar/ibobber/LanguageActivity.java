// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.settings.Language;


public class LanguageActivity extends Activity {

    private LanguageAdapter _languageAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        String langCode = UserService.getInstance(this).getLanguageCode();
        Language language = Language.forCode(langCode);

        _languageAdapter = new LanguageAdapter(this, language);
        ListView listView = (ListView)findViewById(R.id.lstLanguages);
        listView.setAdapter(_languageAdapter);
    }

    @Override
    public void onBackPressed() {
        Language language = _languageAdapter.getSelectedLanguage();
        if (language != null) {
            UserService.getInstance(this).setLanguageCode(language.getCode());
        }

        super.onBackPressed();
    }
}
