package com.colbyreinhart.worldindex

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.UUID

private val LOCATION_TEXT_COLOR = TextColor.color(65, 186, 217)

data class Location(val name: String, val owner: UUID, val password: String?, val x: Double, val y: Double, val z: Double, val world: String)
{
	fun formattedName() = Component.text(name, LOCATION_TEXT_COLOR, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/go to ${name}"))
}
