package uk.co.jacekk.bukkit.infiniteplots.nms;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import uk.co.jacekk.bukkit.baseplugin.util.ReflectionUtils;

public class ChunkWrapper extends LevelChunk {
	
	private LevelChunk chunk;
	private int[] buildLimits;
	
	public ChunkWrapper(LevelChunk chunk, int[] buildLimits){
		super(chunk.getLevel(), chunk.getPos());
		
		this.chunk = chunk;
		this.buildLimits = buildLimits;
        
        try {
	        ReflectionUtils.setFieldValue(LevelChunk.class, "sections", this, this.chunk.getSections());
			// todo something about biome index
	        //this.a(this.chunk.getBiomeIndex()); // Sets this.f
	        ReflectionUtils.setFieldValue(LevelChunk.class, "g", this, ReflectionUtils.getFieldValue(LevelChunk.class, "g", int[].class, this.chunk));
	        ReflectionUtils.setFieldValue(LevelChunk.class, "h", this, ReflectionUtils.getFieldValue(LevelChunk.class, "h", boolean[].class, this.chunk));
	        ReflectionUtils.setFieldValue(LevelChunk.class, "tileEntities", this, ReflectionUtils.getFieldValue(LevelChunk.class, "tileEntities", Map.class, this.chunk));
	        ReflectionUtils.setFieldValue(LevelChunk.class, "w", this, ReflectionUtils.getFieldValue(LevelChunk.class, "w", int.class, this.chunk));
	        ReflectionUtils.setFieldValue(LevelChunk.class, "x", this, ReflectionUtils.getFieldValue(LevelChunk.class, "x", ConcurrentLinkedQueue.class, this.chunk));
	        ReflectionUtils.setFieldValue(LevelChunk.class, "entitySlices", this, ReflectionUtils.getFieldValue(LevelChunk.class, "entitySlices", ClassInstanceMultiMap[].class, this.chunk));
	        ReflectionUtils.setFieldValue(LevelChunk.class, "world", this, ReflectionUtils.getFieldValue(LevelChunk.class, "world", Level.class, this.chunk));
	        ReflectionUtils.setFieldValue(LevelChunk.class, "locX", this, ReflectionUtils.getFieldValue(LevelChunk.class, "locX", int.class, this.chunk));
	        ReflectionUtils.setFieldValue(LevelChunk.class, "locZ", this, ReflectionUtils.getFieldValue(LevelChunk.class, "locZ", int.class, this.chunk));
	        ReflectionUtils.setFieldValue(LevelChunk.class, "heightMap", this, ReflectionUtils.getFieldValue(LevelChunk.class, "heightMap", int[].class, this.chunk));
        }catch (NoSuchFieldException e){
        	e.printStackTrace();
        }
        
	}
	
	// Check the build limits when setting blocks in this chunk
	public BlockState setBlockState(BlockPos blockposition, BlockState iblockdata){
		int wx = (this.chunk.getPos().x * 16) + blockposition.getX();
		int wz = (this.chunk.getPos().z * 16) + blockposition.getZ();
		
		if (wx >= this.buildLimits[0] && wz >= this.buildLimits[1] && wx <= this.buildLimits[2] && wz <= this.buildLimits[3]){
			return this.chunk.setBlockState(blockposition, iblockdata, false, false);
		}
		
		return null;
	}
	
	// Return air for everything outside the build limits
	@Override
	public BlockEntity getBlockEntity(BlockPos pos){
		int wx = (this.chunk.getPos().x * 16) + pos.getX();
		int wz = (this.chunk.getPos().z * 16) + pos.getZ();
		
		if (wx >= this.buildLimits[0] && wz >= this.buildLimits[1] && wx <= this.buildLimits[2] && wz <= this.buildLimits[3]){
			return this.chunk.getBlockEntity(pos);
		}
		// TODO Return air?
		return this.chunk.getBlockEntity(pos);
	}
	
}
