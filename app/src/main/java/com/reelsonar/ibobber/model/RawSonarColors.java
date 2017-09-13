package com.reelsonar.ibobber.model;

import android.graphics.Color;

import java.util.*;

/**
 * @author Brian Gebala
 * @version 2/16/16
 */
public class RawSonarColors {
    private final static int NUM_BASE_COLORS = 4;

    private final static int[][] COLORS = {
            { 0x00, 0x36, 0x5d },
            { 0x2f, 0xc5, 0x74 },
            { 0x7f, 0xff, 0x46 },
            { 0xe7, 0x4e, 0x0c }
    };

    private int _numColors;
    private Map<Integer, Integer> _amplitudeColors = new HashMap<>();
    private List<Integer> _orderedAmplitues;

    public RawSonarColors(final int maxAmplitude, final int numColors) {
        _numColors = numColors;
        _orderedAmplitues = new ArrayList<>(numColors);

        // Based on http://www.andrewnoske.com/wiki/Code_-_heatmaps_and_color_gradients

        int step = maxAmplitude / _numColors;

        for (int i = 0; i < _numColors; i++) {
            float value = i * (1.f / (float)_numColors);

            // Our desired color will be between these two indexes in `color`.
            int idx1;
            int idx2;

            // Fraction between "idx1" and "idx2" where our value is.
            float fractBetween = 0;

            if (value <= 0) {
                idx1 = idx2 = 0;
            }
            else if (value >= 1) {
                idx1 = idx2 = NUM_BASE_COLORS-1;
            }
            else {
                value = value * (NUM_BASE_COLORS-1);   // Will multiply value by 3.
                idx1  = (int)Math.floor(value);        // Our desired color will be after this index.
                idx2  = idx1+1;                        // ... and before this index (inclusive).
                fractBetween = value - (float)idx1;    // Distance between the two indexes (0-1).
            }

            int red = (int)((COLORS[idx2][0] - COLORS[idx1][0]) * fractBetween + COLORS[idx1][0]);
            int green = (int)((COLORS[idx2][1] - COLORS[idx1][1]) * fractBetween + COLORS[idx1][1]);
            int blue = (int)((COLORS[idx2][2] - COLORS[idx1][2]) * fractBetween + COLORS[idx1][2]);

            int amplitudeColor = Color.argb(255, red, green, blue);
            _amplitudeColors.put(i * step, amplitudeColor);
        }

        _orderedAmplitues = new ArrayList<>(_amplitudeColors.keySet());
        Collections.sort(_orderedAmplitues);
        Collections.reverse(_orderedAmplitues);
    }

    public int getNumColors() {
        return _numColors;
    }

    public int getColorForAmplitude(final int amplitude) {
        int color = _amplitudeColors.get(0);

        for (int anOrderedAmplitude : _orderedAmplitues) {
            if (amplitude >= anOrderedAmplitude) {
                color = _amplitudeColors.get(anOrderedAmplitude);
                break;
            }
        }

        return color;
    }

    public List<Integer> getAllColors() {
        List<Integer> colors = new ArrayList<>(_numColors);

        for (int amplitude : _orderedAmplitues) {
            colors.add(_amplitudeColors.get(amplitude));
        }

        Collections.reverse(colors);

        return colors;
    }
}
