package uk.co.jacekk.bukkit.infiniteplots.plot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import uk.co.jacekk.bukkit.baseplugin.BaseObject;
import uk.co.jacekk.bukkit.baseplugin.config.PluginConfig;
import uk.co.jacekk.bukkit.infiniteplots.Config;
import uk.co.jacekk.bukkit.infiniteplots.InfinitePlots;
import uk.co.jacekk.bukkit.infiniteplots.flag.PlotFlag;
import uk.co.jacekk.bukkit.infiniteplots.generation.PlotsGenerator;
import uk.co.jacekk.bukkit.infiniteplots.plot.PlotLocation.Direction;
import uk.co.jacekk.bukkit.infiniteplots.plot.decorator.FlatPlotDecorator;

/**
 * Represents a plot in the world.
 */
public class Plot extends BaseObject<InfinitePlots> {
	
	private final File configFile;
	private final PluginConfig config;
	private final PlotLocation location;
	private final int size;
	private final PlotStats stats;
	
	private int[] plotLimits;
	private int[] buildLimits;
	
	public Plot(InfinitePlots plugin, File configFile, PluginConfig config){
		super(plugin);
		
		this.configFile = configFile;
		this.config = config;
		this.location = new PlotLocation(config.getString(PlotConfig.LOCATION_WORLD_NAME), config.getInt(PlotConfig.LOCATION_X), config.getInt(PlotConfig.LOCATION_Z));
		this.size = ((PlotsGenerator) this.location.getWorld().getGenerator()).getGridSize();
		this.stats = new PlotStats(this, this.config);
		
		int x1 = (int) Math.floor(((this.location.getX() * this.size) / this.size) * this.size);
		int z1 = (int) Math.floor(((this.location.getZ() * this.size) / this.size) * this.size);
		int x2 = x1 + this.size;
		int z2 = z1 + this.size;
		
		this.plotLimits = new int[]{x1, z1, x2, z2};
		this.buildLimits = new int[]{x1 + 4, z1 + 4, x2 - 4, z2 - 4};
	}
	
	/**
	 * Gets the file used to store this plots config.
	 * 
	 * @return The {@link File}
	 */
	public File getConfigFile(){
		return this.configFile;
	}
	
	/**
	 * Gets the location of this plot.
	 * 
	 * @return The {@link PlotLocation} of this plot.
	 */
	public PlotLocation getLocation(){
		return this.location;
	}
	
	/**
	 * Gets the {@link PlotStats} for this plot.
	 * 
	 * @return The stats
	 */
	public PlotStats getStats(){
		return this.stats;
	}
	
	/**
	 * Gets the name of this plot
	 * 
	 * @return The plot name
	 */
	public String getName(){
		return this.config.getString(PlotConfig.INFO_NAME);
	}
	
	/**
	 * Sets the name of this plot
	 * 
	 * @param name The new name for the plot
	 */
	public void setName(String name){
		this.config.set(PlotConfig.INFO_NAME, name);
	}
	
	/**
	 * Gets the limits of the plot.
	 * 
	 * @return An array of X,Z coordinates [x1, z1, x2, z2]
	 */
	public int[] getBuildLimits(){
		return this.buildLimits;
	}
	
	/**
	 * Gets the player that is the admin of this plot.
	 * 
	 * @return The player name.
	 */
	public OfflinePlayer getAdmin(){
		return plugin.getServer().getOfflinePlayer(UUID.fromString(this.config.getString(PlotConfig.AUTH_ADMIN_UUID)));
	}
	
	/**
	 * Sets the admin of this plot.
	 * 
	 * @param admin The name of the player.
	 */
	public void setAdmin(OfflinePlayer admin){
		this.config.set(PlotConfig.AUTH_ADMIN_UUID, admin.getUniqueId());
	}
	
