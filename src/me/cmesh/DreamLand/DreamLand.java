package me.cmesh.DreamLand;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
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
    public static PermissionHandler Permissions = null;
    public Boolean anyoneCanGo = true;
    public Boolean usingpermissions = false;
    public Integer chance = 1;
    public Boolean dreamFly = true;
    public List<String> flyTool = Arrays.asList("288");
    public Boolean seperateInv = false;
    public Boolean kit = false;
    public Boolean portalExplode = true;
	public Double flySpeed = 1.0;
	public Boolean dreamInvincible;
	public Integer attemptWait = 0;
	public Boolean message = false;
	
    public void onEnable()
	{ 
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);


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

		getServer().createWorld(getServer().getWorlds().get(0).getName()+"_skylands",Environment.SKYLANDS,getServer().getWorlds().get(0).getSeed());
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	  {
    	if (commandLabel.equalsIgnoreCase("setdreamspawn")) 
    	{
    		if (sender instanceof Player) 
    		{
    			Player player = (Player)sender;
    			playerListener.saveSpawn(player);
    			Location location = player.getLocation();
    			player.sendMessage("Spawn set to: " + (int)location.getX() + "x," + (int)location.getY() + "y," + (int)location.getZ() + "z");
    			return true;
    		}
    	}
    	return false;
	}
	
	public void onDisable()
	{
	    log.info(getDescription().getName()+" version "+getDescription().getVersion()+" is disabled!");
	}

	public boolean checkpermissions(Player player, String string, Boolean standard)
	{
		return (anyoneCanGo || (player.isOp() == true) || (usingpermissions ? Permissions.has(player,string) : standard));
	}
	
	public void reload()
	{
		getConfiguration().load();
		chance = getConfiguration().getInt("dreamland.chance",1);
    	anyoneCanGo = getConfiguration().getBoolean("dreamland.allowAll",true);
    	dreamInvincible = getConfiguration().getBoolean("dreamland.dreamInvincible", true);
		dreamFly = getConfiguration().getBoolean("dreamland.fly",true);
		flySpeed = getConfiguration().getDouble("dreamland.flySpeed", 1.0);
		attemptWait = getConfiguration().getInt("dreamland.attemptWait", 0);
		attemptWait *= 30;
		
		message = getConfiguration().getBoolean("dreamland.message", false);
		
		seperateInv = getConfiguration().getBoolean("dreamland.seperateInventories", false  );
		kit = getConfiguration().getBoolean("dreamland.kit", false);
		
		File kitFile = new File(getDataFolder().getAbsolutePath() + File.separator + "kit.txt");
		if(kit)
		{
			createFile(kitFile);
		}
		File messageFile = new File(getDataFolder().getAbsolutePath() + File.separator + "message.txt");
		if(message)
		{
			createFile(messageFile);
		}
		flyTool = Arrays.asList(getConfiguration().getString("dreamland.flytool","288").split(","));
		portalExplode = getConfiguration().getBoolean("dreamland.portalexplode",true);
		getConfiguration().save();
		
		
		File lock =  new File(getDataFolder().getAbsolutePath() + File.separator + "Lock");
		deleteDir(lock);
		File attempts =  new File(getDataFolder().getAbsolutePath() + File.separator + "Attempts");
		deleteDir(attempts);
	}

	public static boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }
	    return dir.delete();
	}

	public static void createFile(File file)
	{
		if(file.exists())
		{
			return;
		}
		try 
		{
			file.createNewFile();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
