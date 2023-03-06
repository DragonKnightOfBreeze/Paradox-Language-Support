package icu.windea.pls.lang

import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于处理本地化信息。
 */
object ParadoxLocalisationHandler {
	@JvmStatic
	fun getInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
		return getInfoFromCache(element)
	}
	
	private fun getInfoFromCache(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedLocalisationInfoKey) {
			val value = resolveInfo(element)
			CachedValueProvider.Result.create(value, element)
		}
	}
	
	private fun resolveInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
		//首先尝试直接基于stub进行解析
		val stub = runCatching { element.stub }.getOrNull()
		if(stub != null) {
			return resolveInfoByStub(stub)
		}
		
		val name = element.name
		val file = element.containingFile.originalFile.virtualFile ?: return null
		val category = ParadoxLocalisationCategory.resolve(file) ?: return null
		val gameType = file.fileInfo?.rootInfo?.gameType //这里还是基于fileInfo获取gameType
		return ParadoxLocalisationInfo(name, category, gameType)
	}
	
	private fun resolveInfoByStub(stub: ParadoxLocalisationStub): ParadoxLocalisationInfo {
		val name = stub.name.orEmpty()
		val category = stub.category
		val gameType = stub.gameType
		return ParadoxLocalisationInfo(name, category, gameType)
	}
}