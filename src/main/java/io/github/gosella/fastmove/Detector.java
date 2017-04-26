package io.github.gosella.fastmove;

/**
 * Created by german on 24/04/17.
 */
public interface Detector {
    byte[] getDetected();
    int getMinX();
    int getMaxX();
    int getMinY();
    int getMaxY();
    int getMinZ();
    int getMaxZ();
    void detect();
}
