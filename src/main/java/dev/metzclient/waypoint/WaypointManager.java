package dev.metzclient.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Waypoint>>(){}.getType();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("metzclient-waypoints.json");
    private final List<Waypoint> waypoints = new ArrayList<>();

    public WaypointManager() { load(); }
    public List<Waypoint> all() { return List.copyOf(waypoints); }
    public void add(Waypoint waypoint) { waypoints.removeIf(w -> w.name().equalsIgnoreCase(waypoint.name())); waypoints.add(waypoint); save(); }
    public void remove(String name) { if (waypoints.removeIf(w -> w.name().equalsIgnoreCase(name))) save(); }

    private void load() {
        if (!Files.exists(FILE)) return;
        try {
            List<Waypoint> loaded = GSON.fromJson(Files.readString(FILE), LIST_TYPE);
            if (loaded != null) waypoints.addAll(loaded);
        } catch (Exception ignored) { }
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(waypoints, LIST_TYPE));
        } catch (IOException ignored) { }
    }
}
