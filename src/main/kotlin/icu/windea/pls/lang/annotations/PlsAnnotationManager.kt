package icu.windea.pls.lang.annotations

import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType

object PlsAnnotationManager {
    /**
     * 基于注解 [WithDefinitionType]，判断目标对象类型（[targetType]）是否支持指定的定义信息（[definitionInfo]）。
     */
    fun check(targetType: Class<*>, definitionInfo: ParadoxDefinitionInfo): Boolean {
        val annotation = targetType.getAnnotation(WithDefinitionType::class.java) ?: return true
        val types = annotation.value
        return types.any { ParadoxDefinitionTypeExpression.resolve(it).matches(definitionInfo) }
    }

    /**
     * 基于注解 [WithDefinitionType]，判断目标对象（[target]）是否支持指定的定义信息（[definitionInfo]）。
     */
    fun check(target: Any, definitionInfo: ParadoxDefinitionInfo?): Boolean {
        if (definitionInfo == null) return false
        return check(target.javaClass, definitionInfo)
    }

    /**
     * 基于注解 [WithGameType]，判断目标类型（[targetType]）是否支持指定的游戏类型（[gameType]）。
     */
    fun check(targetType: Class<*>, gameType: ParadoxGameType?): Boolean {
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        val annotation = targetType.getAnnotation(WithGameType::class.java) ?: return true
        val gameTypes = annotation.value
        return gameType in gameTypes
    }

    /**
     * 基于注解 [WithGameType]，判断目标对象（[target]）是否支持指定的游戏类型（[gameType]）。
     */
    fun check(target: Any, gameType: ParadoxGameType?): Boolean {
        return check(target.javaClass, gameType)
    }
}
