package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.impl.*

val ParadoxLocalisationLocale.localeId: PsiElement
    get() = findChild { it.elementType == LOCALE_TOKEN }!!

val ParadoxLocalisationPropertyKey.propertyKeyId: PsiElement
    get() = findChild { it.elementType == PROPERTY_KEY_TOKEN }!!

val ParadoxLocalisationPropertyReference.propertyReferenceId: PsiElement?
    get() = findChild { it.elementType == PROPERTY_REFERENCE_TOKEN }
val ParadoxLocalisationPropertyReference.propertyReferenceParameter: PsiElement?
    get() = findChild { it.elementType == PROPERTY_REFERENCE_PARAMETER_TOKEN }

val ParadoxLocalisationIcon.iconId: PsiElement?
    get() = findChild { it.elementType == ICON_TOKEN }
val ParadoxLocalisationIcon.iconIdReference: ParadoxLocalisationPropertyReference?
    get() {
        forEachChild {
            if (it is ParadoxLocalisationPropertyReference) return it
            if (it.elementType == PIPE) return null
        }
        return null
    }
val ParadoxLocalisationIcon.iconFrame: PsiElement?
    get() = findChild { it.elementType == ICON_FRAME }
val ParadoxLocalisationIcon.iconFrameReference: ParadoxLocalisationPropertyReference?
    get() {
        var afterPipe = false
        forEachChild {
            if (afterPipe && it is ParadoxLocalisationPropertyReference) return it
            if (it.elementType == PIPE) afterPipe = true
        }
        return null
    }

val ParadoxLocalisationColorfulText.idElement: PsiElement?
    get() = findChild { it.elementType == COLOR_TOKEN }

val ParadoxLocalisationCommand.idElement: PsiElement?
    get() = findChildren { it.elementType == COMMAND_TEXT_TOKEN }.singleOrNull()

val ParadoxLocalisationConceptName.idElement: PsiElement?
    get() = findChildren { it.elementType == CONCEPT_NAME_TOKEN }.singleOrNull()

val ParadoxLocalisationScriptedVariableReference.idElement: PsiElement?
    get() = findChildren { it.elementType == SCRIPTED_VARIABLE_REFERENCE_TOKEN }.singleOrNull()

val ParadoxLocalisationProperty.greenStub: ParadoxLocalisationPropertyStub?
    get() = this.castOrNull<ParadoxLocalisationPropertyImpl>()?.greenStub
