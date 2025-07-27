package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.*

object ParadoxCsvManipulator {
    fun buildSelectedRowSequence(editor: Editor, file: PsiFile): Sequence<ParadoxCsvRow> {
        return emptySequence() //TODO 2.0.1-dev
    }

    fun buildSelectedColumnSequence(editor: Editor, file: PsiFile): Sequence<ParadoxCsvColumn> {
        return emptySequence() //TODO 2.0.1-dev
    }
}
