package fun.ogtimes.skywars.spigot.utils;

import java.util.Random;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

public class VoidUtil extends ChunkGenerator {
   public byte[][] generateBlockSections(World var1, Random var2, int var3, int var4, BiomeGrid var5) {
       return new byte[var1.getMaxHeight() / 16][];
   }
}
