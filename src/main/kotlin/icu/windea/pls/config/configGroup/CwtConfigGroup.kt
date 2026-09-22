package icu.windea.pls.config.configGroup

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.model.CwtConfigGroupDataModel
import icu.windea.pls.config.model.CwtConfigGroupDataModelBase
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupPostProcessor
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupProcessor
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

/**
 * 规则分组。保存了处理后的所有规则数据。
 *
 * 规则分组会在获取时就保证已经被创建，而其中的规则数据的初始化是在打开 IDE 或项目时异步进行的。
 *
 * 参考：
 * - 规则系统的说明文档：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 * - 规则格式的参考手册：[ref-config-format.md](https://windea.icu/Paradox-Language-Support/ref-config-format.md)
 *
 * @property project 对应的项目。如果是默认项目，则不能用于访问 PSI。
 * @property gameType 对应的游戏类型。如果是 [ParadoxGameType.Core]，则为通用的规则分组。
 * @property dataModel 底层的数据模型。如果规则分组已被清理，则会得到空模型。
 * @property initializer 底层的用于初始化的可变数据模型。如果规则分组已被清理，则会得到新创建的临时模型。
 *
 * @see CwtConfigGroupDataModel
 * @see CwtConfigGroupService
 * @see CwtConfigGroupProcessor
 * @see CwtConfigGroupPostProcessor
 * @see CwtConfigGroupFileProvider
 */
interface CwtConfigGroup : CwtConfigGroupDataModel, UserDataHolder {
    val project: Project
    val gameType: ParadoxGameType
    var initialized: Boolean
    var changed: Boolean
    val modificationTracker: SimpleModificationTracker
    val dataModel: CwtConfigGroupDataModel
    val initializer: CwtConfigGroupDataModelBase

    suspend fun init()

    fun clear()

    object Keys : KeyRegistry()

    companion object {
        @JvmStatic
        fun create(project: Project, gameType: ParadoxGameType): CwtConfigGroup {
            return CwtConfigGroupImpl(project, gameType)
        }
    }
}

// region Implementations

private class CwtConfigGroupImpl(
    override val project: Project,
    override val gameType: ParadoxGameType,
) : UserDataHolderBase(), CwtConfigGroup, CwtConfigGroupDataModel {
    private val mutex = Mutex()
    @Volatile private var _dataModel: CwtConfigGroupDataModel? = CwtConfigGroupDataModel.create()
    @Volatile private var _initializer: CwtConfigGroupDataModelBase? = CwtConfigGroupDataModel.create()

    @Volatile override var initialized = false
    @Volatile override var changed = false
    override val modificationTracker = SimpleModificationTracker()
    override val dataModel get() = _dataModel ?: CwtConfigGroupDataModel.createEmpty()
    override val initializer get() = _initializer ?: CwtConfigGroupDataModel.create()

    override suspend fun init() {
        // 即使规则数据已全部加载完毕，也可能需要再次重新加载
        mutex.withLock { doInit() }
    }

    private suspend fun doInit() {
        try {
            val start = System.currentTimeMillis()
            _initializer = CwtConfigGroupDataModel.create() // refresh initializer
            doApplyProcessors() // apply processors
            _dataModel = _initializer /// bind data model
            clearUserData() // also necessary
            doApplyPostProcessors() // apply post processors
            modificationTracker.incModificationCount() // 显式增加修改计数
            initialized = true // 标记规则数据已全部加载完毕
            val end = System.currentTimeMillis()
            val targetName = if (project.isDefault) "application" else "project '${project.name}'"
            logger.info("Initialized config group '${gameType.id}' for $targetName in ${end - start} ms.")
        } catch (e: Exception) {
            checkCancellation(e)
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
        clearUserData() // also necessary
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is CwtConfigGroup && gameType == other.gameType && project == other.project)
    }

    override fun hashCode(): Int {
        return Objects.hash(gameType, project)
    }

    override fun toString(): String {
        return "CwtConfigGroupBase(gameType=${gameType.id}, project=$project)"
    }

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
    override val modifiers get() = dataModel.modifiers
    override val modifierCategories get() = dataModel.modifierCategories
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
    override val relatedLocalisationPatterns get() = dataModel.relatedLocalisationPatterns
    override val typeModel get() = dataModel.typeModel
    override val scopeModel get() = dataModel.scopeModel
    override val linkModel get() = dataModel.linkModel
    override val localisationLinkModel get() = dataModel.localisationLinkModel
    override val aliasModel get() = dataModel.aliasModel
    override val unionModel get() = dataModel.unionModel
    override val macroModel get() = dataModel.macroModel
    override val attribute get() = dataModel.attribute
    override fun getUnionAttribute(name: String) = dataModel.getUnionAttribute(name)
    override fun getAliasAttribute(name: String) = dataModel.getAliasAttribute(name)
    override fun getSingleAliasAttribute(name: String) = dataModel.getSingleAliasAttribute(name)

    companion object {
        private val logger = logger<CwtConfigGroup>()
    }
}

// endregion
