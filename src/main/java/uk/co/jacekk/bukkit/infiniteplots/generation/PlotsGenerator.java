package uk.co.jacekk.bukkit.infiniteplots.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

/**
 * Responsible for building the ground of a world 
 */
public class PlotsGenerator extends ChunkGenerator {
	
	private int size;
	private int height;
	
	private Material pathId;
	private Material wallLowerId;
	private Material wallUpperId;
	private Material surfaceId;
	private Material groundId;
	
	public PlotsGenerator(int size, int height, Material pathId, Material wallLowerId, Material wallUpperId, Material surfaceId, Material groundId){
		this.size = size;
		this.height = height;
		
		this.pathId = pathId;
		this.wallLowerId = wallLowerId;
		this.wallUpperId = wallUpperId;
		this.surfaceId = surfaceId;
		this.groundId = groundId;
	}
	
	/**
	 * Gets the grid size used by this generator
	 * 
	 * @return The grid size
	 */
	public int getGridSize(){
		return this.size;
	}
	
	@Override
	public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
		return new PlainsBiomeProvider();
	}
	
	@Override
	public List<BlockPopulator> getDefaultPopulators(World world){
		ArrayList<BlockPopulator> populators = new ArrayList<>();
		
		populators.add(new PathPopulator(this.size, this.height, this.pathId, this.wallLowerId, this.wallUpperId));
		
		return populators;
	}
	
	@Override
	public Location getFixedSpawnLocation(World world, Random rand){
		return new Location(world, 0, this.height + 1, 0);
	}
	
	@Override
	public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
		for (int x = 0; x < 16; ++x){
			for (int z = 0; z < 16; ++z){
				chunkData.setBlock(x, 0, z, Material.BEDROCK);
				
				for (int y = 1; y < this.height; ++y){
					chunkData.setBlock(x, y, z, this.groundId);
				}
				
				chunkData.setBlock(x, this.height, z, this.surfaceId);
				
				// TODO Set biome to plains
				//biomes.setBiome(x, z, Biome.PLAINS);
			}
		}
	}
	
}
