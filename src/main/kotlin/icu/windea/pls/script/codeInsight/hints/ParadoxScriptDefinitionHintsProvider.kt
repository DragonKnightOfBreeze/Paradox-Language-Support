package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import icu.windea.pls.*

@Suppress("UnstableApiUsage")
class ParadoxScriptDefinitionHintsProvider : ParadoxScriptInlayHintsProvider<NoSettings>() {
	override val name: String = PlsBundle.message("script.hints.definition")
	
	override fun createSettings(): NoSettings = NoSettings()
}