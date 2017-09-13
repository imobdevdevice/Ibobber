package com.reelsonar.ibobber.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by james on 9/8/14.
 */
public class GrowableIntArray {

    public interface IntInterator {
        boolean hasNext();
        int next();
        int nextIndex();
    }

    private int[] _buffer;
    private int _size;

    public GrowableIntArray(List<Integer> intList) {
        this(intList.size());

        for (int i : intList) {
            add(i);
        }
    }

    public GrowableIntArray(int[] buffer) {
        _buffer = buffer;
        _size = buffer.length;
    }

    public GrowableIntArray(int[] buffer, int size) {
        _buffer = buffer;
        _size = size;
    }

    public GrowableIntArray(int initialCapacity) {
        _buffer = new int[initialCapacity];
        _size = 0;
    }

    public GrowableIntArray() {
        this(10);
    }

    public GrowableIntArray(GrowableIntArray intArray) {
        int[] buffer = new int[intArray.size()];
        System.arraycopy(intArray.buffer(), 0, buffer, 0, buffer.length);
        _buffer = buffer;
        _size = buffer.length;
    }

    public void add(int val) {
        if (_size == _buffer.length) {
            int[] buffer = new int[_size * 2];
            System.arraycopy(_buffer, 0, buffer, 0, _size);
            _buffer = buffer;
        }

        _buffer[_size] = val;
        ++_size;
    }

    public int get(int index) {
        return _buffer[index];
    }

    public int size() {
        return _size;
    }

    public int[] buffer() {
        return _buffer;
    }

    public GrowableIntArray subArray(int startIndex, int endIndex) {
        int size = endIndex - startIndex;
        int[] buffer = new int[size];
        System.arraycopy(_buffer, startIndex, buffer, 0, size);
        return new GrowableIntArray(buffer);
    }

    public void sort() {
        Arrays.sort(_buffer);
    }

    public IntInterator intInterator() {
        return new Iterator();
    }

    private class Iterator implements IntInterator {
        private int _pos;

        @Override
        public boolean hasNext() {
            return _pos < _size;
        }

        @Override
        public int next() {
            int val = _buffer[_pos];
            ++_pos;
            return val;
        }

        @Override
        public int nextIndex() {
            return _pos;
        }
    }

}
