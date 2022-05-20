package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

val ParadoxLocalisationLocale.localeId: PsiElement get() = findRequiredChild(LOCALE_ID)

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement get() = findRequiredChild(PROPERTY_KEY_ID)

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement? get() = findChild(PROPERTY_REFERENCE_ID)

val ParadoxLocalisationIcon.iconId: PsiElement? get() = findChild(ICON_ID)

val ParadoxLocalisationSequentialNumber.sequentialNumberId: PsiElement? get() = findChild(SEQUENTIAL_NUMBER_ID)

val ParadoxLocalisationColorfulText.colorId: PsiElement? get() = findChild(COLOR_ID)

val ParadoxLocalisationCommandScope.commandScopeId: PsiElement get() = findRequiredChild(COMMAND_SCOPE_ID)

val ParadoxLocalisationCommandField.commandFieldId: PsiElement? get() = findChild(COMMAND_FIELD_ID)

fun ParadoxLocalisationLocale.changeLocaleId(id: String) {
	val newId = ParadoxLocalisationElementFactory.createLocale(project, id).localeId
	this.localeId.replace(newId)
}

fun ParadoxLocalisationSequentialNumber.changeSequentialNumberId(id: String) {
	val newId = ParadoxLocalisationElementFactory.createSequentialNumber(project, id).sequentialNumberId ?: return
	this.sequentialNumberId?.replace(newId)
}

fun ParadoxLocalisationColorfulText.changeColorId(id: String) {
	val newId = ParadoxLocalisationElementFactory.createColorfulText(project, id).colorId ?: return
	this.colorId?.replace(newId)
}