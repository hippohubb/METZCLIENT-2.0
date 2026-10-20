package dev.metzclient.schematic;

import java.util.ArrayList;
import java.util.List;

/** Simple text blueprint format: one block identifier per line, row-major, dimensions in a sidecar header. */
public record Blueprint(String name, int width, int height, int length, List<String> blockIds) {
    public Blueprint {
        blockIds = List.copyOf(blockIds == null ? new ArrayList<>() : blockIds);
    }
    public int expectedBlocks() { return Math.max(0, width) * Math.max(0, height) * Math.max(0, length); }
    public boolean isValid() { return width > 0 && height > 0 && length > 0 && blockIds.size() == expectedBlocks(); }
}
