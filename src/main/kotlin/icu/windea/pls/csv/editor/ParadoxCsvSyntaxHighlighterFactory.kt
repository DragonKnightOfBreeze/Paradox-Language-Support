package icu.windea.pls.csv.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ParadoxCsvSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): ParadoxCsvSyntaxHighlighter {
        return ParadoxCsvSyntaxHighlighter(project, virtualFile)
    }
}
