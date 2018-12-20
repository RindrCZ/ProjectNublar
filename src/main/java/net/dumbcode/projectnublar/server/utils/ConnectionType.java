package net.dumbcode.projectnublar.server.utils;

import lombok.Getter;

@Getter
public enum ConnectionType {
    LIGHT_STEEL(2, 3, 1/32F, 90F),
    HIGH_SECURITY(1, 8, 1/16F, 0F);
    private final double[] offsets;
    private final int height;
    private final float cableWidth;
    private final float rotationOffset;
    ConnectionType(int amount, int height, float cableWidth, float defaultRotation) {
        this.offsets = new double[amount];
        this.height = height;
        this.cableWidth = cableWidth;
        this.rotationOffset = defaultRotation;
        double off = 1D / (amount * 2);
        for (int i = 0; i < amount; i++) {
            this.offsets[i] = (i*2 + 1) * off;
        }
    }

    public static ConnectionType getType(int id) {
        return values()[id % values().length];
    }
}