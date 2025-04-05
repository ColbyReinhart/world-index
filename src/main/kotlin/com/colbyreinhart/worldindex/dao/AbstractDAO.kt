package com.colbyreinhart.worldindex.dao

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

open class AbstractDAO(protected val connection: Connection)
{
	protected fun <T> query (
		sql: String,
		initializer: (stmt: PreparedStatement) -> Unit = {},
		constructor: (results: ResultSet) -> T
	) = runCatching {
		
		connection.prepareStatement(sql).use { stmt ->
			initializer.invoke(stmt)
			stmt.executeQuery().let { resultSet ->
				val results: ArrayList<T> = arrayListOf()
				while (resultSet.next()) {
					results.add(constructor.invoke(resultSet))
				}
				results
			}
		}
	}

	protected fun update (
		sql: String,
		initializer: (stmt: PreparedStatement) -> Unit = {}
	) = runCatching {
		connection.prepareStatement(sql).use { stmt ->
			initializer.invoke(stmt)
			stmt.executeUpdate()
		}
	}

	protected fun <T> batchUpdate (
		sql: String,
		entities: List<T>,
		initializer: (stmt: PreparedStatement, entity: T) -> Unit
	) = runCatching {
		connection.prepareStatement(sql).use { stmt ->
			entities.forEach { entity ->
				initializer.invoke(stmt, entity)
				stmt.addBatch()
			}
			stmt.executeBatch()
		}
	}
}