package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

class CwtValueTreeElement(
	element: CwtValue
) : PsiTreeElementBase<CwtValue>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		if(element !is CwtBlock) return emptyList()
		//允许混合value和property
		val result: MutableList<StructureViewTreeElement> = SmartList()
		element.forEachChild {
			when {
				it is CwtValue -> result.add(CwtValueTreeElement(it))
				it is CwtProperty -> result.add(CwtPropertyTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return when {
			element is CwtBlock -> PlsFolders.blockFolder
			element is CwtString -> element.text //保留可能包围的引号
			else -> element.text
		}
	}
}