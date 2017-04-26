package io.github.gosella.fastmove;

import net.minecraft.server.v1_10_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("do")) {
            getLogger().info("do command executed!");

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only a Player can do that.");
                return false;
            }

            CraftPlayer player = (CraftPlayer) sender;

//            new MoveManager(
//                    this,
//                    player,
//                    ((CraftWorld) player.getWorld()).getHandle(),
////                    player.getLocation().getBlockX() & ~15
//                    player.getLocation().getBlockX() - 1,
//                    player.getLocation().getBlockY() & ~15,
////                    player.getLocation().getBlockY() - 1,
//                    player.getLocation().getBlockZ() & ~15
////                    player.getLocation().getBlockZ() - 1
//            ).runTaskTimer(this, 40, 30);
////            ).runTask(this);


            Block block = player.getTargetBlock((Set<Material>) null, 10);
            sender.sendMessage(ChatColor.AQUA + "Block found: " + block);

            int blockX = block.getX();
            int blockY = block.getY();
            int blockZ = block.getZ();

            int chunkX = blockX >> 4;
            int chunkY = blockY >> 4;
            int chunkZ = blockZ >> 4;

            WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
            Chunk chunk = world.getChunkProvider().getChunkAt(chunkX, chunkZ);
            ChunkSection section = chunk.getSections()[chunkY];

            int offsetX = blockX & 15;
            int offsetY = blockY & 15;
            int offsetZ = blockZ & 15;

            IBlockData b = section.getType(offsetX, offsetY, offsetZ);
            b = b.a(EnumBlockRotation.CLOCKWISE_90);
            section.setType(offsetX, offsetY, offsetZ, b);

            Map<BlockPosition, TileEntity> tileEntities = chunk.getTileEntities();
            BlockPosition position = new BlockPosition(blockX, blockY, blockZ);
            TileEntity tileEntity = tileEntities.get(position);
            if (tileEntity instanceof TileEntitySign) {
                TileEntitySign sign = (TileEntitySign) tileEntity;
                sign.lines[0] = new ChatComponentText("Hola!");
                sign.lines[2] = new ChatComponentText("T:" + world.worldData.getTime());
            }

            PacketPlayOutMapChunk pmc = new PacketPlayOutMapChunk(chunk, 1 << chunkY);
            player.getHandle().playerConnection.sendPacket(pmc);


//            IBlockData newBlock = net.minecraft.server.v1_11_R1.Block.getByCombinedId(7); // minecraft:bedrock
//            IBlockData newBlock = net.minecraft.server.v1_11_R1.Block.getByCombinedId(35 | 0xb000); // minecraft:blue_wool

//            WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
//            Chunk chunk = world.getChunkProvider().getChunkAt(chunkX, chunkZ);
//            BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());

//            PacketPlayOutBlockChange change = new PacketPlayOutBlockChange(world, pos);
//            change.block = world.getChunkProvider().getChunkAt(block.getX() >> 4, block.getZ() >> 4).getBlockData(block.getX(), block.getY(), block.getZ());
//            change.block = net.minecraft.server.v1_11_R1.Block.getByCombinedId(55 | (15 << 12));
//            change.block = newBlock;
//            sender.sendMessage(ChatColor.AQUA + "Block changed to: " + change.block);

//            int playerX = block.getX();
//            int playerY = block.getY() + 4;
//            int playerZ = block.getZ();
//
//            sender.sendMessage(ChatColor.AQUA + "Placing blocks at @ (" + playerX + ", " + playerY + ", " + playerZ + ")");

//            for (Entity entity : world.entityList) {
//                getLogger().info("World Entity: " + entity + " - " + entity.locX + " -> " + entity.getChunkX());
//            }

//            Location location = player.getLocation();
//            int playerX = location.getBlockX();
//            int playerY = location.getBlockY();
//            int playerZ = location.getBlockZ();

//            posX = location.getBlockX() - (lenX / 2);
//            posY = location.getBlockY() - (lenY / 2);
//            posZ = location.getBlockZ() - (lenZ / 2);

