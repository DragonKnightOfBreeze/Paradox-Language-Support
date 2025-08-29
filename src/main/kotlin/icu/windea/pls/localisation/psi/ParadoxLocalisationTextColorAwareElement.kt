package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.model.ParadoxTextColorInfo

/**
 * @see ParadoxLocalisationColorfulText
 * @see ParadoxLocalisationParameterArgument
 * @see ParadoxLocalisationCommandArgument
 */
interface ParadoxLocalisationTextColorAwareElement : PsiElement {
    val colorId: String? get() = ParadoxTextColorManager.getId(this)
    val colorInfo: ParadoxTextColorInfo? get() = ParadoxTextColorManager.getInfo(this)
}
