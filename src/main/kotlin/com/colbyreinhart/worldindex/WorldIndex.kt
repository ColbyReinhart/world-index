package com.colbyreinhart.worldindex

import org.bukkit.plugin.java.JavaPlugin

class WorldIndex: JavaPlugin()
{
	val locationManager = LocationManager(this)

	override fun onEnable()
	{
		getCommand("go")!!.setExecutor(GoCommand(locationManager))
		getCommand("go")!!.setTabCompleter(GoTabCompleter(locationManager))
	}
}
