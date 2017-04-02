package io.github.gosella.fastmove;

import net.minecraft.server.v1_11_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class Main extends JavaPlugin {

    private int posX;
    private int posY;
    private int posZ;

    private int lenX;
    private int lenY;
    private int lenZ;

    private boolean[] blocksPos;

    @Override
    public void onEnable() {
        this.lenX = 5;
        this.lenY = 5;
        this.lenZ = 5;
        this.blocksPos = new boolean[this.lenX * this.lenY * this.lenZ];
        for (int p = 0; p < this.blocksPos.length; ++p) {
            this.blocksPos[p] = true;
//            this.blocksPos[posX + lenX * (posY + lenY * posZ)] = false;
        }

        this.posX = 0;
        this.posY = 80;
        this.posZ = 0;
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

            new MoveManager(
                    this,
                    player,
                    ((CraftWorld) player.getWorld()).getHandle(),
//                    player.getLocation().getBlockX() & ~15
                    player.getLocation().getBlockX() - 1
            ).runTaskTimer(this, 90, 15);
//            ).runTask(this);


//            Block block = player.getTargetBlock((Set<Material>) null, 10);
//            sender.sendMessage(ChatColor.AQUA + "Block found: " + block);

//            IBlockData newBlock = net.minecraft.server.v1_11_R1.Block.getByCombinedId(7); // minecraft:bedrock
//            IBlockData newBlock = net.minecraft.server.v1_11_R1.Block.getByCombinedId(35 | 0xb000); // minecraft:blue_wool

//            WorldServer world = ((CraftWorld) player.getWorld()).getHandle();

//            BlockPosition pos = new BlockPosition(block.getX(), block.getY()+1, block.getZ());
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
            IBlockData bedrock = net.minecraft.server.v1_11_R1.Block.getByCombinedId(7);// minecraft:bedrock
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

            List<TileEntity> tileEntities = new ArrayList<TileEntity>(chunk.tileEntities.values());
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
            List<Entity> entities = new ArrayList<Entity>(entitySlice);

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
        }

        return false;
    }
}
