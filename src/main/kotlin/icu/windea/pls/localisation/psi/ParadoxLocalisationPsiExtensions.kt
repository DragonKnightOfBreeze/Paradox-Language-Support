package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

val ParadoxLocalisationLocale.localeId: PsiElement get() = findRequiredChild(LOCALE_ID)

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement get() = findRequiredChild(PROPERTY_KEY_ID)

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement? get() = findOptionalChild(PROPERTY_REFERENCE_ID)
val ParadoxLocalisationPropertyReference.propertyReferenceParameter: PsiElement? get() = findOptionalChild(PROPERTY_REFERENCE_PARAMETER_TOKEN)

val ParadoxLocalisationIcon.iconId: PsiElement? get() = findOptionalChild(ICON_ID)
val ParadoxLocalisationIcon.iconIdReference: ParadoxLocalisationPropertyReference?
	get() {
		forEachChild { 
			if(it is ParadoxLocalisationPropertyReference) return it
			if(it.elementType == PIPE) return null
		}
		return null
	}
val ParadoxLocalisationIcon.iconFrame: PsiElement? get() = findOptionalChild(ICON_FRAME)
val ParadoxLocalisationIcon.iconFrameReference: ParadoxLocalisationPropertyReference?
	get() {
		var afterPipe = false
		forEachChild {
			if(afterPipe && it is ParadoxLocalisationPropertyReference) return it
			if(it.elementType == PIPE) afterPipe = true
		}
		return null
	}

val ParadoxLocalisationColorfulText.colorId: PsiElement? get() = findOptionalChild(COLOR_ID)

val ParadoxLocalisationCommandScope.commandScopeId: PsiElement get() = findRequiredChild(COMMAND_SCOPE_ID)

val ParadoxLocalisationCommandField.commandFieldId: PsiElement? get() = findOptionalChild(COMMAND_FIELD_ID)
