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

/**
 * Created by german on 17/01/17.
 */
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

//            int x = block.getX();
//            int y = block.getY() + 4;
//            int z = block.getZ();
//
//            sender.sendMessage(ChatColor.AQUA + "Placing blocks at @ (" + x + ", " + y + ", " + z + ")");

            Location location = player.getLocation();
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            Chunk chunk = world.getChunkProvider().getChunkAt(x >> 4, z >> 4);
            ChunkSection section = chunk.getSections()[y >> 4];
            List<TileEntity> tileEntities = new ArrayList<TileEntity>(chunk.getTileEntities().values());
            List<Entity>[] entitySlices = chunk.getEntitySlices();
            List<Entity> entitySlice = entitySlices[y >> 4];
            for (Entity entity : entitySlice) {
                getLogger().info("Entity: " + entity);
            }

//            DataPaletteBlock blocks = section.getBlocks();
//
//            short[] coords = new short[6];
//
//            x &= 15;
//            z &= 15;
//            int yy = y & 15;
//            blocks.setBlock(x, yy, z, newBlock);
//            coords[0] = (short)(x << 12 | z << 8 | y);
//
//            x -= 1;
//            blocks.setBlock(x, yy, z, newBlock);
//            coords[1] = (short)(x << 12 | z << 8 | y);
//
//            x += 2;
//            blocks.setBlock(x, yy, z, newBlock);
//            coords[2] = (short)(x << 12 | z << 8 | y);
//
//            x -= 1;
//            z -= 1;
//            blocks.setBlock(x, yy, z, newBlock);
//            coords[3] = (short)(x << 12 | z << 8 | y);
//
//            z += 2;
//            blocks.setBlock(x, yy, z, newBlock);
//            coords[4] = (short)(x << 12 | z << 8 | y);
//
//            z -= 1;
//            y += 1;
//            blocks.setBlock(x, yy+1, z, newBlock);
//            coords[5] = (short)(x << 12 | z << 8 | y);
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
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            Chunk chunk = world.getChunkProvider().getChunkAt(x >> 4, z >> 4);

            ChunkSection section = chunk.getSections()[y >> 4];

            for(int yy = 1; yy < 15; ++yy) {
                for(int zz = 1; zz < 15; ++zz) {
                    if (!section.getType(1, yy, zz).equals(Blocks.AIR.getBlockData())) {
                        sender.sendMessage(ChatColor.RED + "Can't move blocks past the border of this Chunk! (yet)");
                        return true;
                    }
                }
            }

            List<Entity>[] entitySlices = chunk.getEntitySlices();
            List<Entity> entitySlice = entitySlices[y >> 4];
            for (Entity entity : entitySlice) {
                if (entity.getChunkX() == 15) {
                    sender.sendMessage(ChatColor.RED + "Can't move entities past the border of this Chunk! (yet)");
                    return true;
                }
            }

            IBlockData b;
            for(int xx = 1; xx < 14; ++xx) {
                for(int yy = 1; yy < 15; ++yy) {
                    for(int zz = 1; zz < 15; ++zz) {
                        b = section.getType(xx + 1 , yy, zz);
                        section.setType(xx, yy, zz, b);
//                        section.setType(xx, yy, pxx, Blocks.AIR.getBlockData());
                    }
                }
            }

            b = Blocks.AIR.getBlockData();
            for(int yy = 1; yy < 15; ++yy) {
                for(int zz = 1; zz < 15; ++zz) {
                    section.setType(14, yy, zz, b);
                }
            }

            section.recalcBlockCounts();

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


//            List<NextTickListEntry> entries = world.a(chunk, false);  // con true borra los ticks retornados
//
//            StructureBoundingBox bb = new StructureBoundingBox(1, 1, 1, 15, 15, 15);
//            List<NextTickListEntry> entries = world.a(bb, false);  // con true borra los ticks retornados
//
//            if (entries != null) {
//                for (NextTickListEntry entry : entries) {
//                    world.b(entry.a.a(0, 0, 0), entry.a(), (int)(entry.b - world.worldData.getTime()), entry.c);
//                }
//            }

            List<TileEntity> entities = new ArrayList<TileEntity>(chunk.tileEntities.values());
            chunk.tileEntities.clear();
            for (TileEntity entity: entities) {
                BlockPosition position = entity.getPosition();
                final int positionY = position.getY();
                if (positionY > 64 && positionY < 79) {
                    // inside the ChunkSection of interest
                    position = position.a(-1, 0, 0);
                    entity.setPosition(position);
                }
                chunk.tileEntities.put(position, entity);
            }

            for (Entity entity : entitySlice) {
                entity.setPosition(entity.getX() - 1, entity.getY(), entity.getZ());
            }

            chunk.e();

            PacketPlayOutMapChunk pmc = new PacketPlayOutMapChunk(chunk, 1 << (y >> 4));
            player.getHandle().playerConnection.sendPacket(pmc);

            sender.sendMessage(ChatColor.GOLD + "Done!");
            return true;
        }

        return false;
    }
}
