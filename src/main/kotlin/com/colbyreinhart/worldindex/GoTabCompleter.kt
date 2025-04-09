package com.colbyreinhart.worldindex

import com.colbyreinhart.worldindex.model.SavedLocation
import com.colbyreinhart.worldindex.util.uuidToBytes
import org.bukkit.command.TabCompleter
import org.bukkit.command.CommandSender
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.Bukkit

private val SUBCOMMANDS = listOf("to", "list", "add", "remove")

class GoTabCompleter(val locationManager: LocationManager): TabCompleter
{
	override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>
	{
		if (command.getName().equals("go", ignoreCase = true) && sender is Player)
		{
			if (args.size == 0)
			{
				return SUBCOMMANDS
			}
			else if (args.size == 1 && !SUBCOMMANDS.contains(args.get(0)))
			{
				return SUBCOMMANDS.stream()
					.filter { verb -> verb.startsWith(args.get(0)) }
					.toList()
			}
			else if (args.size == 2)
			{
				when (args.get(0))
				{
					"to" -> return getDestinationList(sender, args.get(1))
					"remove" -> return locationManager.getLocationList(args.get(1), uuidToBytes(sender.getUniqueId()))
					else -> return emptyList<String>()
				}
			}
		}

		return emptyList<String>()
	}

	protected fun getDestinationList(sender: Player, arg: String): List<String>
	{
		val playerList = Bukkit.getOnlinePlayers()
			.stream()
			.filter { player -> !player.equals(sender) }
			.map(Player::getName)
			.sorted()
			.map { name -> "@${name}" }
			.toList()
		val locationList = locationManager.getLocationList(arg)

		if (arg.isEmpty())
		{
			return playerList.plus(locationList)
		}
		else if (arg.startsWith("@"))
		{
			return playerList.stream()
				.filter { name -> arg.isEmpty() || name.startsWith(arg) }
				.toList()
		}
		else
		{
			return locationList
		}
	}
}
