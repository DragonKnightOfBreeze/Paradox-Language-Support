package icu.windea.pls.lang.refactoring.rename.naming

/**
 * 用于在重命名本地化时，自动重命名相关项。
 */
abstract class ParadoxLocalisationAutomaticRenamer : ParadoxAutomaticRenamer() {
    abstract class Factory : ParadoxAutomaticRenamer.Factory()
}
