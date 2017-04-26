package io.github.gosella.fastmove;

import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RotateManager extends BukkitRunnable {
    private Main plugin;
    private CraftPlayer player;
    private World world;
    private int posX;
    private int posY;
    private int posZ;
    private int count;

    public RotateManager(Main plugin, CraftPlayer player, World world, int posX, int posY, int posZ) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.count = 48;
    }

    private ChunkSection getSection(Chunk chunk, int chunkY) {
        ChunkSection[] sections = chunk.getSections();
        ChunkSection section = sections[chunkY];
        if (section == Chunk.a) {
            section = new ChunkSection(chunkY << 4, !this.world.worldProvider.m());
            sections[chunkY] = section;
        }
        return section;
    }

    public void run() {
        if (--count < 0) {
            this.plugin.getServer().broadcastMessage("Movimiento terminado!");
            this.cancel();
            return;
        }

        long startTime = System.nanoTime();

        long midTime = System.nanoTime();
        this.plugin.getLogger().info("Tiempo movimiento:   " + (midTime - startTime) + " ns");

        long endTime = System.nanoTime();
        this.plugin.getLogger().info("Tiempo comunicación: " + (endTime - midTime) + " ns");
    }
}
