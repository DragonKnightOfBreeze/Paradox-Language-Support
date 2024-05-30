package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.cwt.psi.*

class CwtFileTreeElement(
	element: CwtFile
) : PsiTreeElementBase<CwtFile>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val rootBlock = element.block ?: return emptyList()
		val result = mutableListOf<StructureViewTreeElement>()
		rootBlock.forEachChild {
			when(it){
				is CwtProperty -> result.add(CwtPropertyTreeElement(it))
				is CwtValue -> result.add(CwtValueTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return element.name
	}
}