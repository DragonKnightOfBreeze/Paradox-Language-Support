package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.lexer.*
import icu.windea.pls.localisation.parser.*

@Suppress("UnstableApiUsage")
class ParadoxLocalisationPropertyValueElementType(debugName: String) : IReparseableElementType(debugName, ParadoxLocalisationLanguage) {
    override fun parseContents(chameleon: ASTNode): ASTNode {
        val psi = chameleon.treeParent.psi
        val project: Project = psi.project
        val lexer = ParadoxLocalisationLexerFactory.createTextLexer(project, selectGameType(psi))
        val languageForParser = ParadoxLocalisationLanguage
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, chameleon.chars)
        val parser = ParadoxLocalisationParser()
        val startTime = System.nanoTime()
        val node = parser.parse(ParadoxLocalisationElementTypes.TEXT_ROOT, builder)
        ParsingDiagnostics.registerParse(builder, languageForParser, System.nanoTime() - startTime)
        return node.firstChildNode
    }

    override fun isReparseable(currentNode: ASTNode, newText: CharSequence, fileLanguage: Language, project: Project): Boolean {
        return newText.none { it == '\r' || it == '\n' }
    }

    override fun createNode(text: CharSequence?): ASTNode {
        return LazyParseablePsiElement(this, text)
    }
}
