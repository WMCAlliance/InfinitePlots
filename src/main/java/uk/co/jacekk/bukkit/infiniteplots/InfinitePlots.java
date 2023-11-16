package uk.co.jacekk.bukkit.infiniteplots;

import java.io.File;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.RegisteredServiceProvider;

import uk.co.jacekk.bukkit.baseplugin.BasePlugin;
import uk.co.jacekk.bukkit.baseplugin.config.PluginConfig;
import uk.co.jacekk.bukkit.infiniteplots.command.AddBuilderCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.AutoCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.ClaimCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.DecorateCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.FlagCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.InfoCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.ListCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.NameCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.PlotCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.ProtectionCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.PurgeCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.ResetCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.SetBiomeCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.command.TeleportCommandExecutor;
import uk.co.jacekk.bukkit.infiniteplots.flag.BlockFlowListener;
import uk.co.jacekk.bukkit.infiniteplots.flag.IceListener;
import uk.co.jacekk.bukkit.infiniteplots.flag.MobSpawnListener;
import uk.co.jacekk.bukkit.infiniteplots.flag.PhysicsListener;
import uk.co.jacekk.bukkit.infiniteplots.generation.PlotsGenerator;
import uk.co.jacekk.bukkit.infiniteplots.plot.PlotManager;
import uk.co.jacekk.bukkit.infiniteplots.protection.BuildListener;
import uk.co.jacekk.bukkit.infiniteplots.protection.EnterListener;

public class InfinitePlots extends BasePlugin {
	
	private static InfinitePlots instance;
	
	private File plotsDir;
	private PlotManager plotManager;
	private Economy economy;
	
	@Override
	public void onEnable(){
		super.onEnable(true);
		
		instance = this;
		
		this.plotsDir = new File(this.baseDirPath + File.separator + "plots");
		
		if (!this.plotsDir.exists()){
			this.plotsDir.mkdirs();
		}
		
		this.config = new PluginConfig(new File(this.baseDirPath + File.separator + "config.yml"), Config.class, this.log);
		
		if (!this.config.getBoolean(Config.GENERATOR_ONLY)){
			this.plotManager = new PlotManager(this);
			
			for (World world : this.getServer().getWorlds()){
				this.plotManager.loadPlotsFor(world);
			}
			
			this.getPermissionManager().registerPermissions(Permission.class);
			
			this.getServer().getPluginManager().registerEvents(new BuildListener(this), this);
			this.getServer().getPluginManager().registerEvents(new EnterListener(this), this);
			
			this.getServer().getPluginManager().registerEvents(new MobSpawnListener(this), this);
			this.getServer().getPluginManager().registerEvents(new BlockFlowListener(this), this);
			this.getServer().getPluginManager().registerEvents(new IceListener(this), this);
			this.getServer().getPluginManager().registerEvents(new PhysicsListener(this), this);
			
			if (this.config.getBoolean(Config.TRACK_STATS)){
				this.getServer().getPluginManager().registerEvents(new PlotStatsListener(this), this);
			}
			
			if (this.config.getDouble(Config.CLAIM_COST) > 0.0d){
				RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
				
				if (economyProvider == null){
					this.log.warn("Vault not found, players will not be charged to claim plots.");
					this.log.warn("Download it from http://dev.bukkit.org/bukkit-plugins/vault/");
				}else{
					this.economy = economyProvider.getProvider();
				}
			}
			
			this.getCommandManager().registerCommandExecutor(new PlotCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new InfoCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new ClaimCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new AutoCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new AddBuilderCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new FlagCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new SetBiomeCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new ResetCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new ListCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new NameCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new TeleportCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new DecorateCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new ProtectionCommandExecutor(this));
			this.getCommandManager().registerCommandExecutor(new PurgeCommandExecutor(this));
		}
	}
	
	@Override
	public void onDisable(){
		instance = null;
	}
	
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id){
		int size = this.config.getInt(Config.GRID_SIZE);
		
		if (id != null && !id.isEmpty()){
			try{
				size = Integer.parseInt(id);
			}catch(NumberFormatException e){  }
		}
		
		int height = this.config.getInt(Config.GRID_HEIGHT);
		
		String pathId = this.config.getString(Config.BLOCKS_PATH);
		String wallLowerId = this.config.getString(Config.BLOCKS_LOWER_WALL);
		String wallUpperId = this.config.getString(Config.BLOCKS_UPPER_WALL);
		String surfaceId = this.config.getString(Config.BLOCKS_SURFACE);
		String groundId = this.config.getString(Config.BLOCKS_GROUND);
		
		return new PlotsGenerator(size, height, Material.getMaterial(pathId), Material.getMaterial(wallLowerId), Material.getMaterial(wallUpperId), Material.getMaterial(surfaceId), Material.getMaterial(groundId));
	}
	
	public static InfinitePlots getInstance(){
		return instance;
	}
	
	/**
	 * Gets the folder used for plot data files.
	 * 
	 * @return The {@link File} for the folder.
	 */
	public File getPlotsDir(){
		return this.plotsDir;
	}
	
	/**
	 * Gets the plot manager.
	 * 
	 * @return The {@link PlotManager}.
	 */
	public PlotManager getPlotManager(){
		return this.plotManager;
	}
	
	/**
	 * Gets the economy instance.
	 * 
	 * @return The instance.
	 */
	public Economy getEconomy(){
		return this.economy;
	}
	
}