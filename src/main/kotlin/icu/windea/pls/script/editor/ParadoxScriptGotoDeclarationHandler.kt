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
 * 以下几种情况并没有真正意义上的声明，将所有的引用直接作为声明：
 *
 * * 参数（`$PARAM$`中的`PARAM`）
 * * 值集中的值（`set_global_flag = xxx`中的`xxx`）
 */
class ParadoxScriptGotoDeclarationHandler : GotoDeclarationHandler {
	override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement>? {
		val elementType = sourceElement.elementType
		return when {
			elementType == STRING_TOKEN -> {
				val element = sourceElement?.parent?.castOrNull<ParadoxScriptString>() ?: return null
				return CwtConfigHandler.multiResolveValue(element) {
					it.type == CwtDataTypes.Value || it.type == CwtDataTypes.ValueSet
				}.takeIfNotEmpty()?.toTypedArray()
			}
			else -> null
		}
	}
}