package icu.windea.pls.core.handler

import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于处理本地化信息。
 */
object ParadoxLocalisationHandler {
	@JvmStatic
	fun getInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedLocalisationInfoKey) {
			val value = resolveInfo(element)
			CachedValueProvider.Result.create(value, element)
		}
	}
	
	@JvmStatic
	fun resolveInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
		val name = element.name
		val file = element.containingFile.originalFile.virtualFile ?: return null
		val type = ParadoxLocalisationCategory.resolve(file) ?: return null
		val gameType = file.fileInfo?.rootInfo?.gameType //这里还是基于fileInfo获取gameType
		return ParadoxLocalisationInfo(name, type, gameType)
	}
}