package com.colbyreinhart.worldindex

import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import com.colbyreinhart.worldindex.model.Coordinate
import com.colbyreinhart.worldindex.model.SavedLocation
import com.colbyreinhart.worldindex.util.uuidToBytes

class GoCommand(val locationManager: LocationManager): CommandExecutor
{
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean
	{
		if (args.size == 0) return false

		if (sender is Player)
		{
			when (args.get(0).lowercase())
			{
				"to" ->
				{
					if (args.size < 1 || args.size > 3) return false
					return goTo(sender, args.get(1))
				}
				"list" ->
				{
					if (args.size != 1) return false
					goList(sender)
					return true
				}
				"add" ->
				{
					if (args.size < 1 || args.size > 3) return false
					return goAdd(sender, args.get(1))
				}
				"remove" ->
				{
					if (args.size != 2) return false
					return goRemove(sender, args.get(1))
				}
				else -> return false
			}
		}
		else
		{
			sender.sendMessage("Only players may execute this command!")
			return false
		}
	}

	protected fun goTo(player: Player, arg: String): Boolean
	{
		val location = locationManager.getLocationByName(arg).let { loc ->
			if (loc.isEmpty())
			{
				player.sendMessage("Location not found: ${arg}")
				return true
			}
			loc.get()
		}

		val destinationWorld = Bukkit.getWorlds()
			.stream()
			.filter { world -> world.getName().equals(location.world) }
			.findAny()
			.get()
		player.teleport(org.bukkit.Location(
			destinationWorld,
			location.coordinates.x,
			location.coordinates.y,
			location.coordinates.z)
		)
		return true
	}

	protected fun goList(player: Player)
	{
		val message = Component.text()
		message.append(Component.text("Available locations:\n"))
		locationManager.getLocationComponentList().forEach { loc ->
			message.append(Component.text("  "))
				.append(loc)
				.append(Component.text('\n'))
		}
		player.sendMessage(message)
	}

	val validLocationName = Regex("\\w+")
	protected fun goAdd(player: Player, arg: String): Boolean
	{
		if (validLocationName.matchEntire(arg) == null)
		{
			player.sendMessage("Invalid location name. Location names can only consist of letters, number and underscores.")
			return true
		}

		locationManager.getLocationByName(arg).let {
			if (it.isPresent())
			{
				player.sendMessage("A location with this name already exists.")
				return true
			}
		}

		locationManager.persist(SavedLocation(
			name = arg,
			owner = uuidToBytes(player.getUniqueId()),
			world = player.getWorld().getName(),
			coordinates = Coordinate (
				player.getLocation().x(),
				player.getLocation().y(),
				player.getLocation().z()
			)
		))

		player.sendMessage("Location added.")
		return true
	}

	protected fun goRemove(player: Player, arg: String): Boolean
	{
		val location = locationManager.getLocationByName(arg).let { option ->
			if (option.isEmpty())
			{
				player.sendMessage("Location not found: ${arg}.")
				return true
			}
			option.get()
		}

		if (!location.owner.contentEquals(uuidToBytes(player.getUniqueId())))
		{
			player.sendMessage("You cannot remove a location that you do not own.")
			return true
		}
		locationManager.delete(location)
		player.sendMessage("Location removed.")

		return true
	}
}