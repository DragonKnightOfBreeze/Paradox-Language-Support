package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxScriptStringReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptString>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的definition、localisation、syncedLocalisation等
		val resolved = resolve()
		when {
			resolved == null -> pass()
			resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
			!resolved.isWritable -> throw IncorrectOperationException() //不允许重命名
			else -> resolved.setName(newElementName)
		}
		return element.setValue(newElementName)
	}
	
	override fun resolve(): PsiNamedElement? {
		//特殊字符串需要被识别为标签的情况
		element.resolveTagConfig()?.let { tagConfig -> return tagConfig.pointer.element }
		return resolveValue(element) //根据对应的expression进行解析
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		//特殊字符串需要被识别为标签的情况
		element.resolveTagConfig()?.let { tagConfig -> tagConfig.pointer.element?.let { e -> return arrayOf(PsiElementResolveResult(e)) } }
		return multiResolveValue(element).mapToArray { PsiElementResolveResult(it) } //根据对应的expression进行解析
	}
	
	//代码提示功能由ParadoxScriptCompletionContributor统一实现
}