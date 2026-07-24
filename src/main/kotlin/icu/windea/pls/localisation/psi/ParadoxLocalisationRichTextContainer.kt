package icu.windea.pls.localisation.psi

import com.intellij.psi.NavigatablePsiElement

/**
 * 富文本容器。可以直接包含各类富文本（[ParadoxLocalisationRichText]）。也包括属性值（[ParadoxLocalisationPropertyValue]）自身。
 *
 * @see ParadoxLocalisationPropertyValue
 * @see ParadoxLocalisationColorfulText
 * @see ParadoxLocalisationConceptText
 * @see ParadoxLocalisationTextFormatText
 */
interface ParadoxLocalisationRichTextContainer : NavigatablePsiElement/*not:*//*, PsiListLikeElement*/ {
    val richTextList: List<ParadoxLocalisationRichText> get() = emptyList()
}
