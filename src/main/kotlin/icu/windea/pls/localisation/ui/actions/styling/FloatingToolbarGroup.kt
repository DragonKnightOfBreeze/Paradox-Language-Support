package icu.windea.pls.localisation.ui.actions.styling

import com.google.common.cache.*
import com.intellij.openapi.actionSystem.*
import icu.windea.pls.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.config.definition.config.*

//这里actions是基于project动态获取的，需要特殊处理

private val setColorActionCache = CacheBuilder.newBuilder()
	.weakKeys()
	.build(CacheLoader.from<ParadoxTextColorConfig, SetColorAction> { SetColorAction(it) })

private fun doGetChildren(): List<AnAction> {
	val textEditor = threadLocalTextEditorContainer.get() ?: return emptyList()
	val project = textEditor.editor.project ?: return emptyList()
	val gameType = textEditor.file.fileInfo?.gameType ?: return emptyList()
	val colorConfigs = DefinitionConfigHandler.getTextColorConfigs(gameType, project)
	if(colorConfigs.isEmpty()) return emptyList()
	return colorConfigs.map { setColorActionCache.get(it) }
}

class FloatingToolbarGroup : DefaultActionGroup(doGetChildren()) {
	override fun isPopup() = true
}

