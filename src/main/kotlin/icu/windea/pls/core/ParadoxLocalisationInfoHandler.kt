package icu.windea.pls.core

import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

/**
 * 用于处理本地化信息。
 */
object ParadoxLocalisationInfoHandler {
	@JvmStatic
	fun get(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedLocalisationInfoKey) {
			val value = resolve(element)
			CachedValueProvider.Result.create(value, element)
		}
	}
	
	@JvmStatic
	fun resolve(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
		val name = element.name
		val file = element.containingFile.originalFile.virtualFile ?: return null
		val type = ParadoxLocalisationCategory.resolve(file) ?: return null
		val gameType = file.fileInfo?.rootInfo?.gameType //这里还是基于fileInfo获取gameType
		return ParadoxLocalisationInfo(name, type, gameType)
	}
}