package icu.windea.pls.lang.annotations

import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType

object PlsAnnotationManager {
    private val definitionTypesCache = CacheBuilder().build<Class<*>, Set<String>> {
        it.getAnnotation(WithDefinitionType::class.java)?.value?.toSet()?.optimized().orEmpty()
    }
    private val gameTypesCache = CacheBuilder().build<Class<*>, Set<ParadoxGameType>> {
        it.getAnnotation(WithGameType::class.java)?.value?.toSet()?.optimized().orEmpty()
    }

    /**
     * 基于注解 [WithDefinitionType]，判断目标对象类型（[targetType]）是否支持指定的定义信息（[definitionInfo]）。
     */
    fun check(targetType: Class<*>, definitionInfo: ParadoxDefinitionInfo?): Boolean {
        if (definitionInfo == null) return false
        return doCheck(targetType, definitionInfo)
    }

    /**
     * 基于注解 [WithDefinitionType]，判断目标对象（[target]）是否支持指定的定义信息（[definitionInfo]）。
     */
    fun check(target: Any, definitionInfo: ParadoxDefinitionInfo?): Boolean {
        if (definitionInfo == null) return false
        return doCheck(target.javaClass, definitionInfo)
    }

    private fun doCheck(targetType: Class<*>, definitionInfo: ParadoxDefinitionInfo): Boolean {
        val types = definitionTypesCache.get(targetType)
        return types.any { ParadoxDefinitionTypeExpression.resolve(it).matches(definitionInfo) }
    }

    /**
     * 基于注解 [WithGameType]，判断目标类型（[targetType]）是否支持指定的游戏类型（[gameType]）。
     */
    fun check(targetType: Class<*>, gameType: ParadoxGameType?): Boolean {
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return doCheck(targetType, gameType)
    }

    /**
     * 基于注解 [WithGameType]，判断目标对象（[target]）是否支持指定的游戏类型（[gameType]）。
     */
    fun check(target: Any, gameType: ParadoxGameType?): Boolean {
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return doCheck(target.javaClass, gameType)
    }

    private fun doCheck(targetType: Class<*>, gameType: ParadoxGameType): Boolean {
        val gameTypes = gameTypesCache.get(targetType)
        return gameType in gameTypes
    }
}
