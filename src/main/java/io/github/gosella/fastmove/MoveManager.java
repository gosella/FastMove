package io.github.gosella.fastmove;

import net.minecraft.server.v1_11_R1.*;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MoveManager extends BukkitRunnable {
    private Main plugin;
    private CraftPlayer player;
    private World world;
    private int posX;
    private int posY;
    private int posZ;
    private int count;

    public MoveManager(Main plugin, CraftPlayer player, World world, int posX) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.posX = posX;
        this.posY = 6 << 4;
        this.posZ = 4 << 4;
        this.count = 16;
    }

    private ChunkSection getSection(Chunk chunk, int chunkY) {
        ChunkSection[] sections = chunk.getSections();
        ChunkSection section = sections[chunkY];
        if (section == Chunk.a) {
            section = new ChunkSection(chunkY << 4, this.world.worldProvider.m());
            sections[chunkY] = section;
        }
        return section;
    }

    public void run() {
        if (--count < 0) {
            this.plugin.getServer().broadcastMessage("movimiento terminado");
            this.cancel();
            return;
        }

        long startTime = System.nanoTime();

        int chunkX = posX >> 4;
        int chunkY = posY >> 4;
        int chunkZ = posZ >> 4;

        int lenX = 16;
        int lenY = 16;
        int lenZ = 16;

        int deltaX = -1;
        int deltaY = 0;
        int deltaZ = 0;

        this.plugin.getLogger().info("Moviendo chunk " + chunkX + ", " + chunkY + ", " + chunkZ + " desde (" + posX + ", " + posY + ", " + posZ + ")");

        int srcFromX = this.posX;
        int srcToX = srcFromX + lenX;

        int srcFromY = this.posY;
        int srcToY = srcFromY + lenY;

        int srcFromZ = this.posZ;
        int srcToZ = srcFromZ + lenZ;

        int dstFromX = srcFromX + deltaX;
        int dstToX = srcToX + deltaX;

        Set<Chunk> affectedChunks = new LinkedHashSet<Chunk>(3);
        Set<ChunkSection> affectedSections = new HashSet<ChunkSection>(3);

        IChunkProvider chunkProvider = world.getChunkProvider();
//        IBlockData AIR = Blocks.AIR.getBlockData();
        IBlockData b;

        int srcX = srcFromX;
        int dstX = dstFromX;
        while (srcX < srcToX) {
            final int srcLen = Math.min(((srcX + 16) & ~15) - srcX, srcToX - srcX);
            final int dstLen = Math.min(((dstX + 16) & ~15) - dstX, dstToX - dstX);
            final int minLen = Math.min(srcLen, dstLen);

            final int srcChunkX = srcX >> 4;
            final int srcBeginX = srcX & 15;
            final int srcEndX = srcBeginX + minLen;

            final Chunk srcChunk = chunkProvider.getChunkAt(srcChunkX, chunkZ);
            final ChunkSection srcSection = this.getSection(srcChunk, chunkY);

            final int dstChunkX = dstX >> 4;
            final int dstBeginX = dstX & 15;
            final int dstEndX = dstBeginX + minLen;

            final Chunk dstChunk = chunkProvider.getChunkAt(dstChunkX, chunkZ);
            final ChunkSection dstSection = this.getSection(dstChunk, chunkY);

            affectedSections.add(dstSection);
            affectedSections.add(srcSection);

            affectedChunks.add(dstChunk);
            affectedChunks.add(srcChunk);

//            this.plugin.getLogger().info("copiando desde (" + srcBeginX + ", " + srcEndX +
//                    ") hasta (" + dstBeginX + ", " + dstEndX + ")");

            for (int sxx = srcBeginX, dxx = dstBeginX; sxx < srcEndX; ++sxx, ++dxx) {
                for (int zz = 0; zz < 16; ++zz) {
                    for (int yy = 0; yy < 16; ++yy) {
                        b = srcSection.getType(sxx, yy, zz);
                        dstSection.setType(dxx, yy, zz, b);
                        // Copy Sky Light
                        dstSection.a(dxx, yy, zz, srcSection.b(sxx, yy, zz));
                        // Copy Emitted Light
                        dstSection.b(dxx, yy, zz, srcSection.c(sxx, yy, zz));
                    }
                }
            }

            // worldserver.b contiene una lista de todas las TileEntities cargadas en el mundo.
            // chunk.tileEntities contiene solamente las entidades del chunk.
            // Afortunadamente, ambas contienen los mismos objetos, por lo que alcanza con
            // modificar únicamente las TilEntities del chunk.

            final List<TileEntity> tileEntities = new ArrayList<TileEntity>(srcChunk.tileEntities.values());
            for (TileEntity tileEntity : tileEntities) {
                BlockPosition position = tileEntity.getPosition();
//                this.plugin.getLogger().info("Considerando TileEntry " + tileEntity + " en " + position);
                final int positionX = position.getX() & 15;
                if (positionX < srcBeginX || positionX >= srcEndX)
                    continue;
                final int positionY = position.getY();
                if (positionY < srcFromY || positionY >= srcToY) {
                    continue;
                }
                final int positionZ = position.getZ();
                if (positionZ < srcFromZ || positionZ >= srcToZ) {
                    continue;
                }
                srcChunk.tileEntities.remove(position);
                BlockPosition newPosition = position.a(deltaX, deltaY, deltaZ);
                tileEntity.setPosition(newPosition);
                dstChunk.tileEntities.put(newPosition, tileEntity);
//                this.plugin.getLogger().info("Moví TileEntry " + tileEntity + " de " + position + " a " + newPosition);
            }


            // world.entityList contiene una lista de todas las entidades cargadas en el mundo.
            // chunk.getEntitySlices() contiene listas con las entidades del chunk en cada slice.
            // Afortunadamente, ambas listas contienen los mismos objetos, por lo que alcanza con
            // modificar únicamente las entities del chunk.

            List<Entity>[] entitySlices = srcChunk.getEntitySlices();
            List<Entity> entitySlice = entitySlices[chunkY];
            List<Entity> entities = new ArrayList<Entity>(entitySlice);

            for (Entity entity : entities) {
                final int positionX = MathHelper.floor(entity.getX()) & 15;
                if (positionX < srcBeginX || positionX >= srcEndX) {
                    continue;
                }
                final int positionY = MathHelper.floor(entity.getY());
                if (positionY < srcFromY || positionY >= srcToY) {
                    continue;
                }
                final int positionZ = MathHelper.floor(entity.getZ());
                if (positionZ < srcFromZ || positionZ >= srcToZ) {
                    continue;
                }
                entity.setPosition(entity.getX() + deltaX, entity.getY() + deltaY, entity.getZ() + deltaZ);
                if (srcChunk != dstChunk) {
                    srcChunk.a(entity, chunkY);
                    dstChunk.a(entity);
                }
            }

            srcX += minLen;
            dstX += minLen;
        }

        for (ChunkSection section : affectedSections) {
            section.recalcBlockCounts();
        }


        final long currentTime = this.world.worldData.getTime();
        for (Chunk chunk : affectedChunks) {
            // La lista de los bloques a los que hay que activar en el próximo tick sólo se encuentra
            // dentro del world, por lo que no es necesario interactuar con el chunk para actualizarla.

            List<NextTickListEntry> entries = world.a(chunk, true);
            if (entries != null) {
                this.plugin.getLogger().info("Analizando TickEntries del chunk " + chunk.k() + " con " + entries.size() + " entries");
                for (NextTickListEntry entry : entries) {
                    BlockPosition position = entry.a;
                    this.plugin.getLogger().info("Considerando entry " + Block.REGISTRY.b(entry.a()) + ": " + entry);
                    int pos = position.getX();
                    if (pos >= srcFromX && pos < srcToX) {
                        pos = position.getY();
                        if (pos >= srcFromY && pos < srcToY) {
                            pos = position.getZ();
                            if (pos >= srcFromZ && pos < srcToZ) {
                                // inside the ChunkSection of interest
                                position = position.a(deltaX, deltaY, deltaZ);
//                                this.plugin.getLogger().info("Moví entry " + Block.REGISTRY.b(entry.a()) + ": " + entry);
                            }
                        }
                    }
                    world.b(position, entry.a(), (int) (entry.b - currentTime), entry.c);
                }

                List<NextTickListEntry> entries2 = world.a(chunk, false);
                if (entries2 != null ) {
                    if (entries.size() != entries2.size()) {
                        this.plugin.getLogger().info("Listando TickEntries del chunk " + chunk.k() + " con " + entries2.size() + " entries");
                        this.plugin.getLogger().info("Perdí entries! (antes " + entries.size() + ")");
                        for (NextTickListEntry entry : entries2) {
                            this.plugin.getLogger().info("Nueva entry " + Block.REGISTRY.b(entry.a()) + ": " + entry);
                        }
                    }
                } else {
                    this.plugin.getLogger().info("Chunk " + chunk.k() + " AHORA sin TickEntries");
                }
            } else {
                this.plugin.getLogger().info("Chunk " + chunk.k() + " sin TickEntries");
            }

            chunk.e();
        }

        long midTime = System.nanoTime();
        this.plugin.getLogger().info("Tiempo movimiento:   " + (midTime - startTime) + " ns");

        for (Chunk chunk : affectedChunks) {
            PacketPlayOutMapChunk pmc = new PacketPlayOutMapChunk(chunk, 1 << chunkY);
            player.getHandle().playerConnection.sendPacket(pmc);
        }

        this.posX += deltaX;
        this.posY += deltaY;
        this.posZ += deltaZ;

        long endTime = System.nanoTime();
        this.plugin.getLogger().info("Tiempo comunicación: " + (endTime - midTime) + " ns");
    }
}
