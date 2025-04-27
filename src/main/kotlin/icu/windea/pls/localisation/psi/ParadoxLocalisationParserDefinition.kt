package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.*

open class ParadoxLocalisationParserDefinition(
    val gameType: ParadoxGameType? = null
) : ParserDefinition {
    override fun getWhitespaceTokens() = ParadoxLocalisationTokenSets.WHITE_SPACES

    override fun getCommentTokens() = ParadoxLocalisationTokenSets.COMMENTS

    override fun getStringLiteralElements() = ParadoxLocalisationTokenSets.STRING_LITERALS

    override fun getFileNodeType() = ParadoxLocalisationFileStubElementType.forGameType(gameType)

    override fun createFile(viewProvider: FileViewProvider): ParadoxLocalisationFile {
        return ParadoxLocalisationFile(viewProvider, gameType)
    }

    override fun createElement(node: ASTNode): PsiElement {
        return Factory.createElement(node)
    }

    override fun createParser(project: Project?): ParadoxLocalisationParser {
        return ParadoxLocalisationParser()
    }

    override fun createLexer(project: Project?): ParadoxLocalisationLexer {
        return ParadoxLocalisationLexer(gameType)
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
        val leftType = left?.elementType
        val rightType = right?.elementType
        return when {
            leftType == COMMENT -> MUST_LINE_BREAK
            leftType == LOCALE_TOKEN && rightType == COLON -> MUST_NOT
            rightType == LOCALE_TOKEN -> MUST_LINE_BREAK
            rightType == PROPERTY_KEY_TOKEN -> MUST_LINE_BREAK
            leftType == COLORFUL_TEXT_START && rightType == COLOR_TOKEN -> MUST_NOT
            leftType == ICON_START || rightType == ICON_END -> MUST_NOT
            leftType == PROPERTY_REFERENCE_START || rightType == PROPERTY_REFERENCE_END -> MUST_NOT
            leftType == PIPE || rightType == PIPE -> MUST_NOT
            else -> MAY
        }
    }

    class Stellaris : ParadoxLocalisationParserDefinition(ParadoxGameType.Stellaris)
    class Ck2 : ParadoxLocalisationParserDefinition(ParadoxGameType.Ck2)
    class Ck3 : ParadoxLocalisationParserDefinition(ParadoxGameType.Ck3)
    class Eu4 : ParadoxLocalisationParserDefinition(ParadoxGameType.Eu4)
    class Hoi4 : ParadoxLocalisationParserDefinition(ParadoxGameType.Hoi4)
    class Ir : ParadoxLocalisationParserDefinition(ParadoxGameType.Ir)
    class Vic2 : ParadoxLocalisationParserDefinition(ParadoxGameType.Vic2)
    class Vic3 : ParadoxLocalisationParserDefinition(ParadoxGameType.Vic3)
}


