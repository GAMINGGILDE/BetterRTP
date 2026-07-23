package me.SuperRonanCraft.BetterRTP.references.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogUploadResultTest {

    @Test
    void parsesSuccessfulResponse() {
        LogUploadResult result = LogUploadResult.parse("{\"key\":\"abc123\"}");

        assertTrue(result.successful());
        assertEquals("abc123", result.key());
    }

    @Test
    void turnsMissingOrMalformedResponsesIntoExplicitFailures() {
        assertFalse(LogUploadResult.parse(null).successful());
        assertFalse(LogUploadResult.parse("").successful());
        assertFalse(LogUploadResult.parse("{}").successful());
        assertFalse(LogUploadResult.parse("not-json").successful());
    }
}
