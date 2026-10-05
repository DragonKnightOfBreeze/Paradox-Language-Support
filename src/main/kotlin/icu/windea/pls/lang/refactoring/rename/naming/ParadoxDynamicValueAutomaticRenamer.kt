package icu.windea.pls.lang.refactoring.rename.naming

import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement

/**
 * 用于在重命名动态值时，自动重命名相关项。
 *
 * @see ParadoxDynamicValueLightElement
 */
abstract class ParadoxDynamicValueAutomaticRenamer : ParadoxAutomaticRenamer() {
    abstract class Factory : ParadoxAutomaticRenamer.Factory()
}
