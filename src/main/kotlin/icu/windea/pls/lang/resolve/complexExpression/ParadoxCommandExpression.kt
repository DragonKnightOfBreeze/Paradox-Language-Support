package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxCommandExpressionResolverImpl
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText

/**
 * （本地化）命令表达式。
 *
 * 说明：
 * - 可以在本地化文件中作为命令文本（[ParadoxLocalisationCommandText]）使用。
 *
 * 示例：
 * ```
 * Root.GetName
 * Root.Owner.event_target:some_event_target.var
 * ```
 *
 * 语法：
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
 */
interface ParadoxCommandExpression : ParadoxComplexExpression {
    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxCommandExpression?
    }

    companion object : Resolver by ParadoxCommandExpressionResolverImpl()
}