//            int chunkX = playerX >> 4;
//            int chunkY = playerY >> 4;
//            int chunkZ = playerZ >> 4;
//
//            Chunk chunk = world.getChunkProvider().getChunkAt(chunkX, chunkZ);
//            getLogger().info("Chunk: " + chunkX + ", " + chunkY + ", " + chunkZ);
//            getLogger().info("HeightMap: " + Arrays.toString(chunk.heightMap));
//
//            ChunkSection section = chunk.getSections()[chunkY];
//            getLogger().info("EmittedLight: " + Arrays.toString(section.getEmittedLightArray().asBytes()));
//            getLogger().info("SkyLight: " + Arrays.toString(section.getSkyLightArray().asBytes()));


//            List<Entity>[] entitySlices = chunk.getEntitySlices();
//            List<Entity> entitySlice = entitySlices[chunkY];
//            for (Entity entity : entitySlice) {
//                getLogger().info("Chunk Entity: " + entity + " - " + entity.locX + " -> " + world.entityList.contains(entity));
//
//            }


//            List<TileEntity> tileEntities = new ArrayList<TileEntity>(chunk.tileEntities.values());
//            for (TileEntity tileEntity : tileEntities) {
//                TileEntity otherTileEntity = world.getTileEntity(tileEntity.getPosition());
//                getLogger().info("Chunk TileEntity: " + tileEntity + " - > " + (otherTileEntity == tileEntity));
//            }

