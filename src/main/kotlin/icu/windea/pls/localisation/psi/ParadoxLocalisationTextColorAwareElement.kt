package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

/**
 * @see ParadoxLocalisationColorfulText
 * @see ParadoxLocalisationPropertyReferenceArgument
 * @see ParadoxLocalisationCommandArgument
 */
interface ParadoxLocalisationTextColorAwareElement : PsiElement {
    val colorId: String? get() = ParadoxTextColorManager.getId(this)
    val colorInfo: ParadoxTextColorInfo? get() = ParadoxTextColorManager.getInfo(this)
}
