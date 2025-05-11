package icu.windea.pls.cwt.psi

import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.lexer.*
import icu.windea.pls.cwt.parser.*

@Suppress("UnstableApiUsage")
class CwtOptionCommentElementType(debugName: String) : IReparseableElementType(debugName, CwtLanguage) {
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
        return newText.startsWith("##") && newText.none { it == '\r' || it == '\n' }
    }

    override fun createNode(text: CharSequence?): ASTNode {
        return LazyParseablePsiElement(this, text)
    }
}
