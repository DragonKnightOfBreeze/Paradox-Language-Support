package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxDataObjectExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectTypeNode
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName

/**
 * 数据库对象表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.DatabaseObject]。
 * - 可以在本地化文件中作为概念名称（[ParadoxLocalisationConceptName]）使用。
 *
 * 示例：
 * ```
 * civic:some_civic
 * civic:some_civic:some_swapped_civic
 * job:job_soldier
 * ```
 *
 * 语法：
 * ```bnf
 * database_object_expression ::= database_object_type ":" database_object
 * database_object_type ::= TOKEN // predefined by CWT Config (see database_object_types.cwt)
 * database_object ::= TOKEN // predefined by CWT Config (see database_object_types.cwt)
 * ```
 */
interface ParadoxDatabaseObjectExpression : ParadoxComplexExpression {
    val typeNode: ParadoxDatabaseObjectTypeNode?
    val valueNode: ParadoxDatabaseObjectNode?

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression?
    }

    companion object : Resolver by ParadoxDataObjectExpressionResolverImpl()
}
