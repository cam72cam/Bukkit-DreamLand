package me.cmesh.DreamLand;

import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class DreamLandOptions 
{
	public static DreamLand plugin;

	public Boolean anyoneCanGo;
	public ItemStack[] kit;
	public Integer attemptWait;
	public String message;
	public Boolean morningReturn;
	public Boolean signedBed;
	public Boolean weatherDisable;

	private static String anyoneCanGoPath = "dreamland.options.allowAll";
	private static String attemptWaitPath = "dreamland.options.attemptWait";
	private static String messagePath = "dreamland.options.message";
	private static String morningReturnPath = "dreamland.options.wakeup";
	private static String weatherDisablePath = "dreamland.options.weatherDisable";
	
	private static String kitPath = "dreamland.inventory.kit";
	
	public DreamLandOptions(DreamLand instance)
	{
		plugin = instance;
	}
	
	public void load()
	{
		FileConfiguration config = plugin.getConfig();
		
		//options
		anyoneCanGo = config.getBoolean(anyoneCanGoPath,true);
		attemptWait = config.getInt(attemptWaitPath, 0) * 30;
		message = config.getString(messagePath, "");
		morningReturn = config.getBoolean(morningReturnPath, true);
		weatherDisable = config.getBoolean(weatherDisablePath, false);
		
		//kit
		if(config.contains(kitPath))
		{
			kit = new ItemStack[36];
			
			Set<String> kitKeys = config.getConfigurationSection(kitPath).getKeys(false);
			for(String key : kitKeys)
				kit[Integer.parseInt(key)] = config.getItemStack(kitPath + "." + key);
		}
	}
}