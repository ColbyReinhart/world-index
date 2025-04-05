package com.colbyreinhart.worldindex.model

import java.util.UUID

data class SavedLocation (
	val id: ByteArray? = null,
	val name: String,
	val world: String,
	val owner: ByteArray,
	val coordinates: Coordinate,
	val whitelist: List<ByteArray> = arrayListOf()
): Comparable<SavedLocation> 
{
	override fun compareTo(other: SavedLocation) = name.compareTo(other.name)
}
