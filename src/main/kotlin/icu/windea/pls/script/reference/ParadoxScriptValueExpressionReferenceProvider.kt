package icu.windea.pls.script.reference

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * @see ParadoxScriptValueReference
 * @see ParadoxScriptLinkValuePrefixReference
 * @see ParadoxScriptLinkValueReference
 */
class ParadoxScriptValueExpressionReferenceProvider: PsiReferenceProvider(){
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		if(element !is ParadoxScriptString) return PsiReference.EMPTY_ARRAY
		element.processChild {
			when(it.elementType) {
				ParadoxScriptElementTypes.STRING_TOKEN -> return arrayOf(ParadoxScriptValueReference(element, it.textRangeInParent))
				ParadoxScriptElementTypes.QUOTED_STRING_TOKEN -> return arrayOf(ParadoxScriptValueReference(element, it.textRangeInParent))
				else -> end()
			}
		}
		return PsiReference.EMPTY_ARRAY
	}
}