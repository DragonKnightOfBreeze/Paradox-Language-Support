package com.windea.plugin.idea.paradox.localisation.highlighter

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*

class ParadoxLocalisationSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
	override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) = ParadoxLocalisationSyntaxHighlighter()
}

