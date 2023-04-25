package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*

val ParadoxLocalisationLocale.localeId: PsiElement get() = findChild(ParadoxLocalisationElementTypes.LOCALE_ID)!!

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement get() = findChild(ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN)!!

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement? get() = findChild(ParadoxLocalisationElementTypes.PROPERTY_REFERENCE_ID)
val ParadoxLocalisationPropertyReference.propertyReferenceParameter: PsiElement? get() = findChild(ParadoxLocalisationElementTypes.PROPERTY_REFERENCE_PARAMETER_TOKEN)

val ParadoxLocalisationIcon.iconId: PsiElement? get() = findChild(ParadoxLocalisationElementTypes.ICON_ID)
val ParadoxLocalisationIcon.iconIdReference: ParadoxLocalisationPropertyReference?
	get() {
		forEachChild {
			if(it is ParadoxLocalisationPropertyReference) return it
			if(it.elementType == ParadoxLocalisationElementTypes.PIPE) return null
		}
		return null
	}
val ParadoxLocalisationIcon.iconFrame: PsiElement? get() = findChild(ParadoxLocalisationElementTypes.ICON_FRAME)
val ParadoxLocalisationIcon.iconFrameReference: ParadoxLocalisationPropertyReference?
	get() {
		var afterPipe = false
		forEachChild {
			if(afterPipe && it is ParadoxLocalisationPropertyReference) return it
			if(it.elementType == ParadoxLocalisationElementTypes.PIPE) afterPipe = true
		}
		return null
	}

val ParadoxLocalisationColorfulText.colorId: PsiElement? get() = findChild(ParadoxLocalisationElementTypes.COLOR_ID)

val ParadoxLocalisationScriptedVariableReference.variableReferenceId: PsiElement get() = findChild(ParadoxLocalisationElementTypes.SCRIPTED_VARIABLE_REFERENCE_ID)!!
