package icu.windea.pls.script.expression

import com.google.common.cache.*
import com.intellij.openapi.application.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*

abstract class ParadoxScriptComplexExpressionResolver<T : ParadoxScriptComplexExpression> {
	//这里需要限制缓存数量 - 因为表达式可能格式错误或者有引用无法解析
	val cache: Cache<String, T> by lazy { CacheBuilder.newBuilder().maximumSize(1000L).buildCache() }
	
	fun resolve(expressionString: String, configGroup: CwtConfigGroup): T {
		return cache.getOrPut(configGroup.gameType.id + " " + expressionString) {
			runReadAction {
				doResolve(expressionString, configGroup)
			}
		}
	}
	
	protected abstract fun doResolve(expressionString: String, configGroup: CwtConfigGroup): T
}