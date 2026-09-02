package icu.windea.pls.localisation.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ParadoxLocalisationSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): ParadoxLocalisationSyntaxHighlighter {
        return ParadoxLocalisationSyntaxHighlighter(project, virtualFile)
    }
}

