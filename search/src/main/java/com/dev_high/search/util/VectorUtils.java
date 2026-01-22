package com.dev_high.search.util;

import java.util.ArrayList;
import java.util.List;

public final class VectorUtils {
    private VectorUtils() {}

    public static float[] meanVector(List<float[]> vectors) {
        int dims = vectors.get(0).length;
        float[] out = new float[dims];

        for (float[] v : vectors) {
            for (int i = 0; i < dims; i++) {
                out[i] += v[i];
            }
        }

        float inv = 1.0f / vectors.size();
        for (int i = 0; i < dims; i++) {
            out[i] *= inv;
        }

        return out;
    }

    public static List<Float> toFloatList(float[] arr) {
        List<Float> list = new ArrayList<>(arr.length);
        for (float f : arr) {
            list.add(f);
        }
        return list;
    }
}