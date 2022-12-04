package icu.windea.pls.gameTypes.stellaris.psi.impl

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.gameTypes.stellaris.*
import icu.windea.pls.gameTypes.stellaris.psi.*
import icu.windea.pls.gameTypes.stellaris.references.*

object StellarisFormatStringPsiImplUtil {
	//region StellarisFormatReference
	@JvmStatic
	fun getName(element: StellarisFormatReference): String? {
		val token = element.findChild(StellarisFormatStringElementTypes.FORMAT_REFERENCE_TOKEN) ?: return null
		return token.text
	}
	
	@JvmStatic
	fun setName(element: StellarisFormatReference, name: String): PsiElement {
		val token = element.findChild(StellarisFormatStringElementTypes.FORMAT_REFERENCE_TOKEN) ?: return element
		return (token as LeafPsiElement).replaceWithText(name) as PsiElement
	}
	
	@JvmStatic
	fun getReference(element: StellarisFormatReference): StellarisFormatPsiReference? {
		val token = element.findChild(StellarisFormatStringElementTypes.FORMAT_REFERENCE_TOKEN) ?: return null
		return StellarisFormatPsiReference(element, token.textRangeInParent)
	}
	//endregion
}