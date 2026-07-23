package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import me.SuperRonanCraft.BetterRTP.player.rtp.RTP_SHAPE;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtpAreaTest {

    @Test
    void circleUsesSquaredDistanceAndHonorsBothBoundaries() {
        RtpArea area = new RtpArea(100, -50, 10, 100, RTP_SHAPE.CIRCLE);

        assertTrue(area.contains(110, -50));
        assertTrue(area.contains(200, -50));
        assertTrue(area.contains(160, 30)); // 3-4-5 triangle scaled by 20
        assertFalse(area.contains(100, -50));
        assertFalse(area.contains(201, -50));
        assertFalse(area.contains(180, 30));
    }

    @Test
    void squareRepresentsAFrameAroundTheMinimumRadius() {
        RtpArea area = new RtpArea(0, 0, 10, 100, RTP_SHAPE.SQUARE);

        assertTrue(area.contains(10, 0));
        assertTrue(area.contains(-100, 100));
        assertTrue(area.contains(5, -10));
        assertFalse(area.contains(9, 9));
        assertFalse(area.contains(101, 0));
    }

    @Test
    void generatedPointsAlwaysStayInsideTheirArea() {
        for (RTP_SHAPE shape : RTP_SHAPE.values()) {
            RtpArea area = new RtpArea(1234, -987, 25, 500, shape);
            Random random = new Random(42);

            for (int i = 0; i < 20_000; i++) {
                RtpArea.Point point = area.randomPoint(random);
                assertTrue(area.contains(point.x(), point.z()),
                        () -> shape + " point outside area: " + point);
            }
        }
    }

    @Test
    void rejectsInvalidRadii() {
        assertThrows(IllegalArgumentException.class,
                () -> new RtpArea(0, 0, -1, 100, RTP_SHAPE.CIRCLE));
        assertThrows(IllegalArgumentException.class,
                () -> new RtpArea(0, 0, 100, 100, RTP_SHAPE.SQUARE));
        assertThrows(IllegalArgumentException.class,
                () -> new RtpArea(0, 0, 101, 100, RTP_SHAPE.SQUARE));
    }
}
