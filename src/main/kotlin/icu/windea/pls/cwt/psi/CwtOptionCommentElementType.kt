package icu.windea.pls.cwt.psi

import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.*

@Suppress("UnstableApiUsage")
class CwtOptionCommentElementType(debugName: String) : IReparseableElementType(debugName, CwtLanguage) {
    //TODO 1.4.0

    override fun parseContents(chameleon: ASTNode): ASTNode {
        val psi = chameleon.treeParent.psi!!
        val project: Project = psi.project
        val lexer = CwtParserDefinition.General.createOptionLexer(project)
        val languageForParser = CwtLanguage
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, chameleon.chars)
        val parser = CwtParserDefinition.General.createOptionParser(project)
        val startTime = System.nanoTime()
        val node = parser.parse(this, builder)
        ParsingDiagnostics.registerParse(builder, languageForParser, System.nanoTime() - startTime)
        return node.firstChildNode
    }

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun isParsable(parent: ASTNode?, buffer: CharSequence, fileLanguage: Language, project: Project): Boolean {
        return buffer.startsWith("##")/* && buffer.none { it == '\r' || it == '\n' }*/
    }
}
