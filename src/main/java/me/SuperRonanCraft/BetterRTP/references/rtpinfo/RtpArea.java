package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import me.SuperRonanCraft.BetterRTP.player.rtp.RTP_SHAPE;

import java.util.random.RandomGenerator;

/** Pure coordinate calculations for RTP areas. */
public record RtpArea(int centerX, int centerZ, int minRadius, int maxRadius, RTP_SHAPE shape) {

    public RtpArea {
        if (minRadius < 0 || maxRadius <= minRadius) {
            throw new IllegalArgumentException("RTP radii must satisfy 0 <= minRadius < maxRadius");
        }
    }

    public boolean contains(int x, int z) {
        long offsetX = (long) x - centerX;
        long offsetZ = (long) z - centerZ;
        return switch (shape) {
            case CIRCLE -> {
                long distanceSquared = offsetX * offsetX + offsetZ * offsetZ;
                long minSquared = (long) minRadius * minRadius;
                long maxSquared = (long) maxRadius * maxRadius;
                yield distanceSquared >= minSquared && distanceSquared <= maxSquared;
            }
            case SQUARE -> {
                long absX = Math.abs(offsetX);
                long absZ = Math.abs(offsetZ);
                yield (absX >= minRadius || absZ >= minRadius)
                        && absX <= maxRadius && absZ <= maxRadius;
            }
        };
    }

    public Point randomPoint(RandomGenerator random) {
        return switch (shape) {
            case CIRCLE -> randomCirclePoint(random);
            case SQUARE -> randomSquarePoint(random);
        };
    }

    private Point randomCirclePoint(RandomGenerator random) {
        double minSquared = (double) minRadius * minRadius;
        double maxSquared = (double) maxRadius * maxRadius;
        double radius = Math.sqrt(random.nextDouble(minSquared, maxSquared));
        double angle = random.nextDouble(0.0, Math.PI * 2.0);
        int x = centerX + (int) Math.round(radius * Math.cos(angle));
        int z = centerZ + (int) Math.round(radius * Math.sin(angle));
        return clampToArea(x, z, random);
    }

    private Point randomSquarePoint(RandomGenerator random) {
        int x = random.nextInt(-maxRadius, maxRadius + 1);
        int z;
        if (Math.abs(x) >= minRadius) {
            z = random.nextInt(-maxRadius, maxRadius + 1);
        } else {
            int distance = random.nextInt(minRadius, maxRadius + 1);
            z = random.nextBoolean() ? distance : -distance;
        }
        return new Point(centerX + x, centerZ + z);
    }

    private Point clampToArea(int x, int z, RandomGenerator random) {
        if (contains(x, z)) {
            return new Point(x, z);
        }
        // Rounding can move a point by one block over a circular boundary.
        return randomCirclePoint(random);
    }

    public record Point(int x, int z) {
    }
}
