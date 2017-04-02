package io.github.gosella.fastmove;

import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Location;
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
            this.plugin.getServer().broadcastMessage("Movimiento terminado!");
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


        int srcFromX = this.posX;
        int srcToX = srcFromX + lenX;

        int srcFromY = this.posY;
        int srcToY = srcFromY + lenY;

        int srcFromZ = this.posZ;
        int srcToZ = srcFromZ + lenZ;

        int dstFromX = srcFromX + deltaX;
        int dstToX = srcToX + deltaX;

        int dstFromY = srcFromY + deltaY;
        int dstToY = srcToY + deltaY;

        int dstFromZ = srcFromZ + deltaZ;
        int dstToZ = srcToZ + deltaZ;

        this.plugin.getLogger().info("Moviendo chunk [" + chunkX + ", " + chunkY + ", " + chunkZ + "] " +
                "desde (" + srcFromX + ", " + srcFromY + ", " + srcFromZ + ") " +
                "hasta (" + dstFromX + ", " + dstFromY + ", " + dstFromZ + ")");

        final Set<Chunk> affectedChunks = new LinkedHashSet<Chunk>(3);  // TODO: Review the initial capacities
        final Set<ChunkSection> affectedSections = new HashSet<ChunkSection>(3);
        final List<EntityPlayer> affectedPlayers = new ArrayList<EntityPlayer>(8);

        // La lista de los bloques a los que hay que activar en el próximo tick sólo se encuentra
        // dentro del world, por lo que no es necesario interactuar con el chunk para actualizarla.

        // Es necesario capturar la lista en este punto dado que al mover las TileEntities, sus
        // NextTickListEntries serán borradas y no estarán disponibles para moverlas más luego.

        StructureBoundingBox srcBoundingBox = new StructureBoundingBox(srcFromX, srcFromY, srcFromZ, srcToX, srcToY, srcToZ);
        List<NextTickListEntry> entries = world.a(srcBoundingBox, true);


        // Hace el movimiento desde cada chunk de origen al chunk de destino correspondiente.

        IChunkProvider chunkProvider = world.getChunkProvider();
//        IBlockData AIR = Blocks.AIR.getBlockData();
        IBlockData b;

        final Queue<TileEntity> tileEntities = new PriorityQueue<TileEntity>();

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

            // Mueve los bloques desde el chunk de origen al chunk de destino.

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
            // Desafortunadamente, en world hay otras listas que asocian una TileEntity con su posición.
            // Esta forma de moverlas no las actualiza... :-(

            this.plugin.getLogger().info("Viendo TileEntries entre " + srcBeginX + " y " + srcEndX);
            this.plugin.getLogger().info("Antes:");
            for (Map.Entry<BlockPosition, TileEntity> entry : srcChunk.tileEntities.entrySet()) {
                this.plugin.getLogger().info("TileEntry " + entry.getValue() + " en " + entry.getKey());
            }

            tileEntities.clear();
            for (Map.Entry<BlockPosition, TileEntity> entry : srcChunk.tileEntities.entrySet()) {
                final BlockPosition position = entry.getKey();
//                this.plugin.getLogger().info("Considerando TileEntry " + entry.getValue() + " en " + position);
                final int positionX = position.getX() & 15;
                if (positionX < srcBeginX || positionX >= srcEndX) {
                    continue;
                }
                final int positionY = position.getY();
                if (positionY < srcFromY || positionY >= srcToY) {
                    continue;
                }
                final int positionZ = position.getZ();
                if (positionZ < srcFromZ || positionZ >= srcToZ) {
                    continue;
                }
                tileEntities.add(entry.getValue());
            }