//            DataPaletteBlock blocks = section.getBlocks();
//
//            short[] coords = new short[6];
//
//            playerX &= 15;
//            playerZ &= 15;
//            int yy = playerY & 15;
//            blocks.setBlock(playerX, yy, playerZ, newBlock);
//            coords[0] = (short)(playerX << 12 | playerZ << 8 | playerY);
//
//            playerX -= 1;
//            blocks.setBlock(playerX, yy, playerZ, newBlock);
//            coords[1] = (short)(playerX << 12 | playerZ << 8 | playerY);
//
//            playerX += 2;
//            blocks.setBlock(playerX, yy, playerZ, newBlock);
//            coords[2] = (short)(playerX << 12 | playerZ << 8 | playerY);
//
//            playerX -= 1;
//            playerZ -= 1;
//            blocks.setBlock(playerX, yy, playerZ, newBlock);
//            coords[3] = (short)(playerX << 12 | playerZ << 8 | playerY);
//
//            playerZ += 2;
//            blocks.setBlock(playerX, yy, playerZ, newBlock);
//            coords[4] = (short)(playerX << 12 | playerZ << 8 | playerY);
//
//            playerZ -= 1;
//            playerY += 1;
//            blocks.setBlock(playerX, yy+1, playerZ, newBlock);
//            coords[5] = (short)(playerX << 12 | playerZ << 8 | playerY);
//
//            PacketPlayOutMultiBlockChange changes = new PacketPlayOutMultiBlockChange(coords.length, coords, chunk);
//
//            CraftPlayer p = (CraftPlayer) player;
//            p.getHandle().playerConnection.sendPacket(change);
//            p.getHandle().playerConnection.sendPacket(changes);
            sender.sendMessage(ChatColor.GOLD + "Done!");

            return true;
        } else if (label.equalsIgnoreCase("box")) {
            WorldServer world = ((CraftWorld) getServer().getWorld("world")).getHandle();
            IBlockData bedrock = net.minecraft.server.v1_10_R1.Block.getByCombinedId(7);// minecraft:bedrock
            Chunk chunk = world.getChunkProvider().getChunkAt(0, 0);
            ChunkSection section = chunk.getSections()[4];
            DataPaletteBlock blocks = section.getBlocks();

            int p = 0;
            short[] coords = new short[16 * 16 + 14 * 4 + 16 * 16];

            for (int y = 0; y < 16; y += 15) {
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        blocks.setBlock(x, y, z, bedrock);
                        coords[p++] = (short) ((x << 12) | (z << 8) | y);
                    }
                }
            }

            for (int y = 1; y < 15; ++y) {
                blocks.setBlock(0, y, 0, bedrock);
                coords[p++] = (short) y;
                blocks.setBlock(15, y, 0, bedrock);
                coords[p++] = (short) (0xF000 | y);
                blocks.setBlock(15, y, 15, bedrock);
                coords[p++] = (short) (0xFF00 | y);
                blocks.setBlock(0, y, 15, bedrock);
                coords[p++] = (short) (0x0F00 | y);
            }

            CraftPlayer player = (CraftPlayer) sender;
            PacketPlayOutMultiBlockChange multiBlockChange = new PacketPlayOutMultiBlockChange(coords.length, coords, chunk);
            player.getHandle().playerConnection.sendPacket(multiBlockChange);

            sender.sendMessage(ChatColor.GOLD + "Done!");
            return true;
        } else if (label.equalsIgnoreCase("move!")) {
            CraftPlayer player = (CraftPlayer) sender;
            WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
            IChunkProvider chunkProvider = world.getChunkProvider();

            Location location = player.getLocation();
            int playerX = location.getBlockX();
            int playerY = location.getBlockY();
            int playerZ = location.getBlockZ();

            int dx = 1;
            int dy = 0;
            int dz = 0;

            /* Necesito:

                Source ChunkSection, x, z, y

                Dest ChunkSection, x, z, y

             */


//            int p = 0;
//            for (int z = 0, srcZ = posZ, dstZ = posZ + dz; z < lenZ; ++z, ++srcZ, ++dstZ) {
//                for (int y = 0, srcY = posY, dstY = posY + dy; y < lenY; ++y, ++srcY, ++dstY) {
//                    for (int x = 0, srcX = posX, dstX = posX + dx; x < lenX; ++x, ++srcX, ++dstX) {
////                        if (this.blocksPos[x + lenX * (y + lenY * z)]) {
//                        if (this.blocksPos[p]) {
//                            // Hay que mover este bloque de src a dst.
//
//                        }
//                        ++p;
//                    }
//                }
//            }


//            int p;
//
//            int[] srcPos = new int[5 * 5 * 5 * 3];
//            p = 0;
//            for (int x = playerX - 2; x <= playerX + 2; ++x) {
//                for (int y = playerY - 2; y <= playerY + 2; ++y) {
//                    for (int z = playerZ - 2; z <= playerZ + 2; ++z) {
//                        srcPos[p] = x;
//                        srcPos[p + 1] = y;
//                        srcPos[p + 2] = z;
//                        p += 3;
//                    }
//                }
//            }
//
//            int dx = -1;
//            int dy = 0;
//            int dz = 0;
//
//            int[] dstPos = new int[srcPos.length];
//            for (p = 0; p < srcPos.length; p += 3) {
//                dstPos[p] = srcPos[p] + dx;
//                dstPos[p + 1] = srcPos[p + 1] + dy;
//                dstPos[p + 2] = srcPos[p + 2] + dz;
//            }
//
//            IChunkProvider chunkProvider = world.getChunkProvider();
//
//            for (p = 0; p < srcPos.length; p += 3) {
//                final int srcX = srcPos[p];
//                final int srcY = srcPos[p + 1];
//                final int srcZ = srcPos[p + 2];
//                Chunk srcChunk = chunkProvider.getChunkAt(srcX >> 4, srcZ >> 4);
//                ChunkSection srcSection = srcChunk.getSections()[srcY >> 4];
//
//                final int dstX = dstPos[p];
//                final int dstY = dstPos[p + 1];
//                final int dstZ = dstPos[p + 2];
//                Chunk dstChunk = chunkProvider.getChunkAt(dstX >> 4, dstZ >> 4);
//                ChunkSection dstSection = dstChunk.getSections()[dstY >> 4];
//
//                if (dstSection == Chunk.a) {
//                    getLogger().warning("Tengo que agregar una nueva ChunkSection: " + (dstY >> 4));
//                    dstChunk.getSections()[dstY >> 4] = new ChunkSection(dstY >> 4, world.worldProvider.m());
//                }
//
//                IBlockData b = srcSection.getType(srcX, srcY, srcZ);
//                srcSection.setType(srcX, srcY, srcZ, Blocks.AIR.getBlockData());
//                dstSection.setType(dstX, dstY, dstZ, b);
//            }


            int chunkX = playerX >> 4;
            int chunkY = playerY >> 4;
            int chunkZ = playerZ >> 4;

            Chunk chunk = chunkProvider.getChunkAt(chunkX, chunkZ);
            ChunkSection section = chunk.getSections()[chunkY];

            Chunk nextChunk = chunkProvider.getChunkAt(chunkX - 1, chunkZ);
            ChunkSection nextSection = nextChunk.getSections()[chunkY];

            IBlockData b;
            for (int yy = 0; yy < 16; ++yy) {
                for (int zz = 0; zz < 16; ++zz) {
                    b = section.getType(0, yy, zz);
                    if (!b.equals(Blocks.AIR.getBlockData())) {
                        nextSection.setType(15, yy, zz, b);
                    }
                }
            }

            for (int yy = 0; yy < 16; ++yy) {
                for (int zz = 0; zz < 16; ++zz) {
                    for (int xx = 0; xx < 15; ++xx) {
                        b = section.getType(xx + 1, yy, zz);
                        section.setType(xx, yy, zz, b);
                    }
                    section.setType(15, yy, zz, Blocks.AIR.getBlockData());
                }
            }

            section.recalcBlockCounts();
            nextSection.recalcBlockCounts();


            // La lista de los bloques a los que hay que activar en el próximo tick sólo se encuentra
            // dentro del world, por lo que no es necesario interactuar con el chunk para actualizarla.

            List<NextTickListEntry> entries = world.a(chunk, true);
            if (entries != null) {
                for (NextTickListEntry entry : entries) {
                    BlockPosition position = entry.a;
                    final int positionY = position.getY();
                    if (positionY > 64 && positionY < 79) {
                        // inside the ChunkSection of interest
                        position = position.a(-1, 0, 0);
                    }
                    world.b(position, entry.a(), (int) (entry.b - world.worldData.getTime()), entry.c);
                }
            }


            // worldserver.b contiene una lista de todas las TileEntities cargadas en el mundo.
            // chunk.tileEntities contiene solamente las entidades del chunk.
            // Afortunadamente, ambas contienen los mismos objetos, por lo que alcanza con
            // modificar únicamente las TilEntities del chunk.

            List<TileEntity> tileEntities = new ArrayList<>(chunk.tileEntities.values());
            chunk.tileEntities.clear();
            for (TileEntity tileEntity : tileEntities) {
                BlockPosition position = tileEntity.getPosition();
                final int positionY = position.getY();
                if (positionY > 64 && positionY < 79) {
                    // inside the ChunkSection of interest
                    position = position.a(-1, 0, 0);
                    tileEntity.setPosition(position);
                }
                // FIXME: Si position está fuera del chunk, hay que insertarla en el chunk correspondiente en lugar de dejarla en este chunk
                chunk.tileEntities.put(position, tileEntity);
            }


            // world.entityList contiene una lista de todas las entidades cargadas en el mundo.
            // chunk.getEntitySlices() contiene listas con las entidades del chunk en cada slice.
            // Afortunadamente, ambas listas contienen los mismos objetos, por lo que alcanza con
            // modificar únicamente las entities del chunk.

            List<Entity>[] entitySlices = chunk.getEntitySlices();
            List<Entity> entitySlice = entitySlices[chunkY];
            List<Entity> entities = new ArrayList<>(entitySlice);

            for (Entity entity : entities) {
                int ex = MathHelper.floor(entity.getX());
                entity.setPosition(entity.getX() - 1, entity.getY(), entity.getZ());
                if (ex == 0) {
                    chunk.a(entity, chunkY);
                    nextChunk.a(entity);
                }
            }


            chunk.e();
            nextChunk.e();

            PacketPlayOutMapChunk pmc = new PacketPlayOutMapChunk(nextChunk, 1 << chunkY);
            player.getHandle().playerConnection.sendPacket(pmc);
            pmc = new PacketPlayOutMapChunk(chunk, 1 << chunkY);
            player.getHandle().playerConnection.sendPacket(pmc);

            sender.sendMessage(ChatColor.GOLD + "Done!");
            return true;
        } else if (label.equalsIgnoreCase("detect")) {
            CraftPlayer player = (CraftPlayer) sender;
            Block block = player.getTargetBlock((Set<Material>) null, 10);
            sender.sendMessage(ChatColor.AQUA + "Block found: " + block);

            WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
            IChunkProvider chunkProvider = world.getChunkProvider();

            int startX = block.getX();
            int startY = block.getY();
            int startZ = block.getZ();

            Detector detector;
            if (args.length > 0 && args[0].equals("2")) {
                detector = new Detector2(chunkProvider, startX, startY, startZ);
            } else if (args.length > 0 && args[0].equals("3")) {
                detector = new Detector3(chunkProvider, startX, startY, startZ);
            } else if (args.length > 0 && args[0].equals("4")) {
                detector = new Detector4(chunkProvider, startX, startY, startZ);
            } else {
                detector = new Detector1(chunkProvider, startX, startY, startZ);
            }

            long startTime = System.nanoTime();
            detector.detect();
            long endTime = System.nanoTime();

            System.out.println(String.format("Min: (%d, %d, %d)", startX + detector.getMinX(), startY + detector.getMinY(), startZ + detector.getMinZ()));
            System.out.println(String.format("Max: (%d, %d, %d)", startX + detector.getMaxX(), startY + detector.getMaxY(), startZ + detector.getMaxZ()));

            StringBuilder builder = new StringBuilder("Result:\n");
            for (int y = detector.getMinY(); y <= detector.getMaxY(); ++y) {
                for (int z = detector.getMinZ(); z <= detector.getMaxZ(); ++z) {
                    for (int x = detector.getMinX(); x <= detector.getMaxX(); ++x) {
                        builder.append(detector.getDetected()[(x + 128) | ((z + 128) << 8) | ((y + 128) << 16)]);
                    }
                    builder.append('\n');
                }
                builder.append('\n');
            }
            System.out.print(builder.toString());
            String timing = String.format("Tiempo detección: %.2f ms", (endTime - startTime) / 1e6);
            System.out.println(timing);
            sender.sendMessage(ChatColor.GOLD + timing);
            return true;
        }

        return false;
    }


    private static class Detector1 implements Detector {
        private final IChunkProvider chunkProvider;
        private int startX;
        private int startY;
        private int startZ;
        private byte[] detected;
        private int minX;
        private int maxX;
        private int minY;
        private int maxY;
        private int minZ;
        private int maxZ;

        public Detector1(IChunkProvider chunkProvider, int startX, int startY, int startZ) {
            this.chunkProvider = chunkProvider;
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.detected = new byte[256 * 256 * 256];
            this.minX = this.maxX = 0;
            this.minY = this.maxY = 0;
            this.minZ = this.maxZ = 0;
        }

        public void detect() {
            detect(0, 0, 0);
        }

        private void detect(int px, int py, int pz) {
            final int pos = (px + 128) | ((pz + 128) << 8) | ((py + 128) << 16);

            if (this.detected[pos] == 0) {
                final int x = this.startX + px;
                final int y = this.startY + py;
                final int z = this.startZ + pz;
                final IBlockData blockData = this.chunkProvider.getChunkAt(x >> 4, z >> 4).getBlockData(x, y, z);
                if (blockData == Blocks.AIR.getBlockData()) {
                    this.detected[pos] = 1;
                } else {
                    this.detected[pos] = 2;

                    if (px < this.minX) this.minX = px;
                    if (px > this.maxX) this.maxX = px;
                    if (py < this.minY) this.minY = py;
                    if (py > this.maxY) this.maxY = py;
                    if (pz < this.minZ) this.minZ = pz;
                    if (pz > this.maxZ) this.maxZ = pz;

                    detect(px - 1, py - 1, pz);
                    detect(px - 1, py, pz);
                    detect(px - 1, py + 1, pz);
                    detect(px + 1, py - 1, pz);
                    detect(px + 1, py, pz);
                    detect(px + 1, py + 1, pz);
                    detect(px, py - 1, pz - 1);
                    detect(px, py, pz - 1);
                    detect(px, py + 1, pz - 1);
                    detect(px, py - 1, pz + 1);
                    detect(px, py, pz + 1);
                    detect(px, py + 1, pz + 1);
                    detect(px, py - 1, pz);
                    detect(px, py + 1, pz);
                }
            }
        }

        public byte[] getDetected() {
            return detected;
        }

        public int getMinX() {
            return minX;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMaxY() {
            return maxY;
        }

        public int getMinZ() {
            return minZ;
        }

        public int getMaxZ() {
            return maxZ;
        }
    }


    private static class Detector2 implements Detector {
        private final IChunkProvider chunkProvider;
        private int startX;
        private int startY;
        private int startZ;
        private byte[] detected;
        private int minX;
        private int maxX;
        private int minY;
        private int maxY;
        private int minZ;
        private int maxZ;

        public Detector2(IChunkProvider chunkProvider, int startX, int startY, int startZ) {
            this.chunkProvider = chunkProvider;
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.detected = new byte[256 * 256 * 256];
            this.minX = this.maxX = 0;
            this.minY = this.maxY = 0;
            this.minZ = this.maxZ = 0;
        }

        public void detect() {
            detect(0x808080); // y << 16 | z << 8 | x con (x, y, z) = (128, 128, 128)
        }

        private void detect(final int pos) {
            if (this.detected[pos] == 0) {
                final int px = (pos & 0xFF) - 128;
                final int pz = ((pos >>> 8) & 0xFF) - 128;
                final int py = ((pos >>> 16) & 0xFF) - 128;

                final int x = this.startX + px;
                final int y = this.startY + py;
                final int z = this.startZ + pz;
                final IBlockData blockData = this.chunkProvider.getChunkAt(x >> 4, z >> 4).getBlockData(x, y, z);
                if (blockData == Blocks.AIR.getBlockData()) {
                    this.detected[pos] = 1;
                } else {
                    this.detected[pos] = 2;

                    if (px < this.minX) this.minX = px;
                    if (px > this.maxX) this.maxX = px;
                    if (py < this.minY) this.minY = py;
                    if (py > this.maxY) this.maxY = py;
                    if (pz < this.minZ) this.minZ = pz;
                    if (pz > this.maxZ) this.maxZ = pz;

                    // 1 increment in x => 0x000001
                    // 1 increment in z => 0x000100
                    // 1 increment in y => 0x010000

                    detect(pos - 0x010001); // detect(px - 1, py - 1, pz);
                    detect(pos - 0x000001); // detect(px - 1, py    , pz);
                    detect(pos + 0x00ffff); // detect(px - 1, py + 1, pz);

                    detect(pos - 0x00ffff); // detect(px + 1, py - 1, pz);
                    detect(pos + 0x000001); // detect(px + 1, py    , pz);
                    detect(pos + 0x010001); // detect(px + 1, py + 1, pz);

                    detect(pos - 0x010100); // detect(px, py - 1, pz - 1);
                    detect(pos - 0x000100); // detect(px, py    , pz - 1);
                    detect(pos + 0x00ff00); // detect(px, py + 1, pz - 1);

                    detect(pos - 0x00ff00); // detect(px, py - 1, pz + 1);
                    detect(pos + 0x000100); // detect(px, py    , pz + 1);
                    detect(pos + 0x010100); // detect(px, py + 1, pz + 1);

                    detect(pos - 0x010000); // detect(px, py - 1, pz);
                    detect(pos + 0x010000); // detect(px, py + 1, pz);
                }
            }
        }

        public byte[] getDetected() {
            return detected;
        }

        public int getMinX() {
            return minX;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMaxY() {
            return maxY;
        }

        public int getMinZ() {
            return minZ;
        }

        public int getMaxZ() {
            return maxZ;
        }
    }


    private static class Detector3 implements Detector {
        private final IChunkProvider chunkProvider;
        private int startX;
        private int startY;
        private int startZ;
        private byte[] detected;
        private int minX;
        private int maxX;
        private int minY;
        private int maxY;
        private int minZ;
        private int maxZ;

        public Detector3(IChunkProvider chunkProvider, int startX, int startY, int startZ) {
            this.chunkProvider = chunkProvider;
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.detected = new byte[256 * 256 * 256];
            this.minX = this.maxX = 128;
            this.minY = this.maxY = 128;
            this.minZ = this.maxZ = 128;
        }

        public void detect() {
            detect(128, 128, 128);
        }

        private void detect(int px, int py, int pz) {
            final int pos = (px) | (pz << 8) | (py << 16);

            if (this.detected[pos] == 0) {
                loadChunkSection(px, py, pz);
            }

            if (this.detected[pos] == 2) {
                this.detected[pos] = 3;

                if (px < this.minX) this.minX = px;
                if (px > this.maxX) this.maxX = px;
                if (py < this.minY) this.minY = py;
                if (py > this.maxY) this.maxY = py;
                if (pz < this.minZ) this.minZ = pz;
                if (pz > this.maxZ) this.maxZ = pz;

                detect(px - 1, py - 1, pz);
                detect(px - 1, py, pz);
                detect(px - 1, py + 1, pz);
                detect(px + 1, py - 1, pz);
                detect(px + 1, py, pz);
                detect(px + 1, py + 1, pz);
                detect(px, py - 1, pz - 1);
                detect(px, py, pz - 1);
                detect(px, py + 1, pz - 1);
                detect(px, py - 1, pz + 1);
                detect(px, py, pz + 1);
                detect(px, py + 1, pz + 1);
                detect(px, py - 1, pz);
                detect(px, py + 1, pz);
            }
        }

        private void loadChunkSection(int px, int py, int pz) {
            final int x = this.startX + px - 128;
            final int y = this.startY + py - 128;
            final int z = this.startZ + pz - 128;
            System.out.println(String.format("Loading chunk at (%d, %d, %d)", x, y, z));

            Chunk chunk = this.chunkProvider.getChunkAt(x >> 4, z >> 4);
            ChunkSection section = chunk.getSections()[y >> 4];
            int pos = (px - (x & 15)) | ((pz - (z & 15)) << 8) | ((py - (y & 15)) << 16);

            if (section == null) {
                System.out.println("Chunk section is empty.");
                for (int yy = 0; yy < 16; ++yy, pos += (256 * (256 - 16))) {
                    for (int zz = 0; zz < 16; ++zz, pos += (256 - 16)) {
                        for (int xx = 0; xx < 16; ++xx, ++pos) {
                            this.detected[pos] = 1;
                        }
                    }
                }
            } else {
                System.out.println("Chunk section has data.");
                IBlockData AIR = Blocks.AIR.getBlockData();
                for (int yy = 0; yy < 16; ++yy, pos += (256 * (256 - 16))) {
                    for (int zz = 0; zz < 16; ++zz, pos += (256 - 16)) {
                        for (int xx = 0; xx < 16; ++xx, ++pos) {
                            this.detected[pos] = section.getType(xx, yy, zz) == AIR ? (byte) 1 : (byte) 2;
                        }
                    }
                }
            }
        }

        public byte[] getDetected() {
            return detected;
        }

        public int getMinX() {
            return minX - 128;
        }

        public int getMaxX() {
            return maxX - 128;
        }

        public int getMinY() {
            return minY - 128;
        }

        public int getMaxY() {
            return maxY - 128;
        }

        public int getMinZ() {
            return minZ - 128;
        }

        public int getMaxZ() {
            return maxZ - 128;
        }
    }


    private static class FastIntQueue {
        private int capacity;
        private int front;
        private int length;
        private int[] data;

        public FastIntQueue(int capacity) {
            capacity = Math.max(16, 2 * Integer.highestOneBit(capacity - 1));
            this.front = 0;
            this.length = 0;
            this.capacity = capacity;
            this.data = new int[capacity];
        }

        public int size() {
            return this.length;
        }

        public boolean isEmpty() {
            return this.length == 0;
        }

        public void enqueue(int value) {
            if (this.length == this.capacity) {
                this.capacity *= 2;
                final int[] newData = new int[this.capacity];
                System.arraycopy(this.data, 0, newData, 0, this.length);
                this.data = newData;
            }
            int i = this.front + this.length;
            if (i >= this.capacity)
                i -= capacity;
            ++this.length;
            this.data[i] = value;
        }

        public int dequeue() {
            final int value = this.data[this.front];
            if (++this.front == this.capacity) {
                this.front = 0;
            }
            --this.length;
            return value;
        }
    }

    private static class FastIntStack {
        private int capacity;
        private int length;
        private int[] data;

        public FastIntStack(int capacity) {
            capacity = Math.max(16, 2 * Integer.highestOneBit(capacity - 1));
            this.length = 0;
            this.capacity = capacity;
            this.data = new int[capacity];
        }

        public int size() {
            return this.length;
        }

        public boolean isEmpty() {
            return this.length == 0;
        }

        public void push(int value) {
            if (this.length == this.capacity) {
                this.capacity *= 2;
                final int[] newData = new int[this.capacity];
                System.arraycopy(this.data, 0, newData, 0, this.length);
                this.data = newData;
            }
            this.data[this.length] = value;
            ++this.length;
        }

        public int pop() {
            --this.length;
            return this.data[this.length];
        }
    }

    private static class Detector4 implements Detector {
        private final IChunkProvider chunkProvider;
        private int startX;
        private int startY;
        private int startZ;
        private byte[] detected;
        private int minX;
        private int maxX;
        private int minY;
        private int maxY;
        private int minZ;
        private int maxZ;

        public Detector4(IChunkProvider chunkProvider, int startX, int startY, int startZ) {
            this.chunkProvider = chunkProvider;
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.detected = new byte[256 * 256 * 256];
            this.minX = this.maxX = 0;
            this.minY = this.maxY = 0;
            this.minZ = this.maxZ = 0;
        }

        public void detect() {
            FastIntStack stack = new FastIntStack(256);
            final int FIRST_POS = 0x808080; // y << 16 | z << 8 | x con (x, y, z) = (128, 128, 128)

            this.detected[FIRST_POS] = 2; // Include initial Block
            stack.push(FIRST_POS);

            do {
                final int pos = stack.pop();
                final int px = (pos & 0xFF) - 128;
                final int pz = ((pos >>> 8) & 0xFF) - 128;
                final int py = ((pos >>> 16) & 0xFF) - 128;
                final int x = this.startX + px;
                final int y = this.startY + py;
                final int z = this.startZ + pz;

                final IBlockData blockData = this.chunkProvider.getChunkAt(x >> 4, z >> 4).getBlockData(x, y, z);
                if (blockData == Blocks.AIR.getBlockData()) {
                    this.detected[pos] = 1;
                } else {
                    this.detected[pos] = 2;

                    if (px < this.minX) this.minX = px;
                    if (px > this.maxX) this.maxX = px;
                    if (py < this.minY) this.minY = py;
                    if (py > this.maxY) this.maxY = py;
                    if (pz < this.minZ) this.minZ = pz;
                    if (pz > this.maxZ) this.maxZ = pz;

                    // 1 increment in x => 0x000001
                    // 1 increment in z => 0x000100
                    // 1 increment in y => 0x010000

                    final int p01 = pos - 0x010001; // detect(px - 1, py - 1, pz);
                    if (this.detected[p01] == 0) stack.push(p01);
                    final int p02 = pos - 0x000001; // detect(px - 1, py    , pz);
                    if (this.detected[p02] == 0) stack.push(p02);
                    final int p03 = pos + 0x00ffff; // detect(px - 1, py + 1, pz);
                    if (this.detected[p03] == 0) stack.push(p03);
                    final int p04 = pos - 0x00ffff; // detect(px + 1, py - 1, pz);
                    if (this.detected[p04] == 0) stack.push(p04);
                    final int p05 = pos + 0x000001; // detect(px + 1, py    , pz);
                    if (this.detected[p05] == 0) stack.push(p05);
                    final int p06 = pos + 0x010001; // detect(px + 1, py + 1, pz);
                    if (this.detected[p06] == 0) stack.push(p06);
                    final int p07 = pos - 0x010100; // detect(px, py - 1, pz - 1);
                    if (this.detected[p07] == 0) stack.push(p07);
                    final int p08 = pos - 0x000100; // detect(px, py    , pz - 1);
                    if (this.detected[p08] == 0) stack.push(p08);
                    final int p09 = pos + 0x00ff00; // detect(px, py + 1, pz - 1);
                    if (this.detected[p09] == 0) stack.push(p09);
                    final int p10 = pos - 0x00ff00; // detect(px, py - 1, pz + 1);
                    if (this.detected[p10] == 0) stack.push(p10);
                    final int p11 = pos + 0x000100; // detect(px, py    , pz + 1);
                    if (this.detected[p11] == 0) stack.push(p11);
                    final int p12 = pos + 0x010100; // detect(px, py + 1, pz + 1);
                    if (this.detected[p12] == 0) stack.push(p12);
                    final int p13 = pos - 0x010000; // detect(px, py - 1, pz);
                    if (this.detected[p13] == 0) stack.push(p13);
                    final int p14 = pos + 0x010000; // detect(px, py + 1, pz);
                    if (this.detected[p14] == 0) stack.push(p14);
                }
            } while (!stack.isEmpty());

            System.out.println("stack.capacity == " + stack.capacity);
        }

        public byte[] getDetected() {
            return detected;
        }

        public int getMinX() {
            return minX;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMaxY() {
            return maxY;
        }

        public int getMinZ() {
            return minZ;
        }

        public int getMaxZ() {
            return maxZ;
        }
    }

}
