package me.cmesh.DreamLand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DreamLand extends JavaPlugin
{
	public static final Logger log = Logger.getLogger("Minecraft");
	private final DreamLandPlayerListener playerListener = new DreamLandPlayerListener(this);
	private final DreamLandEntityListener entityListener = new DreamLandEntityListener(this);
	private final DreamLandWeatherListener weatherListener = new DreamLandWeatherListener(this);
	
	public DreamLandWorld nightmare = new DreamLandWorld(this);
	public DreamLandWorld dream = new DreamLandWorld(this);
	public DreamLandWorld base = new DreamLandWorld(this);
	
	public static PermissionHandler Permissions = null;
	
	public Boolean anyoneCanGo = true;
	public Boolean usingpermissions = false;
	public Integer flyTool = 288;
	public List<String> kit = new ArrayList<String>();
	public Double flySpeed = 1.0;
	public Integer attemptWait = 0;
	public String message = "";
	public Boolean morningReturn = true;
	public Boolean weatherDisable = false;
	
	private HashMap<String, DreamLandPlayer> Players= new HashMap<String, DreamLandPlayer>(); 
	
	public void onEnable()
	{ 
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
		
		pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.High, this);
		
		pm.registerEvent(Event.Type.WEATHER_CHANGE, weatherListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.THUNDER_CHANGE, weatherListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.LIGHTNING_STRIKE, weatherListener, Event.Priority.High, this);
		
		Plugin permissions = getServer().getPluginManager().getPlugin("Permissions");
		if (Permissions == null)
		{
			Permissions = (permissions != null) ? ((Permissions)permissions).getHandler() : null;
		}
		
		reload();
		
		// Load Nightmare world
		if(nightmare.Chance != 0)
		{
			getServer().createWorld(nightmare.World, Environment.NETHER, getServer().getWorlds().get(0).getSeed());
			loadChunk(nightmare.getWorld().getSpawnLocation());
		}
		
		// Load DreamWorld
		getServer().createWorld(dream.World,Environment.SKYLANDS,getServer().getWorlds().get(0).getSeed());
		loadChunk(dream.getWorld().getSpawnLocation());
		
		
		log.info(getDescription().getName()+" version "+getDescription().getVersion()+" is enabled!");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		if (commandLabel.equalsIgnoreCase("setdreamspawn")) 
		{
			if (sender instanceof Player) 
			{
				Player player = (Player)sender;
				DreamLandPlayer playerInfo = getPlayer(player);
				if(playerInfo.Dreaming() && checkPermissions(player, "dreamland.setdreamspawn", true))
				{
					Location location = player.getLocation();
					player.getWorld().setSpawnLocation((int)location.getX(), (int)location.getY(), (int)location.getZ());
					player.sendMessage("Spawn set to: " + (int)location.getX() + "x," + (int)location.getY() + "y," + (int)location.getZ() + "z");
					return true;
				}
			}
		}
		if (commandLabel.equalsIgnoreCase("wakeup")) 
		{
			if (sender instanceof Player) 
			{
				Player player = (Player)sender;
				DreamLandPlayer playerInfo = getPlayer(player);
				if(playerInfo.Dreaming())
				{
					playerInfo.leaveDream();
					return true;
				}
			}
		}
		return false;
	}
	
	public void onDisable()
	{
		log.info(getDescription().getName()+" version "+getDescription().getVersion()+" is disabled!");
	}

	public boolean checkPermissions(Player player, String permission, Boolean standard)
	{
		return (player.isOp() == true) || player.hasPermission(permission) || (usingpermissions ? Permissions.has(player,permission) : standard);
	}
	
	public void reload()
	{
		getConfiguration().load();
		
		dream.World = getConfiguration().getString("dreamland.worlds.dream.name",getServer().getWorlds().get(0).getName()+"_skylands");
		dream.PersistInventory = getConfiguration().getBoolean("dreamland.worlds.dream.persistInventory",true);
		dream.InitialInventoryClear = getConfiguration().getBoolean("dreamland.worlds.dream.clearInitialInventory", true);
		dream.Invincible = getConfiguration().getBoolean("dreamland.worlds.dream.invincible", true);
		dream.Fly = getConfiguration().getBoolean("dreamland.worlds.dream.fly", true);
		dream.Flaming = getConfiguration().getBoolean("dreamland.worlds.dream.flaming", false);
		dream.Kit = getConfiguration().getBoolean("dreamland.worlds.dream.kit", true);
		dream.Chance = getConfiguration().getInt("dreamland.chance.dream",100);
		dream.ReturnToBed = getConfiguration().getBoolean("dreamland.worlds.dream.returnToBed", true);

		dream.MobChance = getConfiguration().getInt("dreamland.worlds.dream.mobChance",0);
		try
		{
			for(String node : getConfiguration().getKeys("dreamland.worlds.dream.mobs"))
			{
				if(getConfiguration().getBoolean("dreamland.worlds.dream.mobs."+node, true))
				{
					dream.Mobs.add(node);
				}
			}
		}
		catch (java.lang.NullPointerException e){}
		
		nightmare.World = getConfiguration().getString("dreamland.worlds.nightmare.name",getServer().getWorlds().get(0).getName()+"_nightmare");
		nightmare.PersistInventory = getConfiguration().getBoolean("dreamland.worlds.nightmare.persistInventory",false);
		nightmare.InitialInventoryClear = getConfiguration().getBoolean("dreamland.worlds.nightmare.clearInitialInventory", true);
		nightmare.Invincible = getConfiguration().getBoolean("dreamland.worlds.nightmare.invincible", false);
		nightmare.Fly = getConfiguration().getBoolean("dreamland.worlds.nightmare.fly", false);
		nightmare.Flaming = getConfiguration().getBoolean("dreamland.worlds.nightmare.flaming", true);
		nightmare.Kit = getConfiguration().getBoolean("dreamland.worlds.nightmare.kit", false);
		nightmare.Chance = getConfiguration().getInt("dreamland.chance.nightmare",50);
		nightmare.ReturnToBed = getConfiguration().getBoolean("dreamland.worlds.nightmare.returnToBed", true);
		
		nightmare.MobChance = getConfiguration().getInt("dreamland.worlds.nightmare.mobChance",0);
		try
		{
			for(String node : getConfiguration().getKeys("dreamland.worlds.nightmare.mobs"))
			{
				if(getConfiguration().getBoolean("dreamland.worlds.nightmare.mobs."+node, true))
				{
					nightmare.Mobs.add(node);
				}
			}
		}
		catch (java.lang.NullPointerException e){}
		
		base.PersistInventory = getConfiguration().getBoolean("dreamland.worlds.default.persistInventory",true);
		
		//options
		anyoneCanGo = getConfiguration().getBoolean("dreamland.options.allowAll",true);
		attemptWait = getConfiguration().getInt("dreamland.options.attemptWait", 0) * 30;
		message = getConfiguration().getString("dreamland.options.message", "");
		morningReturn = getConfiguration().getBoolean("dreamland.options.wakeup", true);
		weatherDisable = getConfiguration().getBoolean("dreamland.options.weatherDisable", false);
		
		//fly
		flySpeed = getConfiguration().getDouble("dreamland.fly.speed", 1.0);
		flyTool = getConfiguration().getInt("dreamland.fly.tool",288);
		
		//inventory
		try
		{
			for(String node : getConfiguration().getKeys("dreamland.inventory.kit"))
			{
				kit.add(node + " " + getConfiguration().getString("dreamland.inventory.kit."+ node, "288 1") + " 0");
			}
		}
		catch (java.lang.NullPointerException e){}
		
		getConfiguration().save();
	}
	
	public DreamLandWorld getSetting(World world)
	{
		if(world.equals(dream.getWorld()))
		{
			return dream;
		}
		if(world.equals(nightmare.getWorld()))
		{
			return nightmare;
		}
		return base;
	}
	

	public void loadChunk(Location location)
	{
		Chunk chunk = location.getBlock().getChunk();
		location.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
	}
	
	public void createPlayer(Player player)
	{
		Players.put(player.getName(), new DreamLandPlayer(this).Update(player));
	}
	
	public DreamLandPlayer getPlayer(Player player)
	{
		String key = player.getName();
		if(Players.containsKey(key))
		{
			return Players.get(key).Update(player);
		}
		log.info("Issue Loading Player");
		createPlayer(player);
		return getPlayer(player);
	}
}
