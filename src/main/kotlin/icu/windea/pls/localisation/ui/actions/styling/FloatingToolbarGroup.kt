package icu.windea.pls.localisation.ui.actions.styling

import com.google.common.cache.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

//这里actions是基于project动态获取的，需要特殊处理

private val setColorActionCache = CacheBuilder.newBuilder()
	.weakKeys()
	.build(CacheLoader.from<ParadoxTextColorInfo, SetColorAction> { SetColorAction(it) })

private fun doGetChildren(): List<AnAction> {
	val textEditor = PlsThreadLocals.textEditor.get() ?: return emptyList()
	val project = textEditor.editor.project ?: return emptyList()
	val virtualFile = textEditor.file
	val file = virtualFile.toPsiFile<PsiFile>(project) ?: return emptyList()
	val colorConfigs = ParadoxTextColorHandler.getInfos(project, file)
	if(colorConfigs.isEmpty()) return emptyList()
	return colorConfigs.map { setColorActionCache.get(it) }
}

class FloatingToolbarGroup : DefaultActionGroup(doGetChildren()) {
	init {
		templatePresentation.isPopupGroup = true
	}
}

