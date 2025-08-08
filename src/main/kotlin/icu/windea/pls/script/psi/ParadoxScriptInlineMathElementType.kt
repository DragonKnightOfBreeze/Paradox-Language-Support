package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.tree.*
import icu.windea.pls.script.*
import icu.windea.pls.script.lexer.*
import icu.windea.pls.script.parser.*

@Suppress("UnstableApiUsage")
class ParadoxScriptInlineMathElementType(debugName: String) : IReparseableElementType(debugName, ParadoxScriptLanguage) {
    companion object {
        private const val invalidChars = "\r\n#{}[]"
    }

    override fun parseContents(chameleon: ASTNode): ASTNode {
        val psi = chameleon.treeParent.psi
        val project: Project = psi.project
        val lexer = ParadoxScriptLexerFactory.createInlineMathLexer(project)
        val languageForParser = ParadoxScriptLanguage
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, chameleon.chars)
        val parser = ParadoxScriptParser()
        val startTime = System.nanoTime()
        val node = parser.parse(ParadoxScriptElementTypes.INLINE_MATH_ROOT, builder)
        ParsingDiagnostics.registerParse(builder, languageForParser, System.nanoTime() - startTime)
        return node.firstChildNode
    }

    override fun isReparseable(currentNode: ASTNode, newText: CharSequence, fileLanguage: Language, project: Project): Boolean {
        return newText.none { it in invalidChars }
    }

    override fun createNode(text: CharSequence?): ASTNode {
        return LazyParseablePsiElement(this, text)
    }
}
