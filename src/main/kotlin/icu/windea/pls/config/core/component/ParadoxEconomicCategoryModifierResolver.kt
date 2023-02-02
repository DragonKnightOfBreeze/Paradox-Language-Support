package icu.windea.pls.config.core.component

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*
import org.jetbrains.kotlin.idea.core.util.*
import java.io.*

/**
 * 通过经济类型（`economic_category`）生成修饰符。
 */
@WithGameType(ParadoxGameType.Stellaris)
class ParadoxEconomicCategoryModifierResolver: ParadoxModifierResolver {
    // will be generated based on detailed declaration
    // <key>_<resource>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
    // 
    // will be generated if use_for_ai_budget = yes
    // <key>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
    
    companion object {
        @JvmField val economicCategoryInfoKey = Key.create<ParadoxEconomicCategoryInfo>("paradox.modifierElement.economicCategoryInfo")
        @JvmField val economicCategoryModifierInfoKey = Key.create<ParadoxEconomicCategoryModifierInfo>("paradox.modifierElement.economicCategoryModifierInfo")
        
        const val economicCategoriesDirPath = "common/economic_categories"
        const val economicCategoriesPathExpression = "common/economic_categories/,.txt"
        
        private val valueExternalizer: DataExternalizer<List<ParadoxEconomicCategoryInfo>> = object : DataExternalizer<List<ParadoxEconomicCategoryInfo>> {
            override fun save(storage: DataOutput, info: List<ParadoxEconomicCategoryInfo>) {
                val json = jsonMapper.writeValueAsString(info)
                storage.writeString(json)
            }
        
            override fun read(storage: DataInput): List<ParadoxEconomicCategoryInfo> {
                val json = storage.readString()
                return jsonMapper.readValue(json)
            }
        }
    
        private val gist: PsiFileGist<List<ParadoxEconomicCategoryInfo>> = GistManager.getInstance().newPsiFileGist("ParadoxEconomicCategoryInfo", 1, valueExternalizer) { file->
            //TODO
            emptyList()
        }
    }
    
    override fun matchModifier(name: String, configGroup: CwtConfigGroup, matchType: Int): Boolean {
        if(configGroup.gameType != ParadoxGameType.Stellaris) return false
        var r = false
        processEconomicCategoryInfos(configGroup) p@{
            for(modifierInfo in it.modifiers) {
                if(modifierInfo.name == name) {
                    r = true
                    return@p false
                }
            }
            true
        }
        return r
    }
    
    override fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
        if(configGroup.gameType != ParadoxGameType.Stellaris) return null
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return null
        var economicCategoryInfo: ParadoxEconomicCategoryInfo? = null
        var economicCategoryModifierInfo: ParadoxEconomicCategoryModifierInfo? = null
        processEconomicCategoryInfos(configGroup) p@{
            for(modifierInfo in it.modifiers) {
                if(modifierInfo.name == name) {
                    economicCategoryInfo = it
                    economicCategoryModifierInfo = modifierInfo
                    return@p false
                }
            }
            true
        }
        if(economicCategoryInfo == null || economicCategoryModifierInfo == null) return null
        val result = ParadoxModifierElement(element, name, null, gameType, project)
        result.putUserData(economicCategoryInfoKey, economicCategoryInfo)
        result.putUserData(economicCategoryModifierInfoKey, economicCategoryModifierInfo)
        return result
    }
    
    private fun processEconomicCategoryInfos(configGroup: CwtConfigGroup, contextElement: PsiElement? = null, processor: (ParadoxEconomicCategoryInfo) -> Boolean) {
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return
        val selector = fileSelector().gameType(gameType).preferRootFrom(contextElement)
        ParadoxFilePathSearch.search(economicCategoriesPathExpression, configGroup.project, CwtPathExpressionType.FilePath, selector = selector).processQuery {
            val file = it.toPsiFile<ParadoxScriptFile>(project) ?: return@processQuery true
            for(info in gist.getFileData(file)) {
                val r = processor(info)
                if(!r) return@processQuery false
            }
            true
        }
    }
    
    override fun buildDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean {
        val economicCategoryInfo = element.getUserData(economicCategoryInfoKey) ?: return false
        val economicCategoryModifierInfo = element.getUserData(economicCategoryModifierInfoKey) ?: return false
        
        return false
    }
}