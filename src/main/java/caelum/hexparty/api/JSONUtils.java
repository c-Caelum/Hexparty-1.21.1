package caelum.hexparty.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public class JSONUtils {
    public static boolean isString(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }
    public static boolean isNumber(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }
    public static boolean isBoolean(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
    }

    public static boolean isString(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }
    public static boolean isNumber(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }
    public static boolean isBoolean(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
    }

    public static boolean isList(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return element.isJsonArray();
    }
    public static boolean isListOf(JsonObject obj, String key, Function<JsonElement, Boolean> predicate) {
        JsonElement element = obj.get(key);
        if (!element.isJsonArray()) {
            return false;
        }
        JsonArray array = element.getAsJsonArray();

        for (JsonElement el : array.asList()) {
            if (!(predicate.apply(el))) {
                return false;
            }
        }

        return true;
    }
}
