package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider
 */
class ParadoxScriptValueReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptString>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的definition、localisation、syncedLocalisation等
		val resolved = resolve()
		when {
			resolved == null -> pass()
			resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
			resolved.isWritable -> resolved.setName(newElementName)
			else -> throw IncorrectOperationException() //不允许重命名
		}
		return element.setValue(newElementName)
	}
	
	override fun resolve(): PsiNamedElement? {
		return CwtConfigHandler.resolveValue(element) //根据对应的expression进行解析
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		return CwtConfigHandler.multiResolveValue(element).mapToArray { PsiElementResolveResult(it) } //根据对应的expression进行解析
	}
	
	//代码提示功能由ParadoxDefinitionCompletionProvider统一实现
}