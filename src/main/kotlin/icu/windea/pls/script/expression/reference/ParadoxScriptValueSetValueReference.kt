package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueSetValueReference(
	element: ParadoxExpressionElement,
	rangeInElement: TextRange,
	private val name: String,
	private val config: CwtDataConfig<*>
) : PsiReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): ParadoxExpressionElement {
		//重命名引用指向的元素（仅修改对应范围的文本）
		return element.setValue(rangeInElement.replace(element.value, newElementName))
	}
	
	override fun resolve(): PsiElement? {
		if(element !is ParadoxScriptString) return null //暂不支持，未发现对应的规则
		return CwtConfigHandler.resolveValueSetValue(element, name, config)
	}
}