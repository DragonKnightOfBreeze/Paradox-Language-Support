package icu.windea.pls.csv.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*

class ParadoxCsvSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): ParadoxCsvSyntaxHighlighter {
        return ParadoxCsvSyntaxHighlighter(project, virtualFile)
    }
}
