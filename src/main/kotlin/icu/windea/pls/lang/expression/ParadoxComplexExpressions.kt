@file:Suppress("KDocUnresolvedReference")

package icu.windea.pls.lang.expression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.expression.impl.ParadoxCommandExpressionResolverImpl
import icu.windea.pls.lang.expression.impl.ParadoxDataObjectExpressionResolverImpl
import icu.windea.pls.lang.expression.impl.ParadoxDefineReferenceExpressionResolverImpl
import icu.windea.pls.lang.expression.impl.ParadoxDynamicValueExpressionResolverImpl
import icu.windea.pls.lang.expression.impl.ParadoxScopeFieldExpressionResolverImpl
import icu.windea.pls.lang.expression.impl.ParadoxScriptValueExpressionResolverImpl
import icu.windea.pls.lang.expression.impl.ParadoxTemplateExpressionResolverImpl
import icu.windea.pls.lang.expression.impl.ParadoxValueFieldExpressionResolverImpl
import icu.windea.pls.lang.expression.impl.ParadoxVariableFieldExpressionResolverImpl
import icu.windea.pls.lang.expression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.expression.nodes.ParadoxDatabaseObjectNode
import icu.windea.pls.lang.expression.nodes.ParadoxDatabaseObjectTypeNode
import icu.windea.pls.lang.expression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.expression.nodes.ParadoxDefineVariableNode
import icu.windea.pls.lang.expression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.expression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.expression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.expression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.expression.nodes.ParadoxValueFieldNode

/**
 * 模版表达式。对应的规则类型为 [CwtDataTypes.TemplateExpression]。
 *
 * @see CwtDataTypes.TemplateExpression
 */
interface ParadoxTemplateExpression : ParadoxComplexExpression {
    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxTemplateExpression?
    }

    companion object : Resolver by ParadoxTemplateExpressionResolverImpl()
}

/**
 * 动态值表达式。对应的规则类型为 [CwtDataTypeGroups.DynamicValue]。
 *
 * 语法：
 * ```bnf
 * dynamic_value_expression ::= dynamic_value ("@" scope_field_expression)?
 * dynamic_value ::= TOKEN // matching config expression "value[xxx]" or "value_set[xxx]"
 * // "event_target:t1.v1@event_target:t2.v2@..." is not used in vanilla files but allowed here
 * ```
 *
 * 示例：
 * - `some_variable`
 * - `some_variable@root`
 *
 * @see CwtDataTypeGroups.DynamicValue
 */
interface ParadoxDynamicValueExpression : ParadoxComplexExpression {
    val configs: List<CwtConfig<*>>

    val dynamicValueNode: ParadoxDynamicValueNode
    val scopeFieldExpression: ParadoxScopeFieldExpression?

    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression?
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression?
    }

    companion object : Resolver by ParadoxDynamicValueExpressionResolverImpl()
}

/**
 * 作用域字段表达式。对应的规则类型为 [CwtDataTypeGroups.ScopeField]。
 *
 * 语法：
 * ```bnf
 * scope_field_expression ::= scope +
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN // predefined by CWT Config (see system_scopes.cwt)
 * scope_link ::= TOKEN // predefined by CWT Config (see links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_value // predefined by CWT Config (see links.cwt)
 * scope_link_prefix ::= TOKEN // e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_value ::= EXPRESSION // e.g. "some_variable" while the link's data source is "value[variable]"
 * expression ::= data_expression | dynamic_value_expression // see: ParadoxDataExpression, ParadoxDynamicValueExpression
 * ```
 *
 * 示例：
 * - `root`
 * - `root.owner`
 * - `event_target:some_target`
 *
 * @see CwtDataTypeGroups.ScopeField
 */
interface ParadoxScopeFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>

    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression?
    }

    companion object : Resolver by ParadoxScopeFieldExpressionResolverImpl()
}

/**
 * 值字段表达式。对应的规则类型为 [CwtDataTypeGroups.ValueField]。
 *
 * 语法：
 *
 * ```bnf
 * value_field_expression ::= scope * value_field
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN // predefined by CWT Config (see system_scopes.cwt)
 * scope_link ::= TOKEN // predefined by CWT Config (see links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_value // predefined by CWT Config (see links.cwt)
 * scope_link_prefix ::= TOKEN // e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_value ::= expression // e.g. "some_variable" while the link's data source is "value[variable]"
 * value_field ::= value_link | value_link_from_data
 * value_link ::= TOKEN // predefined by CWT Config (see links.cwt)
 * value_link_from_data ::= value_field_prefix value_field_value // predefined by CWT Config (see links.cwt)
 * value_field_prefix ::= TOKEN // e.g. "value:" while the link's prefix is "value:"
 * value_field_value ::= expression // e.g. "some" while the link's data source is "value[variable]"
 * expression ::= data_expression | dynamic_value_expression | sv_expression // see: ParadoxDataExpression, ParadoxDynamicValueExpression
 * sv_expression ::= sv_name ("|" (param_name "|" param_value "|")+)? // e.g. value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * ```
 *
 * 示例：
 * - `trigger:some_trigger`
 * - `value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|`
 * - `root.owner.some_variable`
 *
 * @see CwtDataTypeGroups.ValueField
 */
