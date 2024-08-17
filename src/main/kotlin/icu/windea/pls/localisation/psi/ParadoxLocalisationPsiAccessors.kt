package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.impl.*

val ParadoxLocalisationLocale.localeId: PsiElement get() = findChild(LOCALE_TOKEN)!!

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement get() = findChild(PROPERTY_KEY_TOKEN)!!

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement? get() = findChild(PROPERTY_REFERENCE_TOKEN)
val ParadoxLocalisationPropertyReference.propertyReferenceParameter: PsiElement? get() = findChild(PROPERTY_REFERENCE_PARAMETER_TOKEN)

val ParadoxLocalisationIcon.iconId: PsiElement? get() = findChild(ICON_TOKEN)
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

val ParadoxLocalisationColorfulText.idElement: PsiElement? get() = findChild(COLOR_TOKEN)

val ParadoxLocalisationCommand.idElement: PsiElement? get() = findChildren(COMMAND_TEXT_TOKEN).singleOrNull()

val ParadoxLocalisationConceptName.idElement: PsiElement? get() = findChildren(CONCEPT_NAME_TOKEN).singleOrNull()

val ParadoxLocalisationScriptedVariableReference.idElement: PsiElement? get() = findChildren(SCRIPTED_VARIABLE_REFERENCE_TOKEN).singleOrNull()

val ParadoxLocalisationProperty.greenStub: ParadoxLocalisationPropertyStub?
    get() = this.castOrNull<ParadoxLocalisationPropertyImpl>()?.greenStub ?: this.stub
