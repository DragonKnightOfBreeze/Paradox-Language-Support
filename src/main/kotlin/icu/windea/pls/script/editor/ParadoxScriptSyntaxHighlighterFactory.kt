package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*

class ParadoxScriptSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): ParadoxScriptSyntaxHighlighter {
        return ParadoxScriptSyntaxHighlighter(project, virtualFile)
    }
}
