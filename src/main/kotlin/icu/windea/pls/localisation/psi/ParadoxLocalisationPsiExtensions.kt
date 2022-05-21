package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

val ParadoxLocalisationLocale.localeId: PsiElement get() = findRequiredChild(LOCALE_ID)

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement get() = findRequiredChild(PROPERTY_KEY_ID)

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement? get() = findOptionalChild(PROPERTY_REFERENCE_ID)

val ParadoxLocalisationIcon.iconId: PsiElement? get() = findOptionalChild(ICON_ID)
val ParadoxLocalisationIcon.iconFrame: PsiElement? get() = findOptionalChild(ICON_FRAME)

val ParadoxLocalisationColorfulText.colorId: PsiElement? get() = findOptionalChild(COLOR_ID)

val ParadoxLocalisationCommandScope.commandScopeId: PsiElement get() = findRequiredChild(COMMAND_SCOPE_ID)

val ParadoxLocalisationCommandField.commandFieldId: PsiElement? get() = findOptionalChild(COMMAND_FIELD_ID)
