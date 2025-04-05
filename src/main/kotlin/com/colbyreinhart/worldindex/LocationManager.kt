package com.colbyreinhart.worldindex

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.plugin.java.JavaPlugin
import org.sqlite.SQLiteDataSource
import java.io.File
import java.sql.Connection
import com.colbyreinhart.worldindex.WorldIndex
import com.colbyreinhart.worldindex.dao.SavedLocationDAO
import com.colbyreinhart.worldindex.model.SavedLocation
import javax.sql.DataSource
import kotlin.getOrThrow

private const val DB_FILE_NAME = "data.db"
private val LOCATION_TEXT_COLOR = TextColor.color(65, 186, 217)

class LocationManager
{
	protected val locations: ArrayList<SavedLocation>
	protected val dataSource = SQLiteDataSource()

	constructor(plugin: JavaPlugin)
	{
		dataSource.url = "jdbc:sqlite:${plugin.dataFolder}/${DB_FILE_NAME}"
		preparePluginDataFolder(plugin.getDataFolder())
		locations = SavedLocationDAO(getConnection()).retrieveAllLocations().getOrThrow()
		println("Found ${locations.size} locations")
	}

	fun getLocationByName(name: String) = locations.stream()
		.filter { it.name.equals(name, ignoreCase = true) }
		.findAny()

	fun getLocationList(substring: String, owner: ByteArray? = null) = locations.stream()
		.filter { loc -> owner == null || owner.contentEquals(loc.owner) }
		.map(SavedLocation::name)
		.filter { loc -> substring.isEmpty() || loc.startsWith(substring, ignoreCase = true) }
		.sorted()
		.toList()

	fun getLocationComponentList() = locations.stream()
		.sorted()
		.map { loc -> Component.text(loc.name, LOCATION_TEXT_COLOR, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/go to ${loc.name}")) }
		.toList()

	fun persist(loc: SavedLocation) = runCatching {
		val savedLoc = SavedLocationDAO(getConnection()).saveLocation(loc).getOrThrow()
		if (loc.id != null)
		{
			for (i in 0..locations.size)
			{
				val thisId = locations.get(i).id
				if (thisId != null && thisId.equals(savedLoc.id))
				{
					locations[i] = savedLoc
				}
			}
		}
		else
		{
			locations.add(savedLoc)
		}
		savedLoc
	}

	fun delete(loc: SavedLocation) = runCatching {
		if (loc.id == null) throw IllegalArgumentException("Cannot delete location ${loc.name}: location does not exist.")
		locations.remove(loc)
		SavedLocationDAO(getConnection()).deleteLocation(loc)
		Unit
	}

	protected fun getConnection(): Connection
	{
		val connection = dataSource.getConnection()
		connection.createStatement().use { stmt ->
			stmt.execute("PRAGMA foreign_keys = ON;")
		}
		return connection
	}

	protected fun preparePluginDataFolder(dataFolder: File)
	{
		if (!dataFolder.exists()) dataFolder.mkdir()
		dataSource.getConnection().use { connection ->
			val schemaExists: Boolean

			connection.prepareStatement("SELECT COUNT(NAME) FROM SQLITE_MASTER WHERE TYPE='table' AND NAME='SAVED_LOCATION'").use { stmt ->
				stmt.executeQuery().use { results ->
					schemaExists = results.getInt(1) > 0
				}
			}

			if (!schemaExists)
			{
				connection.createStatement().use { stmt ->
					this::class.java
						.getResourceAsStream("/db.sql")
						.bufferedReader()
						.use { stream ->
							stream.readText()
						}.split(";")
						.map { it.trim() }
						.filter { it.isNotBlank() }
						.forEach { sql ->
							stmt.execute(sql)
						}
				}
			}
		}
	}
}
