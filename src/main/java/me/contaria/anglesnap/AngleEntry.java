package me.contaria.anglesnap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class AngleEntry {
    public String name;
    public float yaw;
    public float pitch;
    public int icon;
    public int color;

    public AngleEntry(float yaw, float pitch) {
        this("", yaw, pitch);
    }

    public AngleEntry(String name, float yaw, float pitch) {
        this(name, yaw, pitch, 0, CommonColors.RED);
    }

    public AngleEntry(String name, float yaw, float pitch, int icon, int color) {
        this.name = name;
        this.yaw = yaw;
        this.pitch = pitch;
        this.icon = icon;
        this.color = color;
    }

    public Identifier nextIcon() {
        Identifier next = this.getIcon(++this.icon);
        if (Minecraft.getInstance().getResourceManager().getResource(next).isPresent()) {
            return next;
        }
        return this.getIcon(this.icon = 0);
    }

    public Identifier getIcon() {
        return this.getIcon(this.icon);
    }

    private Identifier getIcon(int icon) {
        return Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/marker-" + icon + ".png");
    }

    public float getDistance(float yaw, float pitch) {
        return AngleSnapMath.angleDistance(yaw, pitch, this.yaw, this.pitch);
    }

    public void snap() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            float yaw = AngleSnapMath.wrapDegrees(this.yaw);
            float pitch = AngleSnapMath.clampPitch(this.pitch);

            player.setYRot(yaw);
            player.setXRot(pitch);
            player.yRotO = yaw;
            player.xRotO = pitch;
            player.setYHeadRot(yaw);
            player.yHeadRotO = yaw;
        }
    }

    public static JsonObject toJson(AngleEntry angle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("name", new JsonPrimitive(angle.name));
        jsonObject.add("yaw", new JsonPrimitive(angle.yaw));
        jsonObject.add("pitch", new JsonPrimitive(angle.pitch));
        jsonObject.add("icon", new JsonPrimitive(angle.icon));
        jsonObject.add("color", new JsonPrimitive(angle.color));
        return jsonObject;
    }

    public static AngleEntry fromJson(JsonObject jsonObject) {
        return new AngleEntry(
                GsonHelper.getAsString(jsonObject, "name"),
                GsonHelper.getAsFloat(jsonObject, "yaw"),
                GsonHelper.getAsFloat(jsonObject, "pitch"),
                GsonHelper.getAsInt(jsonObject, "icon", 0),
                GsonHelper.getAsInt(jsonObject, "color", CommonColors.RED)
        );
    }

    public static JsonObject listToJson(List<AngleEntry> angles) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (AngleEntry angle : angles) {
            jsonArray.add(toJson(angle));
        }
        jsonObject.add("angles", jsonArray);
        return jsonObject;
    }

    public static List<AngleEntry> listFromJson(JsonObject jsonObject) {
        List<AngleEntry> angles = new ArrayList<>();
        for (JsonElement angle : jsonObject.getAsJsonArray("angles")) {
            angles.add(fromJson(angle.getAsJsonObject()));
        }
        return angles;
    }
}
