package com.reelsonar.ibobber.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by james on 9/15/14.
 */
public class Sound implements SoundPool.OnLoadCompleteListener {

    private SoundPool _soundPool;
    private boolean _loaded;
    private int _playQueueCount;
    private int _soundId;

    public Sound(final Context context, final int resource, final int maxStreams) {
        _soundPool = new SoundPool(maxStreams, AudioManager.STREAM_NOTIFICATION, 0);
        _soundPool.setOnLoadCompleteListener(this);
        _soundId = _soundPool.load(context, resource, 1);
    }

    public void play() {
        if (_loaded) {
            _soundPool.play(_soundId, 1.f, 1.f, 0, 0, 1.f);
        } else {
            ++_playQueueCount;
        }
    }

    public void release() {
        _soundPool.release();
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (status == 0) {
            _loaded = true;
            for (int i = 0; i < _playQueueCount; ++i) {
                play();
            }
        }
    }

}
