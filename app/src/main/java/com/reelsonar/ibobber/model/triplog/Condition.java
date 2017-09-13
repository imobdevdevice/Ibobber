// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model.triplog;

import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.form.IdName;

public enum Condition implements IdName {

    CALM {
        @Override
        public int getName() {
            return R.string.trip_log_calm;
        }
    },

    CHOP {
        @Override
        public int getName() {
            return R.string.trip_log_chop;
        }
    },

    ROUGH {
        @Override
        public int getName() {
            return R.string.trip_log_rough;
        }
    };

    @Override
    public int getId() {
        return ordinal() + 1;
    }

}
