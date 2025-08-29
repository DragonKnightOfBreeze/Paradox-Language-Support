package icu.windea.pls.cwt.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.PsiBuilderFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.ParsingDiagnostics
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.IReparseableElementType
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.lexer.CwtLexerFactory
import icu.windea.pls.cwt.parser.CwtParser

@Suppress("UnstableApiUsage")
class CwtOptionCommentElementType(debugName: String) : IReparseableElementType(debugName, CwtLanguage) {
    companion object {
        private const val invalidChars = "\r\n"
    }

    override fun parseContents(chameleon: ASTNode): ASTNode {
        val psi = chameleon.treeParent.psi
        val project: Project = psi.project
        val lexer = CwtLexerFactory.createOptionCommentLexer(project)
        val languageForParser = CwtLanguage
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, chameleon.chars)
        val parser = CwtParser()
        val startTime = System.nanoTime()
        val node = parser.parse(CwtElementTypes.OPTION_COMMENT_ROOT, builder)
        ParsingDiagnostics.registerParse(builder, languageForParser, System.nanoTime() - startTime)
        return node.firstChildNode
    }

    override fun isReparseable(currentNode: ASTNode, newText: CharSequence, fileLanguage: Language, project: Project): Boolean {
        return newText.startsWith("##") && newText.none { it in invalidChars }
    }

    override fun createNode(text: CharSequence?): ASTNode {
        return LazyParseablePsiElement(this, text)
    }
}
