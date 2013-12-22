package me.cmesh.DreamLand;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

public class DreamPlayers {
	
	private  HashMap<UUID, DreamPlayer> self = new HashMap<UUID, DreamPlayer> (); 
	
	public void add(Player player) {
		if(!self.containsKey(player.getUniqueId())) {
			DreamLandPlugin.log.info("Adding player " + player.getName());
			DreamPlayer dlPlayer = new DreamPlayer(player);
			self.put(player.getUniqueId(), dlPlayer);
		}
	}
	
	public DreamPlayer get(Player player) {
		return self.get(player.getUniqueId());
	}
	
	public void Load() {		
		for(Player player : DreamLandPlugin.Instance.getServer().getOnlinePlayers()) {
			add(player);
		}
	}
	
	public void Save() {
		for(DreamPlayer player : self.values()) {
			player.Save();
		}
	}
}
