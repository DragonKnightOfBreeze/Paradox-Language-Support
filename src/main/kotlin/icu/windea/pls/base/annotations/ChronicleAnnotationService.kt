package icu.windea.pls.base.annotations

import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.optimized
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression

object ChronicleAnnotationService {
    // TODO 3.0.x refactor: remove all annotation based check (since it's not effective and necessary)

    private val gameTypesCache = CacheBuilder().build<Class<*>, Set<ParadoxGameType>> {
        it.getAnnotation(ForGameType::class.java)?.value?.toSet()?.optimized().orEmpty()
    }
    private val definitionTypesCache = CacheBuilder().build<Class<*>, Set<String>> {
        it.getAnnotation(ForDefinitionType::class.java)?.value?.toSet()?.optimized().orEmpty()
    }

    /**
     * 基于注解 [ForGameType]，判断目标类型（[targetType]）是否支持指定的游戏类型（[gameType]）。
     */
    fun check(targetType: Class<*>, gameType: ParadoxGameType?): Boolean {
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return doCheck(targetType, gameType)
    }

    /**
     * 基于注解 [ForGameType]，判断目标对象（[target]）是否支持指定的游戏类型（[gameType]）。
     */
    fun check(target: Any, gameType: ParadoxGameType?): Boolean {
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return doCheck(target.javaClass, gameType)
    }

    private fun doCheck(targetType: Class<*>, gameType: ParadoxGameType): Boolean {
        val gameTypes = gameTypesCache.get(targetType)
        if (gameTypes.isEmpty()) return true
        return gameType in gameTypes
    }

    /**
     * 基于注解 [ForDefinitionType]，判断目标对象类型（[targetType]）是否支持指定的定义信息（[definitionInfo]）。
     */
    fun check(targetType: Class<*>, definitionInfo: ParadoxDefinitionInfo?): Boolean {
        if (definitionInfo == null) return false
        return doCheck(targetType, definitionInfo)
    }

    /**
     * 基于注解 [ForDefinitionType]，判断目标对象（[target]）是否支持指定的定义信息（[definitionInfo]）。
     */
    fun check(target: Any, definitionInfo: ParadoxDefinitionInfo?): Boolean {
        if (definitionInfo == null) return false
        return doCheck(target.javaClass, definitionInfo)
    }

    private fun doCheck(targetType: Class<*>, definitionInfo: ParadoxDefinitionInfo): Boolean {
        val types = definitionTypesCache.get(targetType)
        if (types.isEmpty()) return true
        return types.any { ParadoxDefinitionTypeExpression.resolve(it).matches(definitionInfo) }
    }
}
