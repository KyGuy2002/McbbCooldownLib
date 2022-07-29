package net.mcblockbuilds.mcbbcooldownlib;

import java.util.UUID;

@FunctionalInterface
public interface SaveRunnable<T> {

    void run(UUID uuid, T data);

}
