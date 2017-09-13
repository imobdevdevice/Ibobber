// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model;

public class EchoPeak {

    public enum Type { BOTTOM, FISH };

    private static final double UNPUBLISHED_TI_DELAY = 250;
    private static final double BAD_IBOBBER_DELAY = -850;   //reduces depth slightly to compensate for bad ibobber being unable to report under 7ft or so

    private static final double GLOBAL_PING_SAMPLE_PERIOD = 132;
    private static final double GLOBAL_LPF_DELAY = 66;
    private static final double GLOBAL_TBURST = 208;
    private static final double GLOBAL_TX_OFF_TO_RX_ON = 500;
    private static final double GLOBAL_RX_ON_TO_RX = 500;

    private static final double ECHO_RETURN_MICROSECONDS = GLOBAL_TBURST + GLOBAL_TX_OFF_TO_RX_ON + GLOBAL_RX_ON_TO_RX;
    private static final float SECONDS_PER_MICROSECOND = 0.000001f;
    private static final double TEMP_OFFSET_CELSIUS = 2.778;

    private int _location;
    private int _amplitude;
    private Type _type;
    private FishSize _fishSize;
    private boolean _badBobber;

    public EchoPeak(int location, int amplitude, Type type, boolean badBobber) {
        _location = location;
        _amplitude = amplitude;
        _type = type;
        _badBobber = badBobber;
    }

    public double depth(double prevDepth, double tempCelsius) {
        double surfacePressure = 101.325 + 1025*9.81*0/1000;
        double surfaceSpeedOfSound = speedOfSound(0, tempCelsius, surfacePressure / 10.0); // Convert from kPa to dbar.

        double depthPressure = 101.325 + 1025*9.81*prevDepth/1000;
        double depthSpeedOfSound = speedOfSound(0, tempCelsius, depthPressure / 10.0); // Convert from kPa to dbar.

        double speedOfSound = (surfaceSpeedOfSound + depthSpeedOfSound) / 2.0;

        double delay = ECHO_RETURN_MICROSECONDS;

        if (_badBobber) delay += BAD_IBOBBER_DELAY;
        else delay+= UNPUBLISHED_TI_DELAY;

        double echoReturn = _location * GLOBAL_PING_SAMPLE_PERIOD + delay;
        return ((echoReturn - GLOBAL_LPF_DELAY) * speedOfSound * SECONDS_PER_MICROSECOND) / 2.0;
    }

    public int getLocation() {
        return _location;
    }

    public int getAmplitude() {
        return _amplitude;
    }

    public Type getType() {
        return _type;
    }

    public FishSize getFishSize() {
        return _fishSize;
    }

    public void setFishSize(FishSize fishSize) {
        _fishSize = fishSize;
    }

