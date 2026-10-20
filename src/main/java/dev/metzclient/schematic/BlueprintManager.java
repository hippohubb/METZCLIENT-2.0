package dev.metzclient.schematic;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads a deliberately simple, auditable text blueprint format from config/metzclient/blueprints.
 * First line: width height length. Remaining lines: namespaced block IDs, one per block.
 */
public final class BlueprintManager {
    private final Path directory = FabricLoader.getInstance().getConfigDir().resolve("metzclient/blueprints");

    public Path directory() {
        try { Files.createDirectories(directory); } catch (IOException ignored) { }
        return directory;
    }

    public Blueprint load(String filename) throws IOException {
        Path file = directory().resolve(filename).normalize();
        if (!file.getParent().equals(directory.normalize())) throw new IOException("Invalid blueprint path");
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) throw new IOException("Blueprint is empty");
        String[] dimensions = lines.get(0).trim().split("\\s+");
        if (dimensions.length != 3) throw new IOException("First line must be: width height length");
        int w = Integer.parseInt(dimensions[0]), h = Integer.parseInt(dimensions[1]), l = Integer.parseInt(dimensions[2]);
        List<String> ids = lines.subList(1, lines.size()).stream().map(String::trim).filter(s -> !s.isBlank()).toList();
        Blueprint blueprint = new Blueprint(file.getFileName().toString(), w, h, l, ids);
        if (!blueprint.isValid()) throw new IOException("Expected " + blueprint.expectedBlocks() + " block IDs, found " + ids.size());
        return blueprint;
    }
}
