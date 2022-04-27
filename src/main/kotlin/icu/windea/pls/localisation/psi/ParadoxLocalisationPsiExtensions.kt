package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

val ParadoxLocalisationLocale.localeId: PsiElement get() = findRequiredChild(LOCALE_ID)

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement get() = findRequiredChild(PROPERTY_KEY_ID)

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement? get() = findChild(PROPERTY_REFERENCE_ID)

val ParadoxLocalisationIcon.iconId: PsiElement? get() = findChild(ICON_ID)

val ParadoxLocalisationSequentialNumber.sequentialNumberId: PsiElement? get() = findChild(SEQUENTIAL_NUMBER_ID)

val ParadoxLocalisationColorfulText.colorId: PsiElement? get() = findChild(COLOR_ID)

val ParadoxLocalisationCommandScope.commandScopeId: PsiElement get() = findRequiredChild(COMMAND_SCOPE_ID)

val ParadoxLocalisationCommandField.commandFieldId: PsiElement? get() = findChild(COMMAND_FIELD_ID)

