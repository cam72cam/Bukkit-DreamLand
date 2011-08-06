package me.cmesh.DreamLand;

import java.util.ArrayList;
import java.util.List;

public class DreamLandOptions 
{
	public static DreamLand plugin;

	public Boolean anyoneCanGo;
	public Integer flyTool;
	public List<String> kit;
	public Double flySpeed;
	public Integer attemptWait;
	public String message;
	public Boolean morningReturn;
	public Boolean weatherDisable;
	
	public DreamLandOptions(DreamLand instance)
	{
		plugin = instance;
	}
	
	public void load()
	{
		//options
		anyoneCanGo = plugin.getConfiguration().getBoolean("dreamland.options.allowAll",true);
		attemptWait = plugin.getConfiguration().getInt("dreamland.options.attemptWait", 0) * 30;
		message = plugin.getConfiguration().getString("dreamland.options.message", "");
		morningReturn = plugin.getConfiguration().getBoolean("dreamland.options.wakeup", true);
		weatherDisable = plugin.getConfiguration().getBoolean("dreamland.options.weatherDisable", false);
		
		//fly
		flySpeed = plugin.getConfiguration().getDouble("dreamland.fly.speed", 1.0);
		flyTool = plugin.getConfiguration().getInt("dreamland.fly.tool",288);
		
		//kit
		try
		{
			kit = new ArrayList<String>();
			for(String node : plugin.getConfiguration().getKeys("dreamland.inventory.kit"))
			{
				kit.add(node + " " + plugin.getConfiguration().getString("dreamland.inventory.kit."+ node, "288 1") + " 0");
			}
		}
		catch (java.lang.NullPointerException e){}
		
		plugin.getConfiguration().save();
	}
}
