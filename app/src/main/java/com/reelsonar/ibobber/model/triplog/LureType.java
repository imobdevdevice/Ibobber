// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model.triplog;

import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.form.IdName;

public enum LureType implements IdName {

    OTHER{
        @Override
        public int getName() {
            return R.string.trip_log_lure_other;
        }
    },

    ALABAMA_RIG {
        @Override
        public int getName() {
            return R.string.trip_log_lure_alabama_rig;
        }
    },

    CRANK_BAIT {
        @Override
        public int getName() {
            return R.string.trip_log_lure_crank_bait;
        }
    },

    FISH_DECOY {
        @Override
        public int getName() {
            return R.string.trip_log_lure_fish_decoy;
        }
    },

    FLY {
        @Override
        public int getName() {
            return R.string.trip_log_lure_fly;
        }
    },

    JIG {
        @Override
        public int getName() {
            return R.string.trip_log_lure_jig;
        }
    },

    LIVE_BAIT {
        @Override
        public int getName() {
            return R.string.trip_log_lure_live_bait;
        }
    },

    MINNOW {
        @Override
        public int getName() {
            return R.string.trip_log_lure_minnow;
        }
    },

    PLUG {
        @Override
        public int getName() {
            return R.string.trip_log_lure_plug;
        }
    },

    SENKO {
        @Override
        public int getName() {
            return R.string.trip_log_lure_senko;
        }
    },

    SHAD {
        @Override
        public int getName() {
            return R.string.trip_log_lure_shad;
        }
    },

    SHRIMP {

        @Override
        public int getName() {
            return R.string.trip_log_lure_shrimp;
        }
    },

    SPINNER_BAIT {

        @Override
        public int getName() {
            return R.string.trip_log_lure_spinner_bait;
        }
    },

    SPOON {

        @Override
        public int getName() {
            return R.string.trip_log_lure_spoon;
        }
    },


    SQUID {

        @Override
        public int getName() {
            return R.string.trip_log_lure_squid;
        }
    },

    SWIM_BAIT {

        @Override
        public int getName() {
            return R.string.trip_log_lure_swim_bait;
        }
    },

    TOP_WATER {

        @Override
        public int getName() {
            return R.string.trip_log_lure_top_water;
        }
    },

    WORM {

        @Override
        public int getName() {
            return R.string.trip_log_lure_worm;
        }
    };



    @Override
    public int getId() {
        return ordinal() + 1;
    }
}
