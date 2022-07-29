package net.mcblockbuilds.mcbbcooldownlib;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
public class Cache<T> {

    private final SaveRunnable<T> saveRunnable;
    private final Plugin plugin;

    private final int cooldownSeconds;

    private final HashMap<UUID, Long> lastSaved = new HashMap<>();
    private final HashMap<UUID, Runnable> scheduledSaves = new HashMap<>();
    private final HashMap<UUID, T> cache = new HashMap<>();


    /**
     * Create a new cache for a type of data.
     * @param plugin Plugin instance.
     * @param saveRunnable Code to run async when saving to the database.
     * @param cooldownSeconds Min seconds between each save to the database.
     */
    public Cache(Plugin plugin, SaveRunnable<T> saveRunnable, int cooldownSeconds) {
        this.plugin = plugin;
        this.saveRunnable = saveRunnable;
        this.cooldownSeconds = cooldownSeconds;
    }


    /**
     * Does the cache contain this data?
     * @param uuid UUID of data requested.
     * @return True or false.
     */
    public boolean isCached(UUID uuid) {
        return cache.containsKey(uuid);
    }


    /**
     * Get data from the cache if saved or null.
     * @param uuid UUID of data requested.
     * @return The data returned or null.
     */
    public @Nullable T getData(UUID uuid) {
        return cache.getOrDefault(uuid, null);
    }


    /**
     * Save the data to the cache only.
     * @param uuid UUID of data being saved.
     * @param data The data to save.
     */
    public void cacheData(UUID uuid, T data) {
        cache.put(uuid, data);
    }


    /**
     * Run all the scheduled saves now.
     */
    public void runAllScheduledSaves() {
        Collection<Runnable> r = new ArrayList<>(scheduledSaves.values()); // Create a copy to avoid CME
        for (Runnable t : r) {
            t.run();
        }
    }


    /**
     * Run specific scheduled save now.
     * @param uuid UUID of data to save.
     */
    public void runSpecificScheduledSave(UUID uuid) {
        if (!scheduledSaves.containsKey(uuid)) return;
        scheduledSaves.get(uuid).run();
    }


    /**
     * Save data to the cache, and to the database if within cooldown.
     * @param uuid UUID of data being saved.
     * @param data The data to save.
     */
    public void saveData(UUID uuid, T data) {

        cacheData(uuid, data);

        // Save to database
        Runnable save = ()->{
            lastSaved.put(uuid, System.currentTimeMillis());

            // Run async if not onDisable
            if (plugin.isEnabled()) Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> saveRunnable.run(uuid, data));
            else saveRunnable.run(uuid, data);
        };

        // Save to db if last save was over a minute ago
        if (lastSaved.getOrDefault(uuid, 1L) <= System.currentTimeMillis() - (cooldownSeconds * 1000L)) {
            save.run();
            return;
        }

        // Last save was less than a minute ago, so schedule a save unless already scheduled
        if (scheduledSaves.containsKey(uuid)) return;

        // Schedule a save
        Runnable scheduledSave = () -> {
            scheduledSaves.remove(uuid);
            save.run();
        };

        long agoMill = System.currentTimeMillis() - lastSaved.get(uuid);
        long inMill = (cooldownSeconds * 1000L) - agoMill;
        int inTicks = (int) (inMill/50);
        Bukkit.getScheduler().runTaskLater(plugin, scheduledSave, inTicks); // Should be ticks remaining till a min
        scheduledSaves.put(uuid, scheduledSave);

    }


}
