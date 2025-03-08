package com.colbyreinhart.worldindex

import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

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
					return goTo(sender, args.get(1), args.getOrNull(2))
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
					return goAdd(sender, args.get(1), args.getOrNull(2))
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

	protected fun goTo(player: Player, arg: String, password: String?): Boolean
	{
		val location = locationManager
			.locations
			.stream()
			.filter { it.name.equals(arg, ignoreCase = true) }
			.findAny()

		if (location.isEmpty())
		{
			player.sendMessage("Location not found: ${arg}")
			return true
		}

		location.get().let {
			if (it.password != null && !it.owner.equals(player.getUniqueId()))
			{
				if (password == null)
				{
					player.sendMessage("This location is password-protected. Please provide a password.")
					return true
				}
				else if (!password.equals(it.password))
				{
					player.sendMessage("Incorrect password, please try again.")
					return true
				}
			}

			val destinationWorld = Bukkit.getWorlds()
				.stream()
				.filter { world -> world.getName().equals(it.world) }
				.findAny()
				.get()
			player.teleport(org.bukkit.Location(destinationWorld, it.x, it.y, it.z))
		}
		return true
	}

	protected fun goList(player: Player)
	{
		val message = Component.text()
		message.append(Component.text("Available locations:\n"))
		locationManager.locations.forEach { location ->
			message.append(Component.text("  ")).append(location.formattedName()).append(Component.text('\n'))
		}
		player.sendMessage(message)
	}

	val validLocationName = Regex("\\w+")
	protected fun goAdd(player: Player, arg: String, password: String?): Boolean
	{
		if (validLocationName.matchEntire(arg) == null)
		{
			player.sendMessage("Invalid location name. Location names can only consist of letters, number and underscores.")
			return true
		}

		locationManager.locations
			.stream()
			.filter { loc -> loc.name.equals(arg, ignoreCase = true) }
			.findAny()
			.let {
				if (it.isPresent())
				{
					player.sendMessage("A location with this name already exists.")
					return true
				}
			}

		val location = Location (
			arg,
			player.getUniqueId(),
			password,
			player.getLocation().x(),
			player.getLocation().y(),
			player.getLocation().z(),
			player.getWorld().getName()
		)
		locationManager.locations.add(location)
		locationManager.persist()

		player.sendMessage("Location added.")
		return true
	}

	protected fun goRemove(player: Player, arg: String): Boolean
	{
		val location = locationManager
			.locations
			.stream()
			.filter { it.name.equals(arg, ignoreCase = true) }
			.findAny()

		if (location.isEmpty())
		{
			player.sendMessage("Location not found: ${arg}.")
			return true
		}

		location.get().let {
			if (!it.owner.equals(player.getUniqueId()))
			{
				player.sendMessage("You cannot remove a location that you do not own.")
				return true
			}
			locationManager.locations.remove(it)
			locationManager.persist()
			player.sendMessage("Location removed.")
		}

		return true
	}
}