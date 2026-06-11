package me.contaria.anglesnap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class CameraPosEntry {
    public String name;
    public double x;
    public double y;
    public double z;
    
    public CameraPosEntry(double x, double y, double z) {
        this("", x, y, z);
    }
    
    public CameraPosEntry(String name, double x, double y, double z) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static JsonObject toJson(CameraPosEntry pos) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("name", new JsonPrimitive(pos.name));
        jsonObject.add("x", new JsonPrimitive(pos.x));
        jsonObject.add("y", new JsonPrimitive(pos.y));
        jsonObject.add("z", new JsonPrimitive(pos.z));
        return jsonObject;
    }

    public static CameraPosEntry fromJson(JsonObject jsonObject) {
        return new CameraPosEntry(
                GsonHelper.getAsString(jsonObject, "name"),
                GsonHelper.getAsDouble(jsonObject, "x"),
                GsonHelper.getAsDouble(jsonObject, "y"),
                GsonHelper.getAsDouble(jsonObject, "z")
        );
    }

    public static JsonObject listToJson(List<CameraPosEntry> positions) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (CameraPosEntry pos : positions) {
            jsonArray.add(toJson(pos));
        }
        jsonObject.add("positions", jsonArray);
        return jsonObject;
    }

    public static List<CameraPosEntry> listFromJson(JsonObject jsonObject) {
        List<CameraPosEntry> positions = new ArrayList<>();
        for (JsonElement pos : jsonObject.getAsJsonArray("positions")) {
            positions.add(fromJson(pos.getAsJsonObject()));
        }
        return positions;
    }
}
