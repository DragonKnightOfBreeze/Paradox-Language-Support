package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueSetValueReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	private val name: String,
	private val config: CwtKvConfig<*>
): PsiReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement){
	override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
		//尝试重命名关联的definition、localisation、syncedLocalisation等
		val resolved = resolve()
		when {
			resolved == null -> pass()
			resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
			resolved is PsiNamedElement -> resolved.setName(newElementName)
			else -> throw IncorrectOperationException() //不允许重命名
		}
		//重命名引用指向的元素（仅修改对应范围的文本）
		return element.setValue(rangeInElement.replace(element.value, newElementName))
	}
	
	override fun resolve(): PsiElement? {
		if(element !is ParadoxScriptString) return null //暂不支持，未发现对应的规则
		val valueSetName = config.expression.value ?: return null
		val configGroup = config.info.configGroup
		val write = config.expression.type == CwtDataTypes.ValueSet
		return CwtConfigHandler.resolveValueSetValue(name, valueSetName, configGroup, write)
	}
}