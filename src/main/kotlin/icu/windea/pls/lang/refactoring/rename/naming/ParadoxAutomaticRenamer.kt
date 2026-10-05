package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory

abstract class ParadoxAutomaticRenamer : AutomaticRenamer() {
    abstract class Factory : AutomaticRenamerFactory
}
