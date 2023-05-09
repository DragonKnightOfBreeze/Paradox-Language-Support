package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

val ParadoxLocalisationLocale.localeId: PsiElement get() = findChild(LOCALE_ID)!!

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement get() = findChild(PROPERTY_KEY_TOKEN)!!

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement? get() = findChild(PROPERTY_REFERENCE_ID)
val ParadoxLocalisationPropertyReference.propertyReferenceParameter: PsiElement? get() = findChild(PROPERTY_REFERENCE_PARAMETER_TOKEN)

val ParadoxLocalisationIcon.iconId: PsiElement? get() = findChild(ICON_ID)
val ParadoxLocalisationIcon.iconIdReference: ParadoxLocalisationPropertyReference?
	get() {
		forEachChild {
			if(it is ParadoxLocalisationPropertyReference) return it
			if(it.elementType == PIPE) return null
		}
		return null
	}
val ParadoxLocalisationIcon.iconFrame: PsiElement? get() = findChild(ICON_FRAME)
val ParadoxLocalisationIcon.iconFrameReference: ParadoxLocalisationPropertyReference?
	get() {
		var afterPipe = false
		forEachChild {
			if(afterPipe && it is ParadoxLocalisationPropertyReference) return it
			if(it.elementType == PIPE) afterPipe = true
		}
		return null
	}

val ParadoxLocalisationColorfulText.colorId: PsiElement? get() = findChild(COLOR_ID)

val ParadoxLocalisationCommandScope.commandScopeId: PsiElement get() = findChild(COMMAND_SCOPE_ID)!!

val ParadoxLocalisationCommandField.commandFieldId: PsiElement? get() = findChild(COMMAND_FIELD_ID)

val ParadoxLocalisationScriptedVariableReference.idElement: PsiElement? get() = findChild(SCRIPTED_VARIABLE_REFERENCE_TOKEN)
