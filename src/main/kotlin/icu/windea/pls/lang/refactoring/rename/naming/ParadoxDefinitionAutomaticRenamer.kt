package icu.windea.pls.lang.refactoring.rename.naming

/**
 * 用于在重命名定义时，自动重命名相关项。
 */
abstract class ParadoxDefinitionAutomaticRenamer : ParadoxAutomaticRenamer() {
    abstract class Factory : ParadoxAutomaticRenamer.Factory()
}
