@file:Suppress("unused", "UnusedVariable")

package icu.windea.pls.lang.util

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.csv.psi.*

object ParadoxCsvManager {
    const val SEPARATOR = ';'

    fun getSeparator(): Char {
        return SEPARATOR
    }

    //stub methods

    fun createStub(psi: ParadoxCsvRow, parentStub: StubElement<*>): ParadoxCsvRowStub? {
        //TODO 2.0.1.dev
        //val file = selectFile(psi) ?: return null
        //val gameType = selectGameType(file) ?: return null
        return null
    }

    fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxCsvRowStub? {
        //TODO 2.0.1.dev
        //val psi = parentStub.psi
        //val file = selectFile(psi) ?: return null
        //val gameType = selectGameType(file) ?: return null
        return null
    }
}
