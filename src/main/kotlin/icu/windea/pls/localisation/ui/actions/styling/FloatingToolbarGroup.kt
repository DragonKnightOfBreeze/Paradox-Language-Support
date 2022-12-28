package icu.windea.pls.localisation.ui.actions.styling

import com.google.common.cache.*
import com.intellij.openapi.actionSystem.*
import icu.windea.pls.config.script.*
import icu.windea.pls.config.script.config.*
import icu.windea.pls.core.*

//这里actions是基于project动态获取的，需要特殊处理

private val setColorActionCache = CacheBuilder.newBuilder()
	.weakKeys()
	.build(CacheLoader.from<ParadoxTextColorConfig, SetColorAction> { SetColorAction(it) })

private fun doGetChildren(): List<AnAction> {
	val textEditor = PlsThreadLocals.threadLocalTextEditorContainer.get() ?: return emptyList()
	val project = textEditor.editor.project ?: return emptyList()
	val file = textEditor.file
	val gameType = file.fileInfo?.rootInfo?.gameType ?: return emptyList()
	val colorConfigs = ParadoxTextColorConfigHandler.getTextColorConfigs(gameType, project, file)
	if(colorConfigs.isEmpty()) return emptyList()
	return colorConfigs.map { setColorActionCache.get(it) }
}

class FloatingToolbarGroup : DefaultActionGroup(doGetChildren()) {
	init {
		templatePresentation.isPopupGroup = true
	}
}

