package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ParadoxScriptSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): ParadoxScriptSyntaxHighlighter {
        return ParadoxScriptSyntaxHighlighter(project, virtualFile)
    }
}
