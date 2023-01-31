package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.psi.*

data class CwtModifierConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val config: CwtPropertyConfig,
	val name: String, //template name, not actual modifier name!
	val categories: Set<String> = emptySet() //category names
): CwtConfig<CwtProperty> {
	//CWT规则文件中关于（生成的）修饰符的规则有多种写法
	//* 写在 type[xxx] = {...} 子句中的 modifiers = { ... } 子句中，允许按子类型进行匹配，格式为 <template_string> = <categories>
	//* 作为 alias[modifier:xxx] = x 中的 xxx ，这里PLS认为 xxx 需要表示一个常量字符串或者模版表达式（如 mod_<job>_desc）
	//* 写在 modifier.cwt 中的 modifiers = { ... } 子句中，格式为 <template_string> == <categories>
	//目前PLS支持以上所有三种语法
	
	val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()
	
	val template = CwtTemplateExpression.resolve(name)
	val supportedScopes: Set<String> by lazy {
		if(categoryConfigMap.isNotEmpty()) {
			val categoryConfigs = categoryConfigMap.values
			if(categoryConfigs.any { it.supportAnyScope }) {
				ParadoxScopeHandler.anyScopeIdSet
			} else {
				categoryConfigs.flatMapTo(mutableSetOf()) { it.supportedScopes }
			}
		} else {
			//没有注明categories时从scopes选项中获取
			config.supportedScopes
		}
	}
}