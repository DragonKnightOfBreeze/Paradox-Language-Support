package icu.windea.pls.model.expression.complex

/**
 * 本地化命令表达式。
 * 
 * 可以在本地化文件中作为命令文本使用。（如，`[Root.GetName]`）
 * 
 * 语法：
 * 
 * ```bnf
 * localisation_command_expression ::= command_scope * (command_field | variable)
 * command_scope := ["event_target:"] TOKEN //predefined by CWT Config (see localisation scopes)
 * command_field := TOKEN //predefined by CWT Config (see localisation commands)
 * variable ::= TOKEN
 * ```
 * 
 * 示例：
 * 
 * * `Root.GetName`
 * * `Root.Owner.var`
 */
class ParadoxLocalisationCommandExpression {
}
