package icu.windea.pls.core.psi

import icu.windea.pls.*
import icu.windea.pls.core.codeInsight.generation.*
import icu.windea.pls.localisation.*
import icu.windea.pls.tool.*

object ParadoxGenerator {
    //TODO
    
    fun generateLocalisations(context: GenerateLocalisationsContext) {
        val name = "generated_localisations.yml"
        val localeConfig = preferredParadoxLocale() ?: return //unexpected
        val text =  buildString {
            append(localeConfig.id).append(":\n")
            //val indentSize = CodeStyle.getIndentOptions(file).INDENT_SIZE
            val indentSize = 1
            val indent = " ".repeat(indentSize)
            for(localisationName in context.localisationNames) {
                appendLocalisationLine(indent, localisationName)
            }
        }
        ParadoxFileManager.createLightFile(name, text, ParadoxLocalisationLanguage)
    }
    
    fun generateLocalisationsInFile(context: GenerateLocalisationsInFileContext) {
        val name = "generated_localisations.yml"
        val localeConfig = preferredParadoxLocale() ?: return //unexpected
        val text =  buildString {
            append(localeConfig.id).append(":\n")
            //val indentSize = CodeStyle.getIndentOptions(file).INDENT_SIZE
            val indentSize = 1
            val indent = " ".repeat(indentSize)
            for(context0 in context.contextList) {
                for(localisationName in context0.localisationNames) {
                    appendLocalisationLine(indent, localisationName)
                }
            }
        }
        ParadoxFileManager.createLightFile(name, text, ParadoxLocalisationLanguage)
    }
    
    private fun StringBuilder.appendLocalisationLine(indent: String, localisationName: String) {
        append(indent)
        append(localisationName)
        append(": \"")
        append("REPLACE_ME")
        append("\"")
    }
}