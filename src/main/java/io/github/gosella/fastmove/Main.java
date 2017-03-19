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

import java.util.*;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("FastMove enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("do")) {
            getLogger().info("do command executed!");

            if (!(sender instanceof Player) ) {
                sender.sendMessage(ChatColor.RED + "Only a Player can do that.");
                return false;
            }

            Player player = (Player) sender;
//            Block block = player.getTargetBlock((Set<Material>) null, 10);
//            sender.sendMessage(ChatColor.AQUA + "Block found: " + block);

//            IBlockData newBlock = net.minecraft.server.v1_11_R1.Block.getByCombinedId(7); // minecraft:bedrock
//            IBlockData newBlock = net.minecraft.server.v1_11_R1.Block.getByCombinedId(35 | 0xb000); // minecraft:blue_wool

            WorldServer world = ((CraftWorld) player.getWorld()).getHandle();

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

            Location location = player.getLocation();
            int playerX = location.getBlockX();
            int playerY = location.getBlockY();
            int playerZ = location.getBlockZ();

            int chunkX = playerX >> 4;
            int chunkY = playerY >> 4;
            int chunkZ = playerZ >> 4;

            Chunk chunk = world.getChunkProvider().getChunkAt(chunkX, chunkZ);
            Chunk nextChunk = world.getChunkProvider().getChunkAt(chunkX - 1, chunkZ);

            List<Entity>[] entitySlices = chunk.getEntitySlices();
            List<Entity> entitySlice = entitySlices[chunkY];
            for (Entity entity : entitySlice) {
                getLogger().info("Entity: " + entity + " - " + entity.locX + " -> " + entity.getChunkX());
            }

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
                        coords[p++] = (short)((x << 12) | (z << 8) | y);
                    }
                }
            }

            for (int y = 1; y < 15; ++y) {
                blocks.setBlock(0, y, 0, bedrock);
                coords[p++] = (short)y;
                blocks.setBlock(15, y, 0, bedrock);
                coords[p++] = (short)(0xF000 | y);
                blocks.setBlock(15, y, 15, bedrock);
                coords[p++] = (short)(0xFF00 | y);
                blocks.setBlock(0, y, 15, bedrock);
                coords[p++] = (short)(0x0F00 | y);
            }

            CraftPlayer player = (CraftPlayer) sender;
            PacketPlayOutMultiBlockChange multiBlockChange = new PacketPlayOutMultiBlockChange(coords.length, coords, chunk);
            player.getHandle().playerConnection.sendPacket(multiBlockChange);

            sender.sendMessage(ChatColor.GOLD + "Done!");
            return true;
        } else if (label.equalsIgnoreCase("move!")) {
            CraftPlayer player = (CraftPlayer) sender;
            WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
            Location location = player.getLocation();
            int playerX = location.getBlockX();
            int playerY = location.getBlockY();
            int playerZ = location.getBlockZ();

            // La idea es mover 5x5x5 bloques alrededor del player

//            int[] srcPos = new int[5*5*5*3];
//            int p = 0;
//            for (int z = playerZ - 2; z <= playerZ + 2; ++z) {
//                for (int y = playerY - 2; y <= playerY + 2; ++y) {
//                    for (int x = playerX - 2; x <= playerX + 2; ++x) {
//                        srcPos[p] = x;
//                        srcPos[p+1] = y;
//                        srcPos[p+2] = z;
//                        p += 3;
//                    }
//                }
//            }
//
//            int[] dstPos = new int[srcPos.length];
//            p = 0;
//            for (int z = playerZ - 2; z <= playerZ + 2; ++z) {
//                for (int y = playerY - 2; y <= playerY + 2; ++y) {
//                    for (int x = playerX - 2; x <= playerX + 2; ++x) {
//                        srcPos[p] = x;
//                        dstPos[p] = x - 1;
//                        srcPos[p+1] = y;
//                        dstPos[p+1] = y;
//                        srcPos[p+2] = z;
//                        dstPos[p+2] = z;
//                        p += 3;
//                    }
//                }
//            }


            int chunkX = playerX >> 4;
            int chunkY = playerY >> 4;
            int chunkZ = playerZ >> 4;

            Chunk chunk = world.getChunkProvider().getChunkAt(chunkX, chunkZ);
            ChunkSection section = chunk.getSections()[chunkY];

            Chunk nextChunk = world.getChunkProvider().getChunkAt(chunkX - 1, chunkZ);
            ChunkSection nextSection = nextChunk.getSections()[chunkY];

            IBlockData b;
            for(int yy = 0; yy < 16; ++yy) {
                for(int zz = 0; zz < 16; ++zz) {
                    b = section.getType(0, yy, zz);
                    if (!b.equals(Blocks.AIR.getBlockData())) {
                        nextSection.setType(15, yy, zz, b);
                    }
                }
            }

            for(int yy = 0; yy < 16; ++yy) {
                for(int zz = 0; zz < 16; ++zz) {
                    for(int xx = 0; xx < 15; ++xx) {
                        b = section.getType(xx + 1 , yy, zz);
                        section.setType(xx, yy, zz, b);
                    }
                    section.setType(15, yy, zz, Blocks.AIR.getBlockData());
                }
            }

            section.recalcBlockCounts();
            nextSection.recalcBlockCounts();


            List<NextTickListEntry> entries = world.a(chunk, true);
            if (entries != null) {
                for (NextTickListEntry entry : entries) {
                    BlockPosition position = entry.a;
                    final int positionY = position.getY();
                    if (positionY > 64 && positionY < 79) {
                        // inside the ChunkSection of interest
                        position = position.a(-1, 0, 0);
                    }
                    world.b(position, entry.a(), (int)(entry.b - world.worldData.getTime()), entry.c);
                }
            }

            List<TileEntity> tileEntities = new ArrayList<TileEntity>(chunk.tileEntities.values());
            chunk.tileEntities.clear();
            for (TileEntity tileEntity: tileEntities) {
                BlockPosition position = tileEntity.getPosition();
                final int positionY = position.getY();
                if (positionY > 64 && positionY < 79) {
                    // inside the ChunkSection of interest
                    position = position.a(-1, 0, 0);
                    tileEntity.setPosition(position);
                }
                chunk.tileEntities.put(position, tileEntity);
            }


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
