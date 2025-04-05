package com.colbyreinhart.worldindex.util

import java.nio.ByteBuffer
import java.util.UUID

fun uuidToBytes(uuid: UUID): ByteArray
{
	val buf = ByteBuffer.allocate(16)
	buf.putLong(uuid.mostSignificantBits)
	buf.putLong(uuid.leastSignificantBits)
	return buf.array()
}

fun newUUID() = uuidToBytes(UUID.randomUUID())
