package com.colbyreinhart.worldindex

import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import com.colbyreinhart.worldindex.WorldIndex

class LocationManager
{
	protected val locationFile: File
	val locations: ArrayList<Location>
	protected val serializer = LocationSerializer()

	constructor(plugin: JavaPlugin)
	{
		plugin.getDataFolder().let { if (!it.exists()) it.mkdir() }
		locationFile = File(plugin.getDataFolder(), "locations.txt")
		if (!locationFile.exists())
		{
			locationFile.createNewFile()
		}
		locations = serializer.readFromFile(locationFile)
	}

	fun persist()
	{
		serializer.writeToFile(locations, locationFile)
	}
}
