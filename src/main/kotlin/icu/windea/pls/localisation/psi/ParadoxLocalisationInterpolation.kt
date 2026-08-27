package icu.windea.pls.localisation.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.psi.PsiPresentableElement

/**
 * 可以作为插值在特定位置使用的 PSI 元素。
 *
 * 说明：
 * - 基本上，这些高级插值语法，以及对应的标识符/字面量的词元，可以任意组合使用。
 * - 认为参数（[ParadoxLocalisationParameter]）不能在参数自身（包括变体，以及任意深度的子节点）中使用。
 * - 认为命令（[ParadoxLocalisationCommand]）不能在命令自身（包括变体，以及任意深度的子节点）中使用。
 *
 * @see ParadoxLocalisationParameter
 * @see ParadoxLocalisationCommand
 */
interface ParadoxLocalisationInterpolation: NavigatablePsiElement, PsiPresentableElement
