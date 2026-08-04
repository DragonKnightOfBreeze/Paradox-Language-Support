package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationIconPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextColorPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextFormatPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextIconPsiReference
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo

/**
 * 定义信息的受约束索引。
 *
 * 用于优化和调整符合特定约束的定义声明的索引逻辑。
 *
 * @see ParadoxDefinitionIndex
 * @see ParadoxDefinitionIndexConstraint
 */
abstract class ParadoxDefinitionConstrainedIndex : ParadoxDefinitionIndex() {
    abstract val constraint: ParadoxDefinitionIndexConstraint

    override fun getName() = constraint.indexKey

    override fun indexData(psiFile: PsiFile): Map<String, List<ParadoxDefinitionIndexInfo>> {
        // TODO 3.0.1
        return emptyMap()
    }

    /**
     * 用于快速索引文本颜色。它们是 [ParadoxLocalisationTextColorPsiReference] 的解析目标。
     *
     * @see ParadoxDefinitionIndexConstraint.TextColor
     */
    class TextColorIndex : ParadoxDefinitionConstrainedIndex() {
        override val constraint get() = ParadoxDefinitionIndexConstraint.TextColor
    }

    /**
     * 用于快速索引文本图标。它们是 [ParadoxLocalisationTextIconPsiReference] 的解析目标。
     *
     * @see ParadoxDefinitionIndexConstraint.TextIcon
     */
    class TextIconIndex : ParadoxDefinitionConstrainedIndex() {
        override val constraint get() = ParadoxDefinitionIndexConstraint.TextIcon
    }

    /**
     * 用于快速索引文本格式。它们是 [ParadoxLocalisationTextFormatPsiReference] 的解析目标。
     *
     * @see ParadoxDefinitionIndexConstraint.TextFormat
     */
    class TextFormatIndex : ParadoxDefinitionConstrainedIndex() {
        override val constraint get() = ParadoxDefinitionIndexConstraint.TextFormat
    }

    /**
     * 用于快速索引可能是 [ParadoxLocalisationIconPsiReference] 的解析目标的定义信息。
     *
     * @see ParadoxDefinitionIndexConstraint.LocalisationIconResolvable
     */
    class LocalisationIconResolvableIndex : ParadoxDefinitionConstrainedIndex() {
        override val constraint get() = ParadoxDefinitionIndexConstraint.LocalisationIconResolvable
    }
}
