package icu.windea.pls.lang.refactoring.rename.naming

/**
 * 用于在重命名封装变量时，自动重命名相关项。
 */
abstract class ParadoxScriptedVariableAutomaticRenamer : ParadoxAutomaticRenamer() {
    abstract class Factory : ParadoxAutomaticRenamer.Factory()
}
