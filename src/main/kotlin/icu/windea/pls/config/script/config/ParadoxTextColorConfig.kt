package icu.windea.pls.config.script.config

import com.intellij.psi.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*
import java.awt.*

class ParadoxTextColorConfig(
	override val name: String,
	override val gameType: ParadoxGameType,
	override val pointer: SmartPsiElementPointer<ParadoxScriptProperty>,
	val r: Int,
	val g: Int,
	val b: Int
) : ParadoxScriptConfig {
	val color: Color = Color(r, g, b)
	val icon = ColorIcon(16, color)
	
	val text = buildString {
		append(name).append(" = { ").append(r).append(" ").append(g).append(" ").append(b).append(" }")
		val message = PlsExtDocBundle.message(name, "textcolor", gameType)
		if(message != null && message.isNotEmpty()) append(" (").append(message).append(")")
	}
}