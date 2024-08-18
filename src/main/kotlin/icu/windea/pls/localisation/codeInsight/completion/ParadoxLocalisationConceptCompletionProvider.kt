package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供概念的名字和别名的代码补全。
 */
class ParadoxLocalisationConceptCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationConceptName>() ?: return
        if(element.text.isParameterized()) return
        if(element.isDatabaseObjectExpression()) return
        
        val file = parameters.originalFile
        val project = file.project
        
        ParadoxCompletionManager.initializeContext(parameters, context)
        
        //提示concept的name或alias
        val conceptSelector = definitionSelector(project, file).contextSensitive().distinctByName()
        val keysToDistinct = mutableSetOf<String>()
        ParadoxDefinitionSearch.search("game_concept", conceptSelector).processQueryAsync p@{ concept ->
            val tailText = " from concepts"
            val typeFile = concept.containingFile
            val icon = PlsIcons.LocalisationNodes.Concept
            run action@{
                val key = concept.name
                if(!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(concept, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withCompletionId()
                result.addElement(lookupElement, context)
            }
            concept.getData<StellarisGameConceptData>()?.alias?.forEach action@{ alias ->
                val key = alias
                if(!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(concept, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withCompletionId()
                result.addElement(lookupElement, context)
            }
            true
        }
    }
}
