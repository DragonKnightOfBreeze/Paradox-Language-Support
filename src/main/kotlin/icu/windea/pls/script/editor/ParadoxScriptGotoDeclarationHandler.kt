package icu.windea.pls.script.editor

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

/**
 * 以下几种情况一个引用可能对应多个声明：
 *
 * * 参数（`$PARAM$`）
 */
@Deprecated("UNUSED")
class ParadoxScriptGotoDeclarationHandler : GotoDeclarationHandlerBase() {
	override fun getGotoDeclarationTarget(sourceElement: PsiElement?, editor: Editor): PsiElement? {
		return when {
			sourceElement.elementType == PROPERTY_KEY_TOKEN -> {
				val element = sourceElement?.parent?.castOrNull<ParadoxScriptPropertyKey>() ?: return null
				return CwtConfigHandler.resolveKey(element) {
					it.type == CwtDataTypes.Enum && it.value == CwtConfigHandler.paramsEnumName
				}
			}
			else -> null
		}
	}
	
	override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement>? {
		return when {
			sourceElement.elementType == PROPERTY_KEY_TOKEN -> {
				val element = sourceElement?.parent?.castOrNull<ParadoxScriptPropertyKey>() ?: return null
				return CwtConfigHandler.multiResolveKey(element) {
					it.type == CwtDataTypes.Enum && it.value == CwtConfigHandler.paramsEnumName
				}.takeIfNotEmpty()?.toTypedArray()
			}
			else -> null
		}
	}
}