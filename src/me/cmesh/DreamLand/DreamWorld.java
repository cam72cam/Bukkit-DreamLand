package me.cmesh.DreamLand;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.world.PortalCreateEvent;

public class DreamWorld {

	public boolean allowPortals;
	private FileConfiguration worldConfig;
	private World self;

	private List<DreamWorld> nextWorlds = new ArrayList<DreamWorld>();
	private Map<String, Integer> chanceMap = new HashMap<String, Integer>();
	
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
		ConfigurationSection nextWorldConfig = worldConfig.getConfigurationSection(settingNextWorlds);
		if(nextWorldConfig != null) {
			for(String worldName : nextWorldConfig.getKeys(false)) {
				chanceMap.put(worldName, nextWorldConfig.getInt(worldName));
				
				DreamWorld world = DreamLandPlugin.Instance.worlds.getWorld(worldName);
				if (world == null) {
					world = DreamLandPlugin.Instance.worlds.createWorld(worldName);
				}
				nextWorlds.add(world);
			}
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
		Integer total = 0;
		for(Integer chance : chanceMap.values()) {
			total += chance;
		}
		
		
		Random randy = new Random();
		Integer value = randy.nextInt(total);
		
		DreamWorld next = null;
		
		Integer current = 0;
		for(DreamWorld w : nextWorlds) {
			Integer chance = chanceMap.get(w.getName());
			if(value >= current && value < current + chance) {
				next = w;
				break;
			}
			current += chance;
		}
		
		// TODO factor in random chance
		return next;
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
