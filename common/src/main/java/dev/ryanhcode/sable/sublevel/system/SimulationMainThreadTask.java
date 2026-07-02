package dev.ryanhcode.sable.sublevel.system;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;

@FunctionalInterface
public interface SimulationMainThreadTask {
    void run(ServerSubLevelContainer container);
}
