package icu.windea.pls.config.configGroup

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupPostProcessor
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupProcessor
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class CwtConfigGroupBase(
    override val project: Project,
    override val gameType: ParadoxGameType,
) : UserDataHolderBase(), CwtConfigGroup, CwtConfigGroupDataModel {
    private val mutex = Mutex()
    @Volatile private var _dataModel: CwtConfigGroupDataModel? = null
    @Volatile private var _initializer: CwtConfigGroupDataModelBase? = null

    @Volatile override var initialized = false
    @Volatile override var changed = false
    override val modificationTracker = SimpleModificationTracker()
    override val dataModel: CwtConfigGroupDataModel get() = _dataModel ?: CwtConfigGroupDataModel.Empty
    override val initializer: CwtConfigGroupDataModelBase get() = _initializer ?: CwtConfigGroupDataModelBase()

    override suspend fun init() {
        // 即使规则数据已全部加载完毕，也可能需要再次重新加载
        mutex.withLock { doInit() }
    }

    private suspend fun doInit() {
        try {
            val start = System.currentTimeMillis()
            _initializer = CwtConfigGroupDataModelBase()
            doApplyProcessors() // 应用 processors
            _dataModel = _initializer
            _initializer = null
            doApplyPostProcessors() // 应用 postProcessors
            modificationTracker.incModificationCount() // 显式增加修改计数
            initialized = true // 标记规则数据已全部加载完毕
            val end = System.currentTimeMillis()
            val targetName = if (project.isDefault) "application" else "project '${project.name}'"
            logger.info("Initialized config group '${gameType.id}' for $targetName in ${end - start} ms.")
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            if (e is CancellationException) throw e
            logger.error(e) // 不期望在这里出现常规异常
        }
    }

    private suspend fun doApplyProcessors() {
        val dataProviders = CwtConfigGroupProcessor.EP_NAME.extensionList
        dataProviders.forEachFast { it.process(this) }
    }

    private suspend fun doApplyPostProcessors() {
        val postProcessors = CwtConfigGroupPostProcessor.EP_NAME.extensionList
        postProcessors.forEachFast { it.postProcess(this) }
    }

    override fun clear() {
        _dataModel = null
        _initializer = null
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is CwtConfigGroup && gameType == other.gameType && project == other.project)
    }

    override fun hashCode(): Int {
        return Objects.hash(gameType, project)
    }

    override fun toString(): String {
        return "CwtConfigGroupBase(gameType=${gameType.id}, project=$project, initialized=$initialized, changed=$changed)"
    }

    // region Accessors

    override val fileConfigs get() = dataModel.fileConfigs
    override val configPostProcessActions get() = dataModel.configPostProcessActions
    override val schemas get() = dataModel.schemas
    override val foldingSettings get() = dataModel.foldingSettings
    override val postfixTemplateSettings get() = dataModel.postfixTemplateSettings
    override val priorities get() = dataModel.priorities
    override val systemScopes get() = dataModel.systemScopes
    override val locales get() = dataModel.locales
    override val types get() = dataModel.types
    override val swappedTypes get() = dataModel.swappedTypes
    override val type2ModifiersMap get() = dataModel.type2ModifiersMap
    override val declarations get() = dataModel.declarations
    override val rows get() = dataModel.rows
    override val defineNamespaces get() = dataModel.defineNamespaces
    override val enums get() = dataModel.enums
    override val complexEnums get() = dataModel.complexEnums
    override val complexEnumsFromColumns get() = dataModel.complexEnumsFromColumns
    override val unions get() = dataModel.unions
    override val dynamicValueTypes get() = dataModel.dynamicValueTypes
    override val links get() = dataModel.links
    override val localisationLinks get() = dataModel.localisationLinks
    override val localisationCommands get() = dataModel.localisationCommands
    override val localisationPromotions get() = dataModel.localisationPromotions
    override val scopes get() = dataModel.scopes
    override val scopeAliasMap get() = dataModel.scopeAliasMap
    override val scopeGroups get() = dataModel.scopeGroups
    override val modifierCategories get() = dataModel.modifierCategories
    override val modifiers get() = dataModel.modifiers
    override val databaseObjectTypes get() = dataModel.databaseObjectTypes
    override val aliasGroups get() = dataModel.aliasGroups
    override val singleAliases get() = dataModel.singleAliases
    override val macros get() = dataModel.macros
    override val extendedScriptedVariables get() = dataModel.extendedScriptedVariables
    override val extendedDefinitions get() = dataModel.extendedDefinitions
    override val extendedGameRules get() = dataModel.extendedGameRules
    override val extendedOnActions get() = dataModel.extendedOnActions
    override val extendedParameters get() = dataModel.extendedParameters
    override val extendedComplexEnumValues get() = dataModel.extendedComplexEnumValues
    override val extendedDynamicValues get() = dataModel.extendedDynamicValues
    override val extendedInlineScripts get() = dataModel.extendedInlineScripts
    override val globalLocales get() = dataModel.globalLocales
    override val supportedLocales get() = dataModel.supportedLocales
    override val predefinedModifiers get() = dataModel.predefinedModifiers
    override val generatedModifiers get() = dataModel.generatedModifiers
    override val aliasKeysGroupConst get() = dataModel.aliasKeysGroupConst
    override val aliasKeysGroupNoConst get() = dataModel.aliasKeysGroupNoConst
    override val aliasNamesSupportScope get() = dataModel.aliasNamesSupportScope
    override val relatedLocalisationPatterns get() = dataModel.relatedLocalisationPatterns
    override val typesModel get() = dataModel.typesModel
    override val linksModel get() = dataModel.linksModel
    override val localisationLinksModel get() = dataModel.localisationLinksModel
    override val macrosModel get() = dataModel.macrosModel
    override val attribute get() = dataModel.attribute
    override fun getUnionAttribute(name: String) = dataModel.getUnionAttribute(name)
    override fun getAliasAttribute(name: String) = dataModel.getAliasAttribute(name)
    override fun getSingleAliasAttribute(name: String) = dataModel.getSingleAliasAttribute(name)

    // endregion

    companion object {
        private val logger = logger<CwtConfigGroup>()
    }
}
