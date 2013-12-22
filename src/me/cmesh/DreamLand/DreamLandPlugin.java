package me.cmesh.DreamLand;

import java.io.File;
import java.util.logging.Logger;

import me.cmesh.DreamLand.Listeners.DreamPlayerListener;

import org.bukkit.plugin.java.JavaPlugin;

public class DreamLandPlugin extends JavaPlugin
{
	public static final Logger log = Logger.getLogger("Minecraft");
	public static DreamLandPlugin Instance = null;
    
    public DreamWorlds worlds;
    public DreamPlayers players;
	public DreamLandOptions options;
	
	
	public String DreamPath;
	
	public DreamLandPlugin() {
		Instance = this;
		
		options = new DreamLandOptions();
		
		worlds = new DreamWorlds();
		players = new DreamPlayers();
		
		
	}
	
	private DreamPlayerListener playerListener;
	
	@Override
	public void onEnable() {
		SetupFolder();
		
		//options.Load()
		
		worlds.Load();
		
		worlds.createWorld(getServer().getWorlds().get(0).getName());

		players.Load();
		
		playerListener = new DreamPlayerListener();
        this.getServer().getPluginManager().registerEvents(playerListener, this);
	}
	
	@Override
	public void onDisable() {
		players.Save();
		worlds.Save();
	}
	
	private void SetupFolder() {
		File folder = new File(this.getDataFolder().getAbsolutePath());
		
		if (! folder.isDirectory()) {
			if(folder.exists()) {
				folder.delete();
			}
			folder.mkdir();
		}
		DreamPath = folder.getAbsolutePath();
	}
}
