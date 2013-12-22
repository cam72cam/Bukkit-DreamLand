package me.cmesh.DreamLand;

import java.io.*;
import java.util.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DreamPlayerState {
	private ItemStack[] inventory;
	private ItemStack[] armor;
	private double health;
	private int food;
	private double enchant;
	private double exhaustion;
	private double saturation;
	private Location location;
	
	private File file;
	private DreamPlayer player;
	private DreamWorld world;
	
	public DreamPlayerState(DreamPlayer dlPlayer, DreamWorld world) {
		this.player = dlPlayer;
		this.world = world;
		
		file = new File(player.getPlayerFile().getAbsolutePath() + "." + world.getName());
		location = world.getDreamSpawn();
	}
	
	private void serializeLocation(ConfigurationSection config, String path, Location loc) {
		List<String> locRaw = new ArrayList<String>();
		locRaw.add(location.getX() + "");
		locRaw.add(location.getY() + "");
		locRaw.add(location.getZ() + "");
		config.set(path, locRaw);
	}
	
	private Location deserializeLocation(ConfigurationSection config, String path) {
		if(config.contains(path)) {
			List<String> locRaw = config.getStringList(path);
			return new Location(world.self(), Double.parseDouble(locRaw.get(0)),Double.parseDouble(locRaw.get(1)),Double.parseDouble(locRaw.get(2)));
		}
		return null;
		
	}
	
	private void serializeItems(ConfigurationSection section, ItemStack [] items) {
		for(int i = 0; i < items.length; i++)
		{
			ItemStack item = items[i];
			section.set(i + "", item == null ? new ItemStack(Material.AIR) : item);
		}
	}
	private void deserializeItems(ConfigurationSection section, ItemStack [] items) {
		for(String key : section.getKeys(false)) {
			try {
				int index = Integer.parseInt(key);
				items[index] = section.getItemStack(key);
			} catch (NumberFormatException ex) {
				//Bukkit YAML is a piece of crap
				//It reads into the next configuration section when reading an section
			}
		}
	}

	private final static String inventorySection = "inventory";
	private final static String armorSection = "inventory.armor";
	private final static String mainSection = "main";
	
	private final static String healthConfig = "health";
	private final static String foodConfig = "food";
	private final static String enchantConfig = "enchant";
	private final static String exhaustionConfig = "exhaustion";
	private final static String saturationConfig = "saturation";
	private final static String locationConfig = "location";
	
	private void serialize() {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		ConfigurationSection main = config.createSection(mainSection);
		main.set(healthConfig, health);
		main.set(foodConfig, food);
		main.set(enchantConfig, enchant);
		main.set(exhaustionConfig, exhaustion);
		main.set(saturationConfig, saturation);
		serializeLocation(main, locationConfig, location);
		
		serializeItems(config.createSection(inventorySection), inventory);
		serializeItems(config.createSection(armorSection), armor);
		
		try {
			config.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void deserialize() {
		if (exists()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);

			ConfigurationSection main = config.getConfigurationSection(mainSection);
			health = main.getDouble(healthConfig);
			food = main.getInt(foodConfig);
			enchant = main.getDouble(enchantConfig); //TODO bug report and testing
			exhaustion = main.getDouble(exhaustionConfig);
			saturation = main.getDouble(saturationConfig);
			Location possibleLoc = deserializeLocation(main, locationConfig);
			if (possibleLoc != null) {
				location = possibleLoc;
			}
			
			inventory = new ItemStack[player.getSelf().getInventory().getContents().length];
			armor = new ItemStack[player.getSelf().getInventory().getArmorContents().length];
			deserializeItems(config.getConfigurationSection(inventorySection), inventory);
			deserializeItems(config.getConfigurationSection(armorSection), armor);
		}
	}
	
	private boolean inMemory = false;
	
	public void save(Location loc)
	{
		location = loc;
		
		Player player = this.player.getSelf();
		DreamLandPlugin.log.info("Saving " + player.getName() + " " + world.getName());
		health = player.getHealth();
		food = player.getFoodLevel();
		enchant = player.getExp();
		exhaustion = player.getExhaustion();
		saturation = player.getSaturation();
		if(world.persistInventory()) {
			inventory = player.getInventory().getContents();
			armor = player.getInventory().getArmorContents();
		}
		serialize();
		inMemory = true;
	}
	
	private boolean exists() {
		return file.exists();
	}
	
	public void load()
	{
		Player player = this.player.getSelf();
		player.getInventory().clear();
		
		if (exists()) {
			DreamLandPlugin.log.info("loading " + player.getName() + " " + world.getName());
			if (! inMemory) {
				deserialize();
				inMemory = true;
			}
			player.setHealth(health);
			player.setFoodLevel(food);
			player.setExp((float) enchant);
			player.setExhaustion((float) exhaustion);
			player.setSaturation((float) saturation);
	
			if(world.persistInventory()) {
				player.getInventory().setContents(inventory);
				player.getInventory().setArmorContents(armor);
			}
		}
		player.sendMessage(location.toString());
		player.teleport(location);
	}
}
