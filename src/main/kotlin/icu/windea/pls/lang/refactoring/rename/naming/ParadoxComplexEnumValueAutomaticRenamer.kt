package icu.windea.pls.lang.refactoring.rename.naming

import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement

/**
 * 用于在重命名复杂枚举值时，自动重命名相关项。
 *
 * @see ParadoxComplexEnumValueLightElement
 */
abstract class ParadoxComplexEnumValueAutomaticRenamer : ParadoxAutomaticRenamer() {
    abstract class Factory : ParadoxAutomaticRenamer.Factory()
}
