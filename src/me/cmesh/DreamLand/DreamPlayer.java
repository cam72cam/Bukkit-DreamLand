package me.cmesh.DreamLand;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Location;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

public class DreamPlayer {
	
	private final Player self;
	private final Stack<DreamWorld> worlds = new Stack<DreamWorld>();
	private final HashMap<DreamWorld, DreamPlayerState> states = new HashMap<DreamWorld, DreamPlayerState>();
	
	private DreamWorld baseWorld = null;
	
	public File getPlayerFile() {
		File playersDir =  new File(DreamLandPlugin.Instance.DreamPath + File.separatorChar + "Players");
		if (! playersDir.exists()) {
			playersDir.mkdir();
		}
		return new File(playersDir.getAbsolutePath() + File.separatorChar + self.getName());
	}
	
	private DreamWorld getCurrentWorld() {
		return worlds.empty() ? baseWorld : worlds.peek();
	}
	
	private DreamPlayerState getWorldState(DreamWorld world) {
		if (! states.containsKey(world)) {
			states.put(world, new DreamPlayerState(this, world));
		}
		return states.get(world);
	}
	
	private void fromWorld(DreamWorld world, Location loc) {
		getWorldState(world).save(loc);
	}
	
	private void toWorld(DreamWorld world) {
		getWorldState(world).load();
	}
	
	private void enterDream() {
		DreamWorld current = getCurrentWorld();
		if (current != null && current.hasNext()) {
			self.sendMessage("Entering dream!");
			self.sendMessage(worlds.toString());
			
			DreamWorld next = current.chooseNextWorld();
			
			fromWorld(current, self.getLocation());
			worlds.push(next);
			toWorld(next);
		} else {
			self.sendMessage("BaseWorld" + baseWorld.getName());
			self.sendMessage(worlds.toString());
		}
	}
	
	private void leaveDream(boolean saveLoc) {
		if (hasPrevWorld()) {
			self.sendMessage("Leaving dream!");
			self.sendMessage(worlds.toString());
			DreamWorld current = worlds.pop();
			DreamWorld prev = getCurrentWorld(); 
			
			if ( ! saveLoc ) {
				fromWorld(current, current.getDreamSpawn());
			} else {
				fromWorld(current, self.getLocation());
			}
			
			
			toWorld(prev);
		} else {
			self.sendMessage("You are not dreaming? Or are you!");
		}
	}
	
	private boolean hasPrevWorld() {
		return ! worlds.empty();
	}

	public DreamPlayer(Player player) {
		self = player;
		//TODO deserialize file
		Load();
		
		if (baseWorld == null) {
			DreamLandPlugin.log.info("Trying to start " + self.getName() + " in world " + self.getLocation().getWorld().getName());
			baseWorld = DreamLandPlugin.Instance.worlds.getWorld(self.getLocation().getWorld());
			if (baseWorld != null) {
				DreamLandPlugin.log.info("Starting " + self.getName() + " in world " + baseWorld.getName());
			}
		}
	}
	
	private final String worldsConfig = "worlds";
	private final String baseWorldConfig = "baseWorld";
	
	private void Load() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(getPlayerFile());

		List<String> worlds = config.getStringList(worldsConfig);
		for(String worldName : worlds) {
			//What happens when the player is in a world no longer in config?
			DreamWorld world = DreamLandPlugin.Instance.worlds.getWorld(worldName);
			this.worlds.push(world);
		}
		
		if(config.contains(baseWorldConfig)) {
			baseWorld = DreamLandPlugin.Instance.worlds.getWorld(config.getString(baseWorldConfig));
		}
	}
	
	public void Save() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(getPlayerFile());
		
		List<String> worlds = new ArrayList<String>();
		for(DreamWorld world : this.worlds) {
			worlds.add(world.getName());
		}
		config.set(worldsConfig, worlds);
		
		if(baseWorld != null) {
			config.set(baseWorldConfig, baseWorld.getName());
		}
		
		try {
			config.save(getPlayerFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onDamageEvent(EntityDamageEvent event) {
		if (getCurrentWorld().isPlayerInvincible()) {
			event.setCancelled(true);
		}
		
	}

	public void onMoveEvent(PlayerMoveEvent event) {
		if (hasPrevWorld() && getCurrentWorld().isMorning()) {
			self.sendMessage("It was all just a dream");
			leaveDream(true);
		}
	}
	
	private Integer dreamDelay = null;
	public void onBedEnterEvent(final PlayerBedEnterEvent event) {
		DreamLandPlugin.log.info("SLEEP!");
		DreamWorld world = getCurrentWorld();
		self.sendMessage("" + (world != null));
		if (world != null && world.hasNext()) {
			Runnable task = new Runnable() 
	        {
				@Override
	        	public void run()
	        	{
					try {
						event.setCancelled(true);
						
						enterDream();
						dreamDelay = null;
					}
					catch (Exception e) {
						//We were canceled (player has left the bed)!
						e.printStackTrace();
						self.sendMessage("You wake up dazed and confuzed!");
						self.leaveVehicle();
					}
		    	}
	    	};
	    	
	    	dreamDelay = DreamLandPlugin.Instance.getServer().getScheduler().scheduleSyncDelayedTask(DreamLandPlugin.Instance, task, 60L);
		}
	}
	
	public void onBedLeaveEvent(PlayerBedLeaveEvent event) {
		if (dreamDelay != null) {
			DreamLandPlugin.Instance.getServer().getScheduler().cancelTask(dreamDelay);
		}
	}

	public Player getSelf() {
		return self;
	}
}
