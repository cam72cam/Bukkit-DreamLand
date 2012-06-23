package me.cmesh.DreamLand;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.nijiko.permissions.PermissionHandler;

public class DreamLand extends JavaPlugin
{
	public static final Logger log = Logger.getLogger("Minecraft");

	public DreamLandWorld nightmare = new DreamLandWorld(this);
	public DreamLandWorld dream = new DreamLandWorld(this);
	public DreamLandWorld base = new DreamLandWorld(this);
	
    public PermissionHandler permissionsPlugin = null;
    public PermissionManager permissionsExPlugin = null;
	
	public DreamLandOptions options = new DreamLandOptions(this);
	
	private HashMap<String, DreamLandPlayer> Players= new HashMap<String, DreamLandPlayer>(); 
	
	private Scheduler scheduler = new Scheduler(this);
	
	public void onEnable()
	{
		reload();
		
		SetupPermissions();
		
		if(nightmare.Chance != 0)
		{
			nightmare.create();
		}
		dream.create();
		
		scheduler.Start();

		new DreamLandPlayerListener(this);
		new DreamLandEntityListener(this);
		new DreamLandWeatherListener(this);
	}
	
	public boolean PermissionEnabled()
	{
		return permissionsPlugin != null;
	}
	public boolean PermissionExEnabled()
	{
		return permissionsExPlugin != null;
	}
	
	private void SetupPermissions()
	{
		Plugin p = getServer().getPluginManager().getPlugin("Permissions");
		if (p != null && p.isEnabled()) 
		{
			permissionsPlugin = ((com.nijikokun.bukkit.Permissions.Permissions)p).getHandler();
		}
		
		Plugin q = getServer().getPluginManager().getPlugin("PermissionsEx");
        if (q != null && q.isEnabled()) 
        {
        	permissionsExPlugin = PermissionsEx.getPermissionManager();
        }
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		if(sender instanceof Player)
		{
			DreamLandPlayer player = player((Player)sender);
			if (commandLabel.equalsIgnoreCase("setdreamspawn"))
			{
				if(player.hasPermission("dreamland.setdreamspawn", false) && player.Dreaming())
				{
					Location location = player.getLocation();
					location.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
					return true;
				}
			}
			
			if (commandLabel.equalsIgnoreCase("wakeup")) 
			{
				if(player.Dreaming())
				{
					player.leaveDream();
					return true;
				}
			}
		}
		return false;
	}
	
	public void onDisable()
	{
		for(DreamLandPlayer player : Players.values())
		{
			if(player.Dreaming())
			{
				player.leaveDream();
			}
		}
		scheduler.Stop();
	}
	
	public void reload()
	{
		reloadConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		dream.load("dream");
		nightmare.load("nightmare");
		setupBase();
		options.load();
	}
	
	private void setupBase()
	{
		FileConfiguration config = getConfig();
		base.PersistInventory = config.getBoolean("dreamland.worlds.default.persistInventory",true);
	}
	
	public DreamLandWorld world(World world)
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
	
	public DreamLandPlayer player(Player player)
	{
		String key = player.getName();
		if(Players.containsKey(key))
		{
			return Players.get(key).self(player);
		}
		Players.put(player.getName(), new DreamLandPlayer(this).self(player));
		return player(player);
	}

	public void removePlayer(DreamLandPlayer player) 
	{
		Players.remove(player.getName());		
	}
}
