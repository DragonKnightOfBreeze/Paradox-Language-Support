package icu.windea.pls.config.cwt

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.cwt.psi.*
import java.util.*

object CwtConfigPathHandler {
	@JvmStatic
	fun get(element: PsiElement): CwtConfigPath? {
		if(element is CwtFile) return EmptyCwtConfigPath
		if(element !is CwtProperty && element !is CwtValue) return null
		return getFromCache(element)
	}
	
	private fun getFromCache(element: PsiElement): CwtConfigPath? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedCwtConfigPathKey) {
			val value = resolve(element)
			CachedValueProvider.Result.create(value, element)
		}
	}
	
	private fun resolve(element: PsiElement): CwtConfigPath? {
		var current: PsiElement = element
		var depth = 0
		val subPaths = LinkedList<String>()
		while(current !is PsiFile) {
			when {
				current is CwtProperty -> {
					subPaths.addFirst(current.name)
					depth++
				}
				current is CwtValue && current.isBlockValue() -> {
					subPaths.addFirst("-")
					depth++
				}
			}
			current = current.parent ?: break
		}
		if(current !is CwtFile) return null //unexpected
		return CwtConfigPath.resolve(subPaths)
	}
}