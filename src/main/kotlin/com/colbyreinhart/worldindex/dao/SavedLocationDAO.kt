package com.colbyreinhart.worldindex.dao

import java.sql.Connection
import java.sql.ResultSet
import com.colbyreinhart.worldindex.model.SavedLocation
import com.colbyreinhart.worldindex.model.Coordinate
import com.colbyreinhart.worldindex.util.newUUID
import kotlin.getOrThrow

class SavedLocationDAO(connection: Connection): AbstractDAO(connection)
{
	fun retrieveAllLocations() = query (
		sql = "SELECT * FROM SAVED_LOCATION",
		constructor = this::constructSavedLocation
	)

	fun retrieveLocationWhitelist(locationId: ByteArray) = query (
		"SELECT PLAYER_ID FROM LOCATION_WHITELIST WHERE LOCATION_ID=?",
		{ stmt -> stmt.setBytes(1, locationId) },
		this::constructLocaionWhitelistEntry
	)

	fun saveLocation(loc: SavedLocation) = runCatching {
		if (loc.id != null)
		{
			update ("UPDATE SAVED_LOCATION SET LOCATION_NAME=?,LOCATION_WORLD=?,LOCATION_OWNER=?,POS_X=?,POS_Y=?,POS_Z=? WHERE LOCATION_ID=?") {
				it.setString(1, loc.name)
				it.setString(2, loc.world)
				it.setBytes(3, loc.owner)
				it.setDouble(4, loc.coordinates.x)
				it.setDouble(5, loc.coordinates.y)
				it.setDouble(6, loc.coordinates.z)
				it.setBytes(7, loc.id)
			}
			saveLocationWhitelist(loc)
			loc
		}
		else
		{
			val id = newUUID()
			update ("INSERT INTO SAVED_LOCATION VALUES (?,?,?,?,?,?,?)") {
				it.setBytes(1, id)
				it.setString(2, loc.name)
				it.setString(3, loc.world)
				it.setBytes(4, loc.owner)
				it.setDouble(5, loc.coordinates.x)
				it.setDouble(6, loc.coordinates.y)
				it.setDouble(7, loc.coordinates.z)
			}
			saveLocationWhitelist(loc)
			SavedLocation (
				id = id,
				name = loc.name,
				world = loc.world,
				owner = loc.owner,
				coordinates = loc.coordinates,
				whitelist = loc.whitelist
			)
		}
	}

	fun deleteLocation(loc: SavedLocation) = runCatching {
		if (loc.id == null) throw IllegalArgumentException("Cannot delete location ${loc.name}: location does not exist.")
		update("DELETE FROM SAVED_LOCATION WHERE LOCATION_ID=?") { it.setBytes(1, loc.id) }
		update("DELETE FROM LOCATION_WHITELIST WHERE LOCATION_ID=?") { it.setBytes(1, loc.id) }
	}

	protected fun saveLocationWhitelist(loc: SavedLocation)
	{
		update (
			"DELETE FROM LOCATION_WHITELIST WHERE LOCATION_ID=?",
			{ stmt -> stmt.setBytes(1, loc.id) }
		)
		batchUpdate("INSERT INTO LOCATION_WHITELIST VALUES (?,?)", loc.whitelist) { stmt, entity ->
			stmt.setBytes(1, loc.id)
			stmt.setBytes(2, entity)
		}
	}

	protected fun constructSavedLocation(resultSet: ResultSet): SavedLocation
	{
		val locId = resultSet.getBytes(1);
		return SavedLocation (
			id = locId,
			name = resultSet.getString(2),
			world = resultSet.getString(3),
			owner = resultSet.getBytes(4),
			coordinates = Coordinate (
				x = resultSet.getDouble(5),
				y = resultSet.getDouble(6),
				z = resultSet.getDouble(7)
			),
			whitelist = retrieveLocationWhitelist(locId).getOrThrow()
		)
	}

	protected fun constructLocaionWhitelistEntry(resultSet: ResultSet) = resultSet.getBytes(1)
}