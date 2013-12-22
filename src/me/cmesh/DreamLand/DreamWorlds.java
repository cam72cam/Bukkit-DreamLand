package me.cmesh.DreamLand;

import java.io.File;
import java.util.*;

import org.bukkit.World;

public class DreamWorlds {
	private HashMap<String, DreamWorld> worlds = new HashMap<String, DreamWorld>();
	
	public static File getWorldFolder() {
		File folder = new File(DreamLandPlugin.Instance.DreamPath + File.separatorChar + "Worlds");
		
		if (! folder.exists()) {
			folder.mkdir();
		}
		return folder;
	}

	public void Load() {
		for (File file : getWorldFolder().listFiles()) {
			getWorld(file.getName());
		}
	}
	
	public void Save() {
		for(DreamWorld world : worlds.values()) {
			world.Save();
		}
	}
	
	public DreamWorld createWorld(String worldName) {
		if (! worlds.containsKey(worldName)) {
			DreamWorld world = new DreamWorld(worldName);
			worlds.put(worldName, world);
			return world;
		}
		return null;
	}
	
	public DreamWorld getWorld(String worldName) {
		return worlds.containsKey(worldName) ? worlds.get(worldName) : null;
	}
	
	public DreamWorld getWorld(World world) {
		return getWorld(world.getName());
	}
}