interface ParadoxValueFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>
    val valueFieldNode: ParadoxValueFieldNode
    val scriptValueExpression: ParadoxScriptValueExpression?

    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpression?
    }

    companion object : Resolver by ParadoxValueFieldExpressionResolverImpl()
}

/**
 * 变量字段表达式。对应的规则类型为 [CwtDataTypeGroups.ValueField]。
 *
 * 作为 [ParadoxValueFieldExpression] 的子集。相较之下，仅支持调用变量。
 *
 * 示例：
 * - `root.owner.some_variable`
 *
 * @see CwtDataTypeGroups.ValueField
 * @see ParadoxValueFieldExpression
 */
interface ParadoxVariableFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>
    val variableNode: ParadoxDataSourceNode

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression?
    }

    companion object : Resolver by ParadoxVariableFieldExpressionResolverImpl()
}

/**
 * 脚本值表达式。
 *
 * 作为 [ParadoxValueFieldExpression] 的一部分。
 *
 * 语法：
 * ```bnf
 * script_value_expression ::= script_value ("|" (arg_name "|" arg_value "|")+)?
 * script_value ::= TOKEN // matching config expression "<script_value>"
 * arg_name ::= TOKEN // argument name, no surrounding "$"
 * arg_value ::= TOKEN // boolean, int, float or string
 * ```
 *
 * 示例：
 * - `some_sv`
 * - `some_sv|PARAM|VALUE|`
 *
 * @see ParadoxValueFieldExpression
 */
interface ParadoxScriptValueExpression : ParadoxComplexExpression {
    val config: CwtConfig<*>

    val scriptValueNode: ParadoxScriptValueNode
    val argumentNodes: List<Pair<ParadoxScriptValueArgumentNode, ParadoxScriptValueArgumentValueNode?>>

    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueExpression?
    }

    companion object : Resolver by ParadoxScriptValueExpressionResolverImpl()
}

/**
 * 数据库对象表达式。对应的规则类型为 [CwtDataTypes.DatabaseObject]。
 *
 * 可以在本地化文件中作为概念名称使用（如 `['civic:some_civic', ...]`）。
 *
 * 语法：
 * ```bnf
 * database_object_expression ::= database_object_type ":" database_object
 * database_object_type ::= TOKEN // predefined by CWT Config (see database_object_types.cwt)
 * database_object ::= TOKEN // predefined by CWT Config (see database_object_types.cwt)
 * ```
 *
 * 示例：
 * -`civic:some_civic`
 * -`civic:some_civic:some_swapped_civic`
 * -`job:job_soldier`
 *
 * @see CwtDataTypes.DatabaseObject
 * @see icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
 */
interface ParadoxDatabaseObjectExpression : ParadoxComplexExpression {
    val typeNode: ParadoxDatabaseObjectTypeNode?
    val valueNode: ParadoxDatabaseObjectNode?

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression?
    }

    companion object : Resolver by ParadoxDataObjectExpressionResolverImpl()
}

/**
 * 预设值引用表达式。对应的规则类型为 [CwtDataTypes.DefineReference]。
 *
 * 语法：
 * ```bnf
 * define_reference_expression ::= "define:" define_namespace "|" define_variable
 * define_namespace ::= TOKEN // level 1 property keys in .txt files in common/defines
 * define_variable ::= TOKEN // level 2 property keys in .txt files in common/defines
 * ```
 *
 * 示例：
 * - `define:NPortrait|GRACEFUL_AGING_START`
 *
 * @see CwtDataTypes.DefineReference
 */
interface ParadoxDefineReferenceExpression : ParadoxComplexExpression {
    val namespaceNode: ParadoxDefineNamespaceNode?
    val variableNode: ParadoxDefineVariableNode?

    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression?
    }

    companion object : Resolver by ParadoxDefineReferenceExpressionResolverImpl()
}

/**
 * （本地化）命令表达式。
 *
 * 可以在本地化文件中作为命令文本使用。（如，`[Root.GetName]`）
 *
 * 语法：
 *
 * ```bnf
 * command_expression ::= command_scope_link * (command_field) suffix ?
 * command_scope_link := system_command_scope | command_scope | dynamic_command_scope_link
 * system_command_scope := TOKEN // predefined by CWT Config (see system scopes)
 * command_scope := TOKEN // predefined by CWT Config (see localisation links)
 * dynamic_command_scope_link := dynamic_command_scope_link_prefix ? dynamic_command_scope_link_value
 * dynamic_command_scope_link_prefix := TOKEN // "event_target:", "parameter:", etc.
 * dynamic_command_scope_link_value := TOKEN // matching config expression "value[event_target]" or "value[global_event_target]"
 * command_field ::= predefined_command_field | dynamic_command_field
 * predefined_command_field := TOKEN // predefined by CWT Config (see localisation commands)
 * dynamic_command_field ::= TOKEN // matching config expression "<scripted_loc>" or "value[variable]"
 * suffix ::= TOKEN // see 99_README_GRAMMAR.txt
 * ```
 *
 * 示例：
 * - `Root.GetName`
 * - `Root.Owner.event_target:some_event_target.var`
 *
 * @see icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText
 */
interface ParadoxCommandExpression : ParadoxComplexExpression {
    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxCommandExpression?
    }

    companion object : Resolver by ParadoxCommandExpressionResolverImpl()
}
