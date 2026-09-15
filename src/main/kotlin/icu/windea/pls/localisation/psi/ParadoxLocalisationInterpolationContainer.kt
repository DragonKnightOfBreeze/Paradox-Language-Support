package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.children
import icu.windea.pls.core.select.listBy

/**
 * 插值容器，可以直接包含各类插值（[ParadoxLocalisationInterpolation]）。这意味着其标识符/字面量可能是参数化的。
 *
 * 说明：
 * - 基本上，这些高级插值语法，以及对应的标识符/字面量的词元，可以任意组合使用。
 * - 认为参数（[ParadoxLocalisationParameter]）不能在参数自身（包括变体，以及任意深度的子节点）中使用。
 * - 认为命令（[ParadoxLocalisationCommand]）不能在命令自身（包括变体，以及任意深度的子节点）中使用。
 *
 * @see ParadoxLocalisationParameter
 * @see ParadoxLocalisationIcon
 * @see ParadoxLocalisationIconArgument
 * @see ParadoxLocalisationCommandText
 * @see ParadoxLocalisationCommandArgument
 * @see ParadoxLocalisationConceptName
 * @see ParadoxLocalisationTextIcon
 * @see ParadoxLocalisationTextFormat
 */
@Suppress("unused")
interface ParadoxLocalisationInterpolationContainer : PsiElement {
    val interpolations: List<ParadoxLocalisationInterpolation> get() = children().listBy()
    val parameters: List<ParadoxLocalisationParameter> get() = children().listBy()
    val commands: List<ParadoxLocalisationCommand> get() = children().listBy()
}
