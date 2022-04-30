package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.script.psi.*

fun PsiElement.isParadoxLocalisationPsiElement() : Boolean{
	val elementType = this.elementType?:return false
	return elementType is ParadoxLocalisationTokenType || elementType is ParadoxLocalisationElementType
}

val ParadoxLocalisationLocale.localeId: PsiElement get() = findRequiredChild(LOCALE_ID)

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement get() = findRequiredChild(PROPERTY_KEY_ID)

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement? get() = findChild(PROPERTY_REFERENCE_ID)

val ParadoxLocalisationIcon.iconId: PsiElement? get() = findChild(ICON_ID)

val ParadoxLocalisationSequentialNumber.sequentialNumberId: PsiElement? get() = findChild(SEQUENTIAL_NUMBER_ID)

val ParadoxLocalisationColorfulText.colorId: PsiElement? get() = findChild(COLOR_ID)

val ParadoxLocalisationCommandScope.commandScopeId: PsiElement get() = findRequiredChild(COMMAND_SCOPE_ID)

val ParadoxLocalisationCommandField.commandFieldId: PsiElement? get() = findChild(COMMAND_FIELD_ID)

