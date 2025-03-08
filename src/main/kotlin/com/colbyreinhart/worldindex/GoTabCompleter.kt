package com.colbyreinhart.worldindex

import org.bukkit.command.TabCompleter
import org.bukkit.command.CommandSender
import org.bukkit.command.Command
import org.bukkit.entity.Player

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
			else if (args.size == 1)
			{
				when (args.get(0))
				{
					"to" ->
					{
						return locationManager.locations
							.stream()
							.map(Location::name)
							.toList()
							.sorted()
					}
					"remove" ->
					{
						return locationManager.locations
							.stream()
							.filter { loc -> loc.owner.equals(sender.getUniqueId()) }
							.map(Location::name)
							.toList()
							.sorted()
					}
					else -> emptyList<String>()
				}
			}
			else if (args.size == 2)
			{
				when (args.get(0))
				{
					"to" ->
					{
						return locationManager.locations
							.stream()
							.map(Location::name)
							.filter { loc -> loc.startsWith(args.get(1), ignoreCase = true) }
							.toList()
							.sorted()
					}
					"remove" ->
					{
						return locationManager.locations
							.stream()
							.filter { loc -> loc.owner.equals(sender.getUniqueId()) }
							.map(Location::name)
							.filter { loc -> loc.startsWith(args.get(1), ignoreCase = true) }
							.toList()
							.sorted()
					}
					else -> emptyList<String>()
				}
			}
		}

		return emptyList<String>()
	}
}
