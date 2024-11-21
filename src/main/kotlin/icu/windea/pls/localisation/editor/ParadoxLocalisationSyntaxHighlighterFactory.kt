package icu.windea.pls.localisation.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*

class ParadoxLocalisationSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): ParadoxLocalisationSyntaxHighlighter {
        return ParadoxLocalisationSyntaxHighlighter(project, virtualFile)
    }
}

