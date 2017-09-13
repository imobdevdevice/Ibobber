package com.reelsonar.ibobber.sonar;

/**
 * @author Brian Gebala
 * @version 2/15/16
 */
public class SonarLiveRawActivity extends SonarActivity {
    @Override
    protected Mode getMode() {
        return Mode.RAW;
    }
}
