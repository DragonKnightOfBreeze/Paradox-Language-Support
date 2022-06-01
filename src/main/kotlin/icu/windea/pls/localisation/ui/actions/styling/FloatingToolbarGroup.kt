package icu.windea.pls.localisation.ui.actions.styling

import com.intellij.openapi.actionSystem.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*

//这里actions是基于project动态获取的，需要特殊处理

private val setColorActionCache = createCache<ParadoxColorConfig, SetColorAction> { SetColorAction(it) }

private fun doGetChildren(): List<AnAction> {
	val project = threadLocalProjectContainer.get()
	val colorConfigs = ParadoxColorConfig.findAllAsArray(project)
	if(colorConfigs.isEmpty()) return emptyList()
	return colorConfigs.map { setColorActionCache.get(it) }
}

class FloatingToolbarGroup : DefaultActionGroup(doGetChildren()){
	override fun isPopup() = true
}