    private static double speedOfSound(double sa /* salinity */, double ct /* tempCelsius */, double p /* pressure */) {
        ct -= TEMP_OFFSET_CELSIUS;

        double  v01 =  9.998420897506056e+2, v02 =  2.839940833161907e0,
                v03 = -3.147759265588511e-2, v04 =  1.181805545074306e-3,
                v05 = -6.698001071123802e0,  v06 = -2.986498947203215e-2,
                v07 =  2.327859407479162e-4, v08 = -3.988822378968490e-2,
                v09 =  5.095422573880500e-4, v10 = -1.426984671633621e-5,
                v11 =  1.645039373682922e-7, v12 = -2.233269627352527e-2,
                v13 = -3.436090079851880e-4, v14 =  3.726050720345733e-6,
                v15 = -1.806789763745328e-4, v16 =  6.876837219536232e-7,
                v17 = -3.087032500374211e-7, v18 = -1.988366587925593e-8,
                v19 = -1.061519070296458e-11,v20 =  1.550932729220080e-10,
                v21 =  1.0e0,
                v22 =  2.775927747785646e-3, v23 = -2.349607444135925e-5,
                v24 =  1.119513357486743e-6, v25 =  6.743689325042773e-10,
                v26 = -7.521448093615448e-3, v27 = -2.764306979894411e-5,
                v28 =  1.262937315098546e-7, v29 =  9.527875081696435e-10,
                v30 = -1.811147201949891e-11,v31 = -3.303308871386421e-5,
                v32 =  3.801564588876298e-7, v33 = -7.672876869259043e-9,
                v34 = -4.634182341116144e-11,v35 =  2.681097235569143e-12,
                v36 =  5.419326551148740e-6, v37 = -2.742185394906099e-5,
                v38 = -3.212746477974189e-7, v39 =  3.191413910561627e-9,
                v40 = -1.931012931541776e-12,v41 = -1.105097577149576e-7,
                v42 =  6.211426728363857e-10,v43 = -1.119011592875110e-10,
                v44 = -1.941660213148725e-11,v45 = -1.864826425365600e-14,
                v46 =  1.119522344879478e-14,v47 = -1.200507748551599e-15,
                v48 =  6.057902487546866e-17 ,
                c01 = -2.233269627352527e-2, c02 = -3.436090079851880e-4,
                c03 =  3.726050720345733e-6, c04 = -1.806789763745328e-4,
                c05 =  6.876837219536232e-7, c06 = -6.174065000748422e-7,
                c07 = -3.976733175851186e-8, c08 = -2.123038140592916e-11,
                c09 =  3.101865458440160e-10,c10 = -2.742185394906099e-5,
                c11 = -3.212746477974189e-7, c12 =  3.191413910561627e-9,
                c13 = -1.931012931541776e-12,c14 = -1.105097577149576e-7,
                c15 =  6.211426728363857e-10,c16 = -2.238023185750219e-10,
                c17 = -3.883320426297450e-11,c18 = -3.729652850731201e-14,
                c19 =  2.239044689758956e-14,c20 = -3.601523245654798e-15,
                c21 =  1.817370746264060e-16;

        double  v_hat_denominator, v_hat_numerator;
        double  sqrtsa, dvden_dp, dvnum_dp, dp_drho;

        sqrtsa                  = Math.sqrt(sa);

        v_hat_denominator       =
                v01 + ct*(v02 + ct*(v03 + v04*ct))
                        + sa*(v05 + ct*(v06 + v07*ct)
                        + sqrtsa*(v08 + ct*(v09 + ct*(v10 + v11*ct))))
                        + p*(v12 + ct*(v13 + v14*ct) + sa*(v15 + v16*ct)
                        + p*(v17 + ct*(v18 + v19*ct) + v20*sa));
        v_hat_numerator         =
                v21 + ct*(v22 + ct*(v23 + ct*(v24 + v25*ct)))
                        + sa*(v26 + ct*(v27 + ct*(v28 + ct*(v29 + v30*ct))) + v36*sa
                        + sqrtsa*(v31 + ct*(v32 + ct*(v33 + ct*(v34 + v35*ct)))))
                        + p*(v37 + ct*(v38 + ct*(v39 + v40*ct))
                        + sa*(v41 + v42*ct)
                        + p*(v43 + ct*(v44 + v45*ct + v46*sa)
                        + p*(v47 + v48*ct)));

        dvden_dp                =
                c01 + ct*(c02 + c03*ct)
                        + sa*(c04 + c05*ct)
                        + p*(c06 + ct*(c07 + c08*ct) + c09*sa);

        dvnum_dp                =
                c10 + ct*(c11 + ct*(c12 + c13*ct))
                        + sa*(c14 + c15*ct)
                        + p*(c16 + ct*(c17 + c18*ct + c19*sa)
                        + p*(c20 + c21*ct));

        dp_drho                 =
                (v_hat_numerator*v_hat_numerator)/
                        (dvden_dp*v_hat_numerator - dvnum_dp*v_hat_denominator);

        return (100.0*Math.sqrt(dp_drho));
    }

}
