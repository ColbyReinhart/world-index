package com.colbyreinhart.worldindex

import java.io.File
import java.io.FileWriter
import java.util.Scanner
import java.util.UUID

class LocationSerializer
{
	fun serialize(location: Location) = "${location.name}	${location.owner}	${location.password ?: ""}	${location.x}	${location.y}	${location.z}	${location.world}"
	fun writeToFile(locations: List<Location>, file: File)
	{
		FileWriter(file).use { writer ->
			locations.forEach { loc ->
				writer.write(serialize(loc))
				writer.write("\n")
			}
		}
	}

	val locationPattern = Regex("(\\w+)\t([\\w-]+)\t(.*)\t([\\d\\.-]+)\t([\\d\\.-]+)\t([\\d\\.-]+)\t([\\w-]+)")
	fun deserialize(data: String): Location
	{
		val result = locationPattern.matchEntire(data)
		if (result == null)
		{
			throw IllegalArgumentException("Invalid location pattern: ${data}")
		}
		val groups = result.groups
		if (groups.size != 8)
		{
			throw IllegalArgumentException("Invalid location pattern: ${data}")
		}

		try
		{
			return Location(
				groups.get(1)!!.value,
				UUID.fromString(groups.get(2)!!.value),
				groups.get(3)!!.value.let { if (it.equals("")) null else it },
				groups.get(4)!!.value.toDouble(),
				groups.get(5)!!.value.toDouble(),
				groups.get(6)!!.value.toDouble(),
				groups.get(7)!!.value
			)
		}
		catch( e: NumberFormatException)
		{
			throw IllegalArgumentException("Invalid location pattern: ${data}")
		}
	}
	fun readFromFile(file: File): ArrayList<Location>
	{
		Scanner(file).use { scanner ->
			val result: ArrayList<Location> = ArrayList()
			while (scanner.hasNextLine())
			{
				result.add(deserialize(scanner.nextLine()))
			}
			return result
		}
	}
}
