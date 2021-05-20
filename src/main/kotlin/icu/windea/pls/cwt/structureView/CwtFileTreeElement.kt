package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.psi.util.*
import icu.windea.pls.cwt.psi.*

class CwtFileTreeElement(
	private val element: CwtFile
) : PsiTreeElementBase<CwtFile>(element) {
	override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
		val rootBlock = element.rootBlock ?: return mutableListOf()
		return PsiTreeUtil.getChildrenOfAnyType(
			rootBlock,
			CwtProperty::class.java,
			CwtValue::class.java
		).mapTo(mutableListOf()) {
			when(it) {
				is CwtProperty -> CwtPropertyTreeElement(it)
				is CwtValue -> CwtValueTreeElement(it)
				else -> throw InternalError()
			}
		}
	}
	
	override fun getPresentableText(): String {
		return element.name
	}
}