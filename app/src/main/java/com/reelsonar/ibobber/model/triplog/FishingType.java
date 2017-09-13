// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model.triplog;

import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.form.IdName;

public enum FishingType implements IdName {

    BAIT {
        @Override
        public int getName() {
            return R.string.trip_log_bait;
        }
    },

    SPIN {
        @Override
        public int getName() {
            return R.string.trip_log_spin;
        }
    },

    TROLL {
        @Override
        public int getName() {
            return R.string.trip_log_troll;
        }
    },

    FLY {
        @Override
        public int getName() {
            return R.string.trip_log_fly;
        }
    };

    @Override
    public int getId() {
        return ordinal() + 1;
    }
}
