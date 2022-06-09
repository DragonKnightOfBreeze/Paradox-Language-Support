package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * @see ParadoxScriptKeyReference
 * @see ParadoxScriptLinkReference
 */
class ParadoxScriptKeyExpressionReferenceProvider : PsiReferenceProvider() {
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		if(element !is ParadoxScriptPropertyKey) return PsiReference.EMPTY_ARRAY
		element.processChild {
			when(it.elementType) {
				ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN -> {
					//如果keyExpression可能是一个linkExpression（支持scope，且格式类似X.Y，则需要另外解析为多个scopeReference）
					val dotIndices = element.text.indicesOf('.')
					if(dotIndices.isNotEmpty()) {
						if(element.mayBeLinkExpression()) {
							val rawTextRangeInParent = element.textRangeInParent
							return Array(dotIndices.size + 1) { i ->
								val start = rawTextRangeInParent.startOffset
								val end = rawTextRangeInParent.endOffset
								val textRangeInParent = when {
									i == 0 -> TextRange.create(start, start + dotIndices[i])
									i == dotIndices.size -> TextRange.create(start + dotIndices[i - 1] + 1, end)
									else -> TextRange.create(start + dotIndices[i - 1] + 1, start + dotIndices[i])
								}
								ParadoxScriptLinkReference(element, textRangeInParent)
							}
						}
					}
					return arrayOf(ParadoxScriptKeyReference(element, it.textRangeInParent))
				}
				ParadoxScriptElementTypes.QUOTED_PROPERTY_KEY_TOKEN -> {
					return arrayOf(ParadoxScriptKeyReference(element, it.textRangeInParent))
				}
				else -> end()
			}
		}
		return PsiReference.EMPTY_ARRAY
	}
}