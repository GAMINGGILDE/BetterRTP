package me.SuperRonanCraft.BetterRTP.references.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseVersionTest {

    @Test
    void stripsGitHubTagPrefixForDisplay() {
        assertEquals("4.0.0", ReleaseVersion.displayName("v4.0.0"));
        assertEquals("4.0.0", ReleaseVersion.displayName("4.0.0"));
    }

    @Test
    void comparesNumericVersionPartsInsteadOfRawText() {
        assertTrue(ReleaseVersion.compare("3.10.0", "3.9.9") > 0);
        assertTrue(ReleaseVersion.compare("4.0.0", "3.7") > 0);
        assertEquals(0, ReleaseVersion.compare("3.7", "3.7.0"));
    }

    @Test
    void stableReleaseIsNewerThanMatchingPrerelease() {
        assertTrue(ReleaseVersion.compare("4.0.0", "4.0.0-SNAPSHOT") > 0);
        assertTrue(ReleaseVersion.compare("4.0.0-rc.2", "4.0.0") < 0);
    }

    @Test
    void olderPublishedReleaseDoesNotDowngradeDevelopmentBuild() {
        assertTrue(ReleaseVersion.compare("3.7", "4.0.0-SNAPSHOT") < 0);
    }
}