//            this.plugin.getLogger().info("Moviendo " + tileEntities.size() + " TileEntries:");
            for (TileEntity tileEntity : tileEntities) {
                BlockPosition position = tileEntity.getPosition();
                srcChunk.tileEntities.remove(position);
                BlockPosition newPosition = position.a(deltaX, deltaY, deltaZ);
                tileEntity.setPosition(newPosition);
                dstChunk.tileEntities.put(newPosition, tileEntity);
//                this.plugin.getLogger().info("\tMoví TileEntry " + tileEntity + " de " + position + " a " + newPosition);
                if (world.capturedTileEntities.containsKey(position)) {
                    this.plugin.getLogger().info("\t\tEncontré un TileEntry capturado!!!!!!! " + tileEntity);
                    world.capturedTileEntities.remove(position);
                    world.capturedTileEntities.put(newPosition, tileEntity);
                }
            }

            this.plugin.getLogger().info("Después:");
            for (Map.Entry<BlockPosition, TileEntity> entry : srcChunk.tileEntities.entrySet()) {
                this.plugin.getLogger().info("TileEntry " + entry.getValue() + " en " + entry.getKey());
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
                if (positionY < srcFromY || positionY > srcToY) { // Add 1 above the BBox for entities on the roof
                    continue;
                }
                final int positionZ = MathHelper.floor(entity.getZ());
                if (positionZ < srcFromZ || positionZ >= srcToZ) {
                    continue;
                }
                entity.setPositionRotation(entity.locX + deltaX, entity.locY + deltaY, entity.locZ + deltaZ, entity.yaw, entity.pitch);
                if (srcChunk != dstChunk) {
                    srcChunk.a(entity, chunkY);
                    dstChunk.a(entity);
                }
                if (entity instanceof EntityPlayer) {
                    Location location = new Location(null, entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
                    ((EntityPlayer) entity).playerConnection.teleport(location);
                    affectedPlayers.add((EntityPlayer) entity);
                }
            }

            srcX += minLen;
            dstX += minLen;
        }

        for (ChunkSection section : affectedSections) {
            section.recalcBlockCounts();
        }


        if (entries != null) {
            final long currentTime = this.world.worldData.getTime();
//            this.plugin.getLogger().info("Analizando " + entries.size() + " NextTickEntries");
            for (NextTickListEntry entry : entries) {
                BlockPosition position = entry.a;
//                this.plugin.getLogger().info("Considerando entry " + Block.REGISTRY.b(entry.a()) + ": " + entry);
                int pos = position.getX();
                if (pos >= srcFromX && pos < srcToX) {
                    pos = position.getY();
                    if (pos >= srcFromY && pos < srcToY) {
                        pos = position.getZ();
                        if (pos >= srcFromZ && pos < srcToZ) {
                            // inside the ChunkSection of interest
                            position = position.a(deltaX, deltaY, deltaZ);
//                            this.plugin.getLogger().info("Moví entry " + Block.REGISTRY.b(entry.a()) + ": " + entry);
                        }
                    }
                }
                world.b(position, entry.a(), (int) (entry.b - currentTime), entry.c);
            }

//            StructureBoundingBox dstBoundingBox = new StructureBoundingBox(dstFromX, dstFromY, dstFromZ, dstToX, dstToY, dstToZ);
//            List<NextTickListEntry> entries2 = world.a(dstBoundingBox, false);
//            if (entries2 != null ) {
//                if (entries.size() != entries2.size()) {
//                    this.plugin.getLogger().info("Listando " + entries2.size() + " NextTickEntries");
//                    this.plugin.getLogger().info("Perdí entries! (antes " + entries.size() + ")");
//                    for (NextTickListEntry entry : entries2) {
//                        this.plugin.getLogger().info("Nueva entry " + Block.REGISTRY.b(entry.a()) + ": " + entry);
//                    }
//                }
//            } else {
//                this.plugin.getLogger().info("AHORA no tiene NextTickEntries!!!");
//            }
//        } else {
//            this.plugin.getLogger().info("Área sin NextTickEntries");
        }

        long midTime = System.nanoTime();
//        this.plugin.getLogger().info("Tiempo movimiento:   " + (midTime - startTime) + " ns");

        if (!affectedPlayers.contains(player.getHandle())) {
            affectedPlayers.add(player.getHandle());
        }

        for (EntityPlayer player : affectedPlayers) {
            for (Chunk chunk : affectedChunks) {
                chunk.e();
                PacketPlayOutMapChunk pmc = new PacketPlayOutMapChunk(chunk, 1 << chunkY);
                player.playerConnection.sendPacket(pmc);
            }

//            EnumSet updatePositionOnly = EnumSet.of(
//                    PacketPlayOutPosition.EnumPlayerTeleportFlags.X,
//                    PacketPlayOutPosition.EnumPlayerTeleportFlags.Y,
//                    PacketPlayOutPosition.EnumPlayerTeleportFlags.Z
//            );
//
//            PacketPlayOutPosition ppos = new PacketPlayOutPosition(player.locX, player.locY, player.locZ, 0, 0, updatePositionOnly, 0);
//            player.playerConnection.sendPacket(ppos);
        }

        this.posX += deltaX;
        this.posY += deltaY;
        this.posZ += deltaZ;

        long endTime = System.nanoTime();
//        this.plugin.getLogger().info("Tiempo comunicación: " + (endTime - midTime) + " ns");
    }
}