	/**
	 * Gets all of the players (not including the plot admin) that are allowed
	 * to build in this plot.
	 * 
	 * @return The list of players.
	 */
	public List<OfflinePlayer> getBuilders(){
		List<String> ids = this.config.getStringList(PlotConfig.AUTH_BUILDER_UUIDS);
		List<OfflinePlayer> players = new ArrayList<OfflinePlayer>(ids.size());
		
		for (String id : ids){
			players.add(plugin.getServer().getOfflinePlayer(UUID.fromString(id)));
		}
		
		return players;
	}
	
	/**
	 * Gets the names of all of the players (not including the plot admin) that 
	 * are allowed to build in this plot.
	 * 
	 * @return The list of player names.
	 */
	public List<String> getBuilderNames(){
		List<String> ids = this.config.getStringList(PlotConfig.AUTH_BUILDER_UUIDS);
		List<String> players = new ArrayList<String>(ids.size());
		
		for (String id : ids){
			OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(id));
			String name = null;
			if (player != null){
				name = player.getName();
			}
				
			if (name == null){
				players.add(id);
			}else{
				players.add(name);
			}
		}
		
		return players;
	}
	
	/**
	 * Checks to see if a player can build in this plot.
	 * 
	 * <p>
	 * A player that is not able to build should not be able to interact with
	 * the environment in any way at all inside the plot area.
	 * <p>
	 * 
	 * @param playerName The name of the player to test.
	 * @return True if the player can build, false if not.
	 */
	public boolean canBuild(OfflinePlayer player){
		return (!this.isBuildProtected() || this.getAdmin().getUniqueId().equals(player.getUniqueId()) || this.getBuilders().contains(player));
	}
	
	/**
	 * Checks to see if a player can enter the plot.
	 * 
	 * Note that this does not perform a permissions check.
	 * 
	 * @param playerName The name of the player
	 * @return True if they can enter false if not.
	 */
	public boolean canEnter(OfflinePlayer player){
		return (!this.isEnterProtected() || this.canBuild(player));
	}
	
	/**
	 * Sets if the plot is protected from people building in it or not
	 * 
	 * @param enable The build protection state.
	 */
	public void setBuildProtection(boolean enable){
		this.config.set(PlotConfig.PROTECTION_BUILD, enable);
	}
	
	/**
	 * Sets if the plot is protected from people entering it or not.
	 * 
	 * @param enable The enter protection state.
	 */
	public void setEnterProtection(boolean enable){
		this.config.set(PlotConfig.PROTECTION_ENTER, enable);
	}
	
	/**
	 * Gets the build protection state of the plot.
	 * 
	 * If this is true then only the owner or players listed as a builder
	 * will be allowed to modify the world inside the plot.
	 * 
	 * @return The state.
	 */
	public boolean isBuildProtected(){
		return this.config.getBoolean(PlotConfig.PROTECTION_BUILD);
	}
	
	/**
	 * Gets the enter protection state of the plot.
	 * 
	 * If this is true then only the owner or players listed as a builder
	 * will be allowed to move into the plot area.
	 * 
	 * @return The state.
	 */
	public boolean isEnterProtected(){
		return this.config.getBoolean(PlotConfig.PROTECTION_ENTER);
	}
	
	/**
	 * Returns a list of strings representing the list of player UUIDs.
	 * 
	 * @param playerList 
	 * @return A list of UUIDs represented as strings
	 */
	protected List<String> getStringList(Collection<OfflinePlayer> playerList) {
		List<String> uuids = new ArrayList<String>();
		for (OfflinePlayer player: playerList){
			uuids.add(player.getUniqueId().toString());
		}
		return uuids;
	}
	
	/**
	 * Adds a builder to this plot.
	 * 
	 * @param playerName The name of the player to add.
	 */
	public void addBuilder(OfflinePlayer player){
		List<OfflinePlayer> builders = this.getBuilders();
		builders.add(player);
		
		this.config.set(PlotConfig.AUTH_BUILDER_UUIDS, getStringList(builders));
	}
	
	/**
	 * Removes a builder from this plot.
	 * 
	 * @param playerName The name of the player to remove.
	 */
	public void removeBuilder(OfflinePlayer player){
		List<OfflinePlayer> builders = this.getBuilders();
		builders.remove(player);
		
		this.config.set(PlotConfig.AUTH_BUILDER_UUIDS, getStringList(builders));
	}
	
	/**
	 * Removes all builders from this plot.
	 */
	public void removeAllBuilders(){
		this.config.set(PlotConfig.AUTH_BUILDER_UUIDS, Arrays.asList(new String[0]));
	}
	
	/**
	 * Checks to see if a player is within the buildable area of a plot.
	 * 
	 * @param player The {@link Player} to check.
	 * @param location The {@link Location} to check.
	 * @return True if the player is in the area false if not.
	 */
	public boolean withinBuildableArea(Player player, Location location){
		int x = location.getBlockX();
		int z = location.getBlockZ();
		
		if (plugin.config.getBoolean(Config.CLAIM_PROTECT_PATHS)){
			return (x >= this.buildLimits[0] && z >= this.buildLimits[1] && x <= this.buildLimits[2] && z <= this.buildLimits[3]);
		}
		
		if (x < this.plotLimits[0] || z < this.plotLimits[1] || x > this.plotLimits[2] || z > this.plotLimits[3]){
			return false;
		}
		
		Plot plot0 = plugin.getPlotManager().getPlotAt(this.location.getRelative(Direction.SOUTH));
		Plot plot1 = plugin.getPlotManager().getPlotAt(this.location.getRelative(Direction.WEST));
		Plot plot2 = plugin.getPlotManager().getPlotAt(this.location.getRelative(Direction.NORTH));
		Plot plot3 = plugin.getPlotManager().getPlotAt(this.location.getRelative(Direction.EAST));
		
		if ((plot0 == null || !plot0.canBuild(player)) && x < this.buildLimits[0]){
			return false;
		}
		
		if ((plot1 == null || !plot1.canBuild(player)) && z < this.buildLimits[1]){
			return false;
		}
		
		if ((plot2 == null || !plot2.canBuild(player)) && x > this.buildLimits[2]){
			return false;
		}
		
		if ((plot3 == null || !plot3.canBuild(player)) && z > this.buildLimits[3]){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks to see if a specific flag is enabled for this plot.
	 * 
	 * <p>
	 * An enabled flag will allow the event it describes to take place,
	 * </p>
	 * 
	 * @param flag The flag to check
	 * @return The value of the flag for this plot
	 */
	public boolean isFlagEnabled(PlotFlag flag){
		return this.config.getBoolean(flag.getConfigKey());
	}
	
	/**
	 * Sets a flag for this plot.
	 * 
	 * <p>
	 * An enabled flag will allow the event it describes to take place,
	 * </p>
	 * 
	 * @param flag The flag to set.
	 * @param value The value to set this flag to.
	 */
	public void setFlag(PlotFlag flag, boolean value){
		this.config.set(flag.getConfigKey(), value);
	}
	
	/**
	 * Sets the biome for the plot.
	 * 
	 * @param biome The biome to set.
	 */
	public void setBiome(Biome biome){
		World world = plugin.getServer().getWorld(this.location.getWorldName());
		
		for (int x = this.buildLimits[0]; x <= this.buildLimits[2]; ++x){
			for (int z = this.buildLimits[1]; z <= this.buildLimits[3]; ++z){
				world.setBiome(x, z, biome);
			}
		}
	}
	
	/**
	 * Gets the biome for this plot
	 * 
	 * @return The {@link Biome} used
	 */
	public Biome getBiome(){
		World world = plugin.getServer().getWorld(this.location.getWorldName());
		return world.getBiome(this.buildLimits[0], this.buildLimits[1]);
	}
	
	/**
	 * Regenerates the buildable region of this plot.
	 */
	public void regenerate(){
		(new FlatPlotDecorator(plugin, Material.getMaterial(plugin.config.getString(Config.BLOCKS_GROUND)), Material.getMaterial(plugin.config.getString(Config.BLOCKS_SURFACE)), (byte) 0, (byte) 0)).decorate(this);
	}
	
	/**
	 * Places a sign at all corners of the plot with the owners name.
	 */
	public void createSigns(){
		int[] buildLimits = this.getBuildLimits();
		
		int x3 = buildLimits[0];
		int z3 = buildLimits[1] + (this.size - 7);
		int x4 = buildLimits[2];
		int z4 = buildLimits[3] - (this.size - 7);
		int y = plugin.config.getInt(Config.GRID_HEIGHT);
		World world = plugin.getServer().getWorld(this.getLocation().getWorldName());
		
		if (plugin.config.getInt(Config.BLOCKS_UPPER_WALL) == 0){
			y += 1;
		}else{
			y += 2;
		}
		
		Block cornerOne = world.getBlockAt(buildLimits[0] - 1, y, buildLimits[1] - 1);
		Block cornerTwo = world.getBlockAt(buildLimits[2] + 1, y, buildLimits[3] + 1);
		Block cornerThree = world.getBlockAt(x3 - 1, y, z3);
		Block cornerFour = world.getBlockAt(x4 + 1, y, z4);
		
		cornerOne.setType(Material.OAK_SIGN);
		cornerTwo.setType(Material.OAK_SIGN);
		cornerThree.setType(Material.OAK_SIGN);
		cornerFour.setType(Material.OAK_SIGN);
		
		// north west
		Sign signOne = (Sign) cornerOne.getState();
		// south east
		Sign signTwo = (Sign) cornerTwo.getState();
		// north east
		Sign signThree = (Sign) cornerThree.getState();
		// south west
		Sign signFour = (Sign) cornerFour.getState();
		
		signOne.setRawData((byte) 0x6);
		signTwo.setRawData((byte) 0xE);
		signThree.setRawData((byte) 0x2);
		signFour.setRawData((byte) 0xA);
		
		signOne.setLine(1, plugin.config.getString(Config.OWNER_PREFIX));
		signOne.setLine(2, this.getAdmin().getName());
		signTwo.setLine(1, plugin.config.getString(Config.OWNER_PREFIX));
		signTwo.setLine(2, this.getAdmin().getName());
		signThree.setLine(1, plugin.config.getString(Config.OWNER_PREFIX));
		signThree.setLine(2, this.getAdmin().getName());
		signFour.setLine(1, plugin.config.getString(Config.OWNER_PREFIX));
		signFour.setLine(2, this.getAdmin().getName());
		
		signOne.update();
		signTwo.update();
		signThree.update();
		signFour.update();
	}
	
	/**
	 * Removes the signs from the plot corners.
	 */
	public void removeSigns(){
		int[] buildLimits = this.getBuildLimits();
		int x3 = buildLimits[0];
		int z3 = buildLimits[1] + (this.size - 7);
		int x4 = buildLimits[2];
		int z4 = buildLimits[3] - (this.size - 7);
		int y = plugin.config.getInt(Config.GRID_HEIGHT);
		World world = plugin.getServer().getWorld(this.getLocation().getWorldName());
		
		if (plugin.config.getInt(Config.BLOCKS_UPPER_WALL) == 0){
			y += 1;
		}else{
			y += 2;
		}
		
		Block cornerOne = world.getBlockAt(buildLimits[0] - 1, y, buildLimits[1] - 1);
		Block cornerTwo = world.getBlockAt(buildLimits[2] + 1, y, buildLimits[3] + 1);
		Block cornerThree = world.getBlockAt(x3 - 1, y, z3);
		Block cornerFour = world.getBlockAt(x4 + 1, y, z4);
		
		cornerOne.setType(Material.AIR);
		cornerTwo.setType(Material.AIR);
		cornerThree.setType(Material.AIR);
		cornerFour.setType(Material.AIR);
	}
	
}
