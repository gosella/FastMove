package io.github.gosella.fastmove;

import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
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

    public MoveManager(Main plugin, CraftPlayer player, World world, int posX, int posY, int posZ) {
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

    private int calculateSegmentsForDecreasingMove(int firstChunk,
                                                   int srcFrom, final int srcTo, final byte srcSegments[],
                                                   int dstFrom, final int dstTo, final byte dstSegments[]) {
        int p = 0;
        while (srcFrom < srcTo) {
            final byte srcLen = (byte) Math.min(((srcFrom + 16) & ~15) - srcFrom, srcTo - srcFrom);
            final byte dstLen = (byte) Math.min(((dstFrom + 16) & ~15) - dstFrom, dstTo - dstFrom);
            final byte minLen = (byte) Math.min(srcLen, dstLen);

            final byte srcChunkIndex = (byte) ((srcFrom >> 4) - firstChunk);
            final byte dstChunkIndex = (byte) ((dstFrom >> 4) - firstChunk);
            srcSegments[p] = srcChunkIndex;
            dstSegments[p] = dstChunkIndex;
            ++p;

            final byte srcBeginPos = (byte) (srcFrom & 15);
            final byte dstBeginPos = (byte) (dstFrom & 15);
            srcSegments[p] = srcBeginPos;
            dstSegments[p] = dstBeginPos;
            ++p;

            final byte srcEndPos = (byte) (srcBeginPos + minLen);
            final byte dstEndPos = (byte) (dstBeginPos + minLen);
            srcSegments[p] = srcEndPos;
            dstSegments[p] = dstEndPos;
            ++p;

            srcFrom += minLen;
            dstFrom += minLen;
        }

        return p;
    }

    private final Comparator<TileEntity> tileEntityComparatorXZY = (TileEntity t1, TileEntity t2) -> {
        final BlockPosition p1 = t1.getPosition();
        final BlockPosition p2 = t2.getPosition();
        if (p1.getX() < p2.getX()) {
            return -1;
        }
        if (p1.getX() > p2.getX()) {
            return 1;
        }
        // p1.getX() == t2.getX()
        if (p1.getZ() < p2.getZ()) {
            return -1;
        }
        if (p1.getZ() > p2.getZ()) {
            return 1;
        }
        // p1.getZ() == p2.getZ()
        if (p1.getY() < p2.getY()) {
            return -1;
        }
        if (p1.getY() > p2.getY()) {
            return 1;
        }
        // p1.getY() == t2.getY()
        return 0;
    };

    private final Comparator<Entity> entityComparatorXZY = (Entity t1, Entity t2) -> {
        if (t1.getX() < t2.getX()) {
            return -1;
        }
        if (t1.getX() > t2.getX()) {
            return 1;
        }
        // p1.getX() == t2.getX()
        if (t1.getZ() < t2.getZ()) {
            return -1;
        }
        if (t1.getZ() > t2.getZ()) {
            return 1;
        }
        // p1.getZ() == p2.getZ()
        if (t1.getY() < t2.getY()) {
            return -1;
        }
        if (t1.getY() > t2.getY()) {
            return 1;
        }
        // p1.getY() == t2.getY()
        return 0;
    };

    public void run() {
        if (--count < 0) {
            this.plugin.getServer().broadcastMessage("Movimiento terminado!");
            this.cancel();
            return;
        }

        long startTime = System.nanoTime();

        IChunkProvider chunkProvider = world.getChunkProvider();


        int lenX = 32; // 16 * 9;
        int lenY = 16; // 16 * 3;
        int lenZ = 16; // 16 * 3;

        int deltaX = -5;
        int deltaY = 0;
        int deltaZ = 0;

        ///////////////////////////////////////
        // From where do we move the things? //
        ///////////////////////////////////////

        final int srcFromX = this.posX;
        final int srcToX = srcFromX + lenX;
        final int srcFromZ = this.posZ;
        final int srcToZ = srcFromZ + lenZ;
        final int srcFromY = this.posY;
        final int srcToY = srcFromY + lenY;


        final int srcFromChunkX = srcFromX >> 4;
        final int srcToChunkX = (srcToX >> 4) + 1;
        final int srcFromChunkZ = srcFromZ >> 4;
        final int srcToChunkZ = (srcToZ >> 4) + 1;
        final int srcFromSectionY = srcFromY >> 4;
        final int srcToSectionY = (srcToY >> 4) + 1;

        /////////////////////////////////////
        // To where do we move the things? //
        /////////////////////////////////////

        final int dstFromX = srcFromX + deltaX;
        final int dstToX = srcToX + deltaX;
        final int dstFromZ = srcFromZ + deltaZ;
        final int dstToZ = srcToZ + deltaZ;
        final int dstFromY = srcFromY + deltaY;
        final int dstToY = srcToY + deltaY;

        final int dstFromChunkX = dstFromX >> 4;
        final int dstToChunkX = (dstToX >> 4) + 1;
        final int dstFromChunkZ = dstFromZ >> 4;
        final int dstToChunkZ = (dstToZ >> 4) + 1;
        final int dstFromSectionY = dstFromY >> 4;
        final int dstToSectionY = (dstToY >> 4) + 1;

//        this.plugin.getLogger().info("Moviendo chunks " +
//                "desde (" + srcFromX + ", " + srcFromY + ", " + srcFromZ + ") " +
//                "hasta (" + dstFromX + ", " + dstFromY + ", " + dstFromZ + ")");

        /////////////////////////////////////
        //                                 //
        /////////////////////////////////////

        final int firstChunkX = Math.min(srcFromChunkX, dstFromChunkX);
        final int firstChunkZ = Math.min(srcFromChunkZ, dstFromChunkZ);
        final int firstSectionY = Math.min(srcFromSectionY, dstFromSectionY);

        final int lastChunkX = Math.max(srcToChunkX, dstToChunkX);
        final int lastChunkZ = Math.max(srcToChunkZ, dstToChunkZ);
        final int lastSectionY = Math.max(srcToSectionY, dstToSectionY);

        final int chunkCountX = lastChunkX - firstChunkX;
        final int chunkCountZ = lastChunkZ - firstChunkZ;
        final int sectionCountY = lastSectionY - firstSectionY;

        final Chunk chunks[] = new Chunk[chunkCountX * chunkCountZ];
        for (int p = 0, z = firstChunkZ; z < lastChunkZ; z++) {
            for (int x = firstChunkX; x < lastChunkX; x++) {
                chunks[p++] = chunkProvider.getChunkAt(x, z);
            }
        }

        final short dirtySections[] = new short[chunkCountX * chunkCountZ];


        //////////////////////////////////////////////////////////////////////////
        // Divide the chunks in overlapping segments to move blocks efficiently //
        //////////////////////////////////////////////////////////////////////////

        final int maxChunkSegmentsX = 2 * chunkCountX;
        final byte srcSegmentsX[] = new byte[3 * maxChunkSegmentsX]; // [Chunk index, Begin pos, End pos] * Segments
        final byte dstSegmentsX[] = new byte[3 * maxChunkSegmentsX]; // [Chunk index, Begin pos, End pos] * Segments
        final int lastChunkSegmentsXPos = calculateSegmentsForDecreasingMove(firstChunkX,
                srcFromX, srcToX, srcSegmentsX, dstFromX, dstToX, dstSegmentsX);

//        this.plugin.getLogger().info("lastChunkSegmentsXPos=" + lastChunkSegmentsXPos +
//                " - srcSegmentsX: " + Arrays.toString(srcSegmentsX) +
//                " - dstSegmentsX: " + Arrays.toString(dstSegmentsX));

        final int maxChunkSegmentsZ = 2 * chunkCountZ;
        final byte srcSegmentsZ[] = new byte[3 * maxChunkSegmentsZ]; // [Chunk index, Begin pos, End pos] * Segments
        final byte dstSegmentsZ[] = new byte[3 * maxChunkSegmentsZ]; // [Chunk index, Begin pos, End pos] * Segments
        final int lastChunkSegmentsZPos = calculateSegmentsForDecreasingMove(firstChunkZ,
                srcFromZ, srcToZ, srcSegmentsZ, dstFromZ, dstToZ, dstSegmentsZ);

//        this.plugin.getLogger().info("lastChunkSegmentsZPos=" + lastChunkSegmentsZPos +
//                " - srcSegmentsZ: " + Arrays.toString(srcSegmentsZ) +
//                " - dstSegmentsZ: " + Arrays.toString(dstSegmentsZ));

        final int maxSectionSegmentsY = 2 * sectionCountY;
        final byte srcSegmentsY[] = new byte[3 * maxSectionSegmentsY]; // [Chunk index, Begin pos, End pos] * Segments
        final byte dstSegmentsY[] = new byte[3 * maxSectionSegmentsY]; // [Chunk index, Begin pos, End pos] * Segments
        final int lastSectionSegmentsYPos = calculateSegmentsForDecreasingMove(0,
                srcFromY, srcToY, srcSegmentsY, dstFromY, dstToY, dstSegmentsY);

//        this.plugin.getLogger().info("lastSectionSegmentsYPos=" + lastSectionSegmentsYPos +
//                " - srcSegmentsY: " + Arrays.toString(srcSegmentsY) +
//                " - dstSegmentsY: " + Arrays.toString(dstSegmentsY));

        /////////////////////////////////////
        //                                 //
        /////////////////////////////////////

        // TODO: Reemplazar a affectedPlayers por la lista de players alrededor de la nave.
        final List<EntityPlayer> affectedPlayers = new ArrayList<EntityPlayer>(8); // Why not?

        // La lista de los bloques a los que hay que activar en el próximo tick sólo se encuentra
        // dentro del world, por lo que no es necesario interactuar con el chunk para actualizarla.
        // Pero es necesario capturar la lista en este punto dado que al mover las TileEntities,
        // sus NextTickListEntries serán borradas y no estarán disponibles para luego poder moverlas.

        StructureBoundingBox srcBoundingBox = new StructureBoundingBox(srcFromX, srcFromY, srcFromZ, srcToX, srcToY, srcToZ);
        List<NextTickListEntry> entries = world.a(srcBoundingBox, true);

//        IBlockData AIR = Blocks.AIR.getBlockData();

        //////////////////////////////////////////////////////////////////////////////////
        // Mueve bloques desde cada chunk de origen al correspondiente chunk de destino //
        //////////////////////////////////////////////////////////////////////////////////

        IBlockData b;

        for (int px = 0; px < lastChunkSegmentsXPos; ) {
            final int srcChunkIndexX = srcSegmentsX[px];
            final int dstChunkIndexX = dstSegmentsX[px];
            ++px;
            final int srcBeginX = srcSegmentsX[px];
            final int dstBeginX = dstSegmentsX[px];
            ++px;
            final int srcEndX = srcSegmentsX[px];
            final int dstEndX = dstSegmentsX[px];
            ++px;

            for (int pz = 0; pz < lastChunkSegmentsZPos; ) {
                final int srcChunkIndexZ = srcSegmentsZ[pz];
                final int dstChunkIndexZ = dstSegmentsZ[pz];
                ++pz;
                final int srcBeginZ = srcSegmentsZ[pz];
                final int dstBeginZ = dstSegmentsZ[pz];
                ++pz;
                final int srcEndZ = srcSegmentsZ[pz];
                final int dstEndZ = dstSegmentsZ[pz];
                ++pz;

                final Chunk srcChunk = chunks[srcChunkIndexX + srcChunkIndexZ * chunkCountX];
                final Chunk dstChunk = chunks[dstChunkIndexX + dstChunkIndexZ * chunkCountX];

//                affectedChunks.add(dstChunk);
//                affectedChunks.add(srcChunk);

                for (int py = 0; py < lastSectionSegmentsYPos; ) {
                    final int srcSectionY = srcSegmentsY[py];
                    final int dstSectionY = dstSegmentsY[py];
                    ++py;
                    final int srcBeginY = srcSegmentsY[py];
                    final int dstBeginY = dstSegmentsY[py];
                    ++py;
                    final int srcEndY = srcSegmentsY[py];
                    final int dstEndY = dstSegmentsY[py];
                    ++py;

                    final ChunkSection srcSection = this.getSection(srcChunk, srcSectionY);
                    final ChunkSection dstSection = this.getSection(dstChunk, dstSectionY);

                    // Mueve los bloques desde el chunk de origen al chunk de destino.

//                    this.plugin.getLogger().info("Moviendo " +
//                            "[" + srcChunk.locX + ":" + srcSectionY + ":" + srcChunk.locZ + "] " +
//                            "(" + srcBeginX + ", " + srcBeginZ + ", " + srcBeginY +
//                            ") - ( " + srcEndX + ", " + srcEndZ + ", " + srcEndY + ") hasta " +
//                            "[" + dstChunk.locX + ":" + dstSectionY + ":" + dstChunk.locZ + "] " +
//                            "(" + dstBeginX + ", " + dstBeginZ + ", " + dstBeginY +
//                            ") - ( " + dstEndX + ", " + dstEndZ + ", " + dstEndY + ").");

                    boolean modified = false;
                    for (int syy = srcBeginY, dyy = dstBeginY; syy < srcEndY; ++syy, ++dyy) {
                        for (int szz = srcBeginZ, dzz = dstBeginZ; szz < srcEndZ; ++szz, ++dzz) {
                            for (int sxx = srcBeginX, dxx = dstBeginX; sxx < srcEndX; ++sxx, ++dxx) {
                                // TODO: Hacer esto sólo si hay que mover el bloque en:
                                // (srcChunkIndexX << 4) + sxx, (srcChunkIndexZ << 4) + szz, (srcSectionY) << 4 + syy
                                modified = true;

                                // Copy Block Type and Data
                                b = srcSection.getType(sxx, syy, szz);
                                dstSection.setType(dxx, dyy, dzz, b);
                                // Copy Emitted Light
                                dstSection.b(dxx, dyy, dzz, srcSection.c(sxx, syy, szz));

                                // TODO: eliminar esta comparación haciendo otro loop
                                if (!this.world.worldProvider.m()) {
                                    // Copy Sky Light
                                    dstSection.a(dxx, dyy, dzz, srcSection.b(sxx, syy, szz));
                                }
                            }
                        }
                    }

                    if (modified) {
                        dirtySections[srcChunkIndexX + srcChunkIndexZ * chunkCountX] |= (1 << srcSectionY);
                        dirtySections[dstChunkIndexX + dstChunkIndexZ * chunkCountX] |= (1 << dstSectionY);
                    }
                }
            }
        }

//        this.plugin.getLogger().info("dirtySections: " + Arrays.toString(dirtySections));

        // worldserver.b contiene una lista de todas las TileEntities cargadas en el mundo.
        // chunk.tileEntities contiene solamente las entidades del chunk.
        // Afortunadamente, ambas contienen los mismos objetos, por lo que alcanza con
        // modificar únicamente las TilEntities del chunk.

        // Desafortunadamente, en world hay otras listas que asocian una TileEntity con su posición.
        // Esta forma de moverlas no las actualiza... :-(  ¿O quizás si? TODO: Investigar esto un poco más...


//        this.plugin.getLogger().info("Viendo TileEntries entre " + srcBeginX + " y " + srcEndX);
//        this.plugin.getLogger().info("Antes:");
//        for (Map.Entry<BlockPosition, TileEntity> entry : srcChunk.tileEntities.entrySet()) {
//            this.plugin.getLogger().info("TileEntry " + entry.getValue() + " en " + entry.getKey());
//        }

        final Queue<TileEntity> tileEntitiesToMove = new PriorityQueue<TileEntity>(tileEntityComparatorXZY);

        for (int chunkXIndex = srcFromChunkX - firstChunkX; chunkXIndex < srcToChunkX - firstChunkX; ++chunkXIndex) {
            for (int chunkZIndex = srcFromChunkZ - firstChunkZ; chunkZIndex < srcToChunkZ - firstChunkZ; ++chunkZIndex) {
                tileEntitiesToMove.clear();

                final Chunk srcChunk = chunks[chunkXIndex + chunkZIndex * chunkCountX];
                for (Iterator<Map.Entry<BlockPosition, TileEntity>> iterator = srcChunk.getTileEntities().entrySet().iterator();
                     iterator.hasNext(); ) {
                    Map.Entry<BlockPosition, TileEntity> entry = iterator.next();
                    final BlockPosition position = entry.getKey();
//                    this.plugin.getLogger().info("Considerando TileEntry " + entry.getValue() + " en " + position);
                    if (position.getX() < srcFromX || position.getX() >= srcToX)
                        continue;
                    if (position.getY() < srcFromY || position.getY() >= srcToY)
                        continue;
                    if (position.getZ() < srcFromZ || position.getZ() >= srcToZ)
                        continue;
                    // TODO: Hacer esto sólo si hay que mover el bloque en:
                    //    (positionX - srcFromX, positionY - srcFromY, positionZ - srcFromZ)
                    tileEntitiesToMove.add(entry.getValue());
                    iterator.remove();
                }

//                this.plugin.getLogger().info("Moviendo " + tileEntities.size() + " TileEntries:");
                TileEntity tileEntity;
                while ((tileEntity = tileEntitiesToMove.poll()) != null) {
                    BlockPosition position = tileEntity.getPosition();
                    BlockPosition newPosition = position.a(deltaX, deltaY, deltaZ);
                    tileEntity.setPosition(newPosition);
                    final int dstChunkIndex = ((newPosition.getX() >> 4) - firstChunkX) + ((newPosition.getZ() >> 4) - firstChunkZ) * chunkCountX;
                    chunks[dstChunkIndex].getTileEntities().put(newPosition, tileEntity);
//                    this.plugin.getLogger().info("\tMoví TileEntry " + tileEntity + " de " + position + " a " + newPosition);
                    if (world.capturedTileEntities.containsKey(position)) {
                        this.plugin.getLogger().info("\t\tEncontré un TileEntry capturado!!!!!!! " + tileEntity);
                        world.capturedTileEntities.remove(position);
                        world.capturedTileEntities.put(newPosition, tileEntity);
                    }
                }

//                this.plugin.getLogger().info("Después:");
//                for (Map.Entry<BlockPosition, TileEntity> entry : srcChunk.getTileEntities().entrySet()) {
//                    this.plugin.getLogger().info("TileEntry " + entry.getValue() + " en " + entry.getKey());
//                }

                // world.entityList contiene una lista de todas las entidades cargadas en el mundo.
                // chunk.getEntitySlices() contiene listas con las entidades del chunk en cada slice.
                // Afortunadamente, ambas listas contienen los mismos objetos, por lo que alcanza con
                // modificar únicamente las entities del chunk.

                final List<Entity>[] srcEntitySlices = srcChunk.getEntitySlices();
                final Queue<Entity> entitiesToMove = new PriorityQueue<Entity>(entityComparatorXZY);

                for (int sectionY = srcFromSectionY; sectionY < srcToSectionY; ++sectionY) {
                    final List<Entity> srcEntitySlice = srcEntitySlices[sectionY];
                    entitiesToMove.clear();
                    entitiesToMove.addAll(srcEntitySlice);

                    Entity entity;
                    while ((entity = entitiesToMove.poll()) != null) {
                        final int positionX = MathHelper.floor(entity.getX());
                        if (positionX < srcFromX || positionX >= srcToX) {
                            continue;
                        }
                        final int positionY = MathHelper.floor(entity.getY());
                        if (positionY < srcFromY || positionY >= srcToY) {
                            // TODO: Add 1 above the BBox for entities on the roof?
                            continue;
                        }
                        final int positionZ = MathHelper.floor(entity.getZ());
                        if (positionZ < srcFromZ || positionZ >= srcToZ) {
                            continue;
                        }

                        // TODO: Hacer esto sólo si hay que mover el bloque debajo de la entity:
                        //    (positionX - srcFromX, positionY - srcFromY - 1, positionZ - srcFromZ)

                        entity.setPositionRotation(entity.locX + deltaX, entity.locY + deltaY, entity.locZ + deltaZ, entity.yaw, entity.pitch);

                        final int dstChunkX = (MathHelper.floor(entity.locX) >> 4) - firstChunkX;
                        final int dstChunkZ = (MathHelper.floor(entity.locZ) >> 4) - firstChunkZ;
                        final int dstSectionY = (MathHelper.floor(entity.locY) >> 4) - firstSectionY;

                        final int dstChunkIndex = dstChunkX + dstChunkZ * chunkCountX;
                        final Chunk dstChunk = chunks[dstChunkIndex];

                        if (srcChunk != dstChunk || sectionY != dstSectionY) {
                            srcChunk.a(entity, sectionY);
                            dstChunk.a(entity);
                        }

                        if (entity instanceof EntityPlayer) {
                            Location location = new Location(null, entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
                            ((EntityPlayer) entity).playerConnection.teleport(location);
                            affectedPlayers.add((EntityPlayer) entity);
                        }
                    }
                }
            }
        }


        for (int p = 0; p < chunks.length; ++p) {
            if (dirtySections[p] != 0) {
                int bits = dirtySections[p];
                for (int y = 0; bits != 0; ++y, bits >>>= 1) {
                    if ((bits & 1) == 1) {
                        chunks[p].getSections()[y].recalcBlockCounts();
                    }
                }
            }
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

        this.posX += deltaX;
        this.posY += deltaY;
        this.posZ += deltaZ;

        long midTime = System.nanoTime();
        this.plugin.getLogger().info("Tiempo movimiento:   " + (midTime - startTime) + " ns");

        if (!affectedPlayers.contains(player.getHandle())) {
            affectedPlayers.add(player.getHandle());
        }

        for (EntityPlayer player : affectedPlayers) {
            for (int p = 0; p < chunks.length; ++p) {
                if (dirtySections[p] != 0) {
                    chunks[p].e();
                    PacketPlayOutMapChunk pmc = new PacketPlayOutMapChunk(chunks[p], dirtySections[p]);
                    player.playerConnection.sendPacket(pmc);
                }
            }
        }

        long endTime = System.nanoTime();
        this.plugin.getLogger().info("Tiempo comunicación: " + (endTime - midTime) + " ns");
    }
}
