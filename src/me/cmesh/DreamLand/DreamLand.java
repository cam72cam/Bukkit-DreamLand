package me.cmesh.DreamLand;

import java.util.ArrayList;
import java.util.Arrays;
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
	public static PermissionHandler Permissions = null;
	
	public HashMap<String, Location> Beds = new HashMap<String, Location>();
	public HashMap<String, Integer> Respawn = new HashMap<String, Integer>();
	public HashMap<String, Long> Attempt = new HashMap<String, Long>();
	
	public Boolean anyoneCanGo = true;
	public Boolean usingpermissions = false;
	public Integer dreamChance = 100;
	public Boolean dreamFly = true;
	public String flyTool = "288";
	public Boolean seperateInv = false;
	public Boolean seperateInvInitial = true;
	public List<String> kit = Arrays.asList("0 288 1 0");
	public Double flySpeed = 1.0;
	public Boolean dreamInvincible;
	public Integer attemptWait = 0;
	public String message = "";
	public String dreamWorld = "world_skylands";
	public String nightmareWorld = "world_nightmare";
	public Integer nightmareChance = 50;
	public Boolean morningReturn = true;
	
	public void onEnable()
	{ 
		Respawn.clear();
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
			if (permissions != null)
			{
				Permissions = ((Permissions)permissions).getHandler();
				log.info(getDescription().getName()+" version "+getDescription().getVersion()+" is enabled with permissions!");
				usingpermissions = true;
			}
			else
			{
				log.info(getDescription().getName()+" version "+getDescription().getVersion()+" is enabled without permissions!");
				usingpermissions = false;
			}
		}

		reload();
		
		// Load Nightmare world
		if(nightmareChance != 0)
		{
			getServer().createWorld(nightmareWorld, Environment.NETHER, getServer().getWorlds().get(0).getSeed());
			loadChunk(nightmareWorld().getSpawnLocation());
		}
		
		// Load DreamWorld
		getServer().createWorld(dreamWorld,Environment.SKYLANDS,getServer().getWorlds().get(0).getSeed());
		loadChunk(dreamWorld().getSpawnLocation());
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		if (commandLabel.equalsIgnoreCase("setdreamspawn")) 
		{
			if (sender instanceof Player) 
			{
				Player player = (Player)sender;
				if(playerListener.playerDreaming(player) && checkPermissions(player, "dreamland.setdreamspawn", true))
				{
					Location location = player.getLocation();
					player.getWorld().setSpawnLocation((int)location.getX(), (int)location.getY(), (int)location.getZ());
					player.sendMessage("Spawn set to: " + (int)location.getX() + "x," + (int)location.getY() + "y," + (int)location.getZ() + "z");
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

	public boolean checkPermissions(Player player, String string, Boolean standard)
	{
		return (player.isOp() == true) || (usingpermissions ? Permissions.has(player,string) : standard);
	}
	
	public void reload()
	{
		getConfiguration().load();
		
		dreamWorld = getConfiguration().getString("dreamland.worlds.dream",getServer().getWorlds().get(0).getName()+"_skylands");
		nightmareWorld = getConfiguration().getString("dreamland.worlds.nightmare",getServer().getWorlds().get(0).getName()+"_nightmare");

		dreamChance = getConfiguration().getInt("dreamland.chance.dream",100);
		nightmareChance = getConfiguration().getInt("dreamland.chance.nightmare",50);
		
		//options
		anyoneCanGo = getConfiguration().getBoolean("dreamland.options.allowAll",true);
		attemptWait = getConfiguration().getInt("dreamland.options.attemptWait", 0);
		attemptWait *= 30;		
		message = getConfiguration().getString("dreamland.options.message", "");
		morningReturn = getConfiguration().getBoolean("dreamland.options.wakeup", true);
		dreamInvincible = getConfiguration().getBoolean("dreamland.options.invincible", true);
		
		//fly
		dreamFly = getConfiguration().getBoolean("dreamland.fly.enable",true);
		flySpeed = getConfiguration().getDouble("dreamland.fly.speed", 1.0);
		flyTool = getConfiguration().getString("dreamland.fly.tool","288");
		
		//inventory
		seperateInv = getConfiguration().getBoolean("dreamland.inventory.seperate", true  );
		seperateInvInitial = getConfiguration().getBoolean("dreamland.inventory.seperateInitial", true);
		
		List<String> kitTemp = Arrays.asList(
				getConfiguration().getString("dreamland.inventory.kit.1", "288 1"),
				getConfiguration().getString("dreamland.inventory.kit.2", ""),
				getConfiguration().getString("dreamland.inventory.kit.3", "")
				);
		
		int count =0;
		kit = new ArrayList<String>();
		for(String item : kitTemp)
		{
			if(!item.equals(""))
				kit.add(count + " " + item + " 0");
			count ++;
		}
		
		getConfiguration().save();
	}

	public World dreamWorld()
	{
		return getServer().getWorld(dreamWorld);
	}
	public World nightmareWorld()
	{
		return getServer().getWorld(nightmareWorld);
	}
	
	public void loadChunk(Location location)
	{
		Chunk chunk = location.getBlock().getChunk();
		location.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
	}
}
