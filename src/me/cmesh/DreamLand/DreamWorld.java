package me.cmesh.DreamLand;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.world.PortalCreateEvent;

public class DreamWorld {

	public boolean allowPortals;
	private FileConfiguration worldConfig;
	private World self;

	private List<DreamWorld> nextWorlds = new ArrayList<DreamWorld>(); 
	
	public DreamWorld(String worldName) {

		File folder = new File(DreamLandPlugin.Instance.DreamPath + File.separatorChar + "Worlds");
		DreamLandPlugin.log.info("Creating World " + worldName);
		if (! folder.exists()) {
			folder.mkdir();
			DreamLandPlugin.log.info("Creating Folder " + folder.getAbsolutePath());
		}
		File worldFile = new File(folder.getAbsolutePath() + File.separatorChar + worldName);
		if (! worldFile.exists()) {
			try {
				DreamLandPlugin.log.info("Creating World Config " + worldFile.getAbsolutePath());
				worldFile.createNewFile();
				Save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		worldConfig = YamlConfiguration.loadConfiguration(worldFile);
		
		Load();

		
		self = DreamLandPlugin.Instance.getServer().getWorld(worldName);
		if (self == null) {
			self = DreamLandPlugin.Instance.getServer().createWorld(new WorldCreator(worldName));
			DreamLandPlugin.log.info("Creating new World " + worldName);
		}
	}
	
	
	private final String settingNextWorlds="nextWorlds";
	
	public void Save() {
		//TODO worldConfig.
		//worldConfig.addDefault(settingNextWorlds, 0);
	}
	
	public void Load() {
		List<String> next = worldConfig.getStringList(settingNextWorlds);
		for(String w : next) {
			DreamLandPlugin.log.info("Start " + w);
			DreamWorld world = DreamLandPlugin.Instance.worlds.getWorld(w);
			if (world == null) {
				world = DreamLandPlugin.Instance.worlds.createWorld(w);
			}
			nextWorlds.add(world);
		}
	}
	
	public void onPortalCreateEvent(PortalCreateEvent event) {
		if(allowPortals) {
			event.setCancelled(true);
		}
	}
	
	public boolean hasNext() {
		return ! nextWorlds.isEmpty();
	}

	public DreamWorld chooseNextWorld() {
		// TODO factor in random chance
		return nextWorlds.get(0);
	}

	public Location getDreamSpawn() {
		return self.getSpawnLocation();
	}

	public boolean isMorning() {
		return self.getTime() > 0 && self.getTime() < 1400;
	}

	public boolean isPlayerInvincible() {
		return false;
	}
	
	public String getName() {
		return self.getName();
	}

	public boolean persistInventory() {
		return true;
	}

	public World self() {
		return self;
	}
}
