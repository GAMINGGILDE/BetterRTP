package me.SuperRonanCraft.BetterRTP.references.web;

import com.google.gson.JsonParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/** Parsed, explicit result of the external log service. */
public record LogUploadResult(String key, String error) {

    public static LogUploadResult parse(String response) {
        if (response == null || response.isBlank()) {
            return failure("The log service returned no response");
        }
        try {
            JsonElement keyElement =
                    JsonParser.parseString(response).getAsJsonObject().get("key");
            if (keyElement == null || !keyElement.isJsonPrimitive()) {
                return failure("The log service returned no key");
            }
            String key = keyElement.getAsString();
            if (key.isBlank()) {
                return failure("The log service returned an empty key");
            }
            return new LogUploadResult(key, null);
        } catch (JsonParseException | IllegalStateException exception) {
            return failure("The log service returned an invalid response");
        }
    }

    public static LogUploadResult failure(String error) {
        return new LogUploadResult(null, error);
    }

    public boolean successful() {
        return key != null;
    }
}
