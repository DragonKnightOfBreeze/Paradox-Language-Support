package com.windea.plugin.idea.paradox.script.highlighter

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.windea.plugin.idea.paradox.script.highlighter.*

class ParadoxScriptSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
	override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) = ParadoxScriptSyntaxHighlighter()
}
