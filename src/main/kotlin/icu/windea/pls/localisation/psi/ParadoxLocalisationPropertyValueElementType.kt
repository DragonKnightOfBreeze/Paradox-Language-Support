package icu.windea.pls.localisation.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.PsiBuilderFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.ParsingDiagnostics
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IReparseableElementType
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.lexer.ParadoxLocalisationLexerFactory
import icu.windea.pls.localisation.parser.ParadoxLocalisationParser

@Suppress("UnstableApiUsage")
class ParadoxLocalisationPropertyValueElementType(debugName: String) : IReparseableElementType(debugName, ParadoxLocalisationLanguage) {
    companion object {
        private const val invalidChars = "\r\n"
    }

    @Optimized
    override fun parseContents(chameleon: ASTNode): ASTNode {
        val chars = chameleon.chars
        // 不包含任何特殊标记时，跳过完整解析，直接构建纯文本 AST（TEXT > TEXT_TOKEN）
        // 这是一个保守的过近似，带转义标记的文本（如 `\$`）会走完整解析
        if (!ParadoxLocalisationManager.isRichText(chars, checkEscape = false)) return buildPlainTextNode(chars)
        return parseContentsFully(chameleon)
    }

    private fun parseContentsFully(chameleon: ASTNode): ASTNode {
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

    private fun buildPlainTextNode(chars: CharSequence): ASTNode {
        val textNode = CompositeElement(ParadoxLocalisationElementTypes.TEXT)
        val leafNode = LeafPsiElement(ParadoxLocalisationElementTypes.TEXT_TOKEN, chars)
        textNode.rawAddChildren(leafNode)
        return textNode
    }

    override fun isReparseable(currentNode: ASTNode, newText: CharSequence, fileLanguage: Language, project: Project): Boolean {
        return newText.none { it in invalidChars }
    }

    override fun createNode(text: CharSequence?): ASTNode {
        return LazyParseablePsiElement(this, text)
    }
}
