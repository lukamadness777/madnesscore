package dev.lukamadness.madnesscore.common.network.util;

public final class PixelPacking {
    private PixelPacking() {}

    public static byte[] pack(boolean[][] pixels) {
        int height = pixels.length;
        int width = height > 0 ? pixels[0].length : 0;
        int totalBits = width * height;
        byte[] out = new byte[(totalBits + 7) / 8];

        int bitIndex = 0;
        for (boolean[] row : pixels) {
            for (boolean value : row) {
                if (value) {
                    out[bitIndex / 8] |= (byte) (1 << (bitIndex % 8));
                }
                bitIndex++;
            }
        }
        return out;
    }

    public static boolean[][] unpack(byte[] packed, int width, int height) {
        boolean[][] out = new boolean[height][width];
        int bitIndex = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int byteIndex = bitIndex / 8;
                boolean value = byteIndex < packed.length
                        && (packed[byteIndex] & (1 << (bitIndex % 8))) != 0;
                out[row][col] = value;
                bitIndex++;
            }
        }
        return out;
    }
}
