// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.settings;

import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.form.IdName;

public enum Language implements IdName {

    ENGLISH("en", R.string.lang_en),
    ENGLISH_UK("en_GB", R.string.lang_en_uk),
    BULGARIAN("bg", R.string.lang_bg),
    FRENCH("fr", R.string.lang_fr),
    SPANISH("es", R.string.lang_es),
    GERMAN("de", R.string.lang_de),
    PORTUGUESE("pt", R.string.lang_pt),
    RUSSIAN("ru", R.string.lang_ru),
    CZECH("cs", R.string.lang_cs),
    SLOVAK("sk", R.string.lang_sk),
    POLISH("pl", R.string.lang_pl),
    JAPANESE("ja", R.string.lang_ja),
    KOREAN("ko", R.string.lang_ko),
    CHINESE("zh", R.string.lang_zh_hans),
    VIETNAMESE("vi", R.string.lang_vi),
    HEBREW("iw", R.string.lang_he),
    ARABIC("ar", R.string.lang_ar);


    public static Language forCode(final String code) {
        for (Language language : values()) {
            if (language.getCode().equals(code)) {
                return language;
            }
        }
        return null;
    }

    private String _code;
    private int _name;

    Language(final String code, final int name) {
        _code = code;
        _name = name;
    }

    @Override
    public int getId() {
        return ordinal() + 1;
    }

    @Override
    public int getName() {
        return _name;
    }

    public String getCode() {
        return _code;
    }
}
