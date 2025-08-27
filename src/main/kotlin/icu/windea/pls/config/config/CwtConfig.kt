package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * CWT规则。
 */
interface CwtConfig<out T : PsiElement> : UserDataHolder {
    val pointer: SmartPsiElementPointer<out T>
    val configGroup: CwtConfigGroup

    val configExpression: CwtDataExpression? get() = null

    /**
     * 用于标记此属性对应CWT规则文件中的特定规则的特定属性。
     * @property value 属性的声明，格式为`{name}: {type}`。
     * @property defaultValue 默认值。如果为空则表示未声明。
     * @property allowedValues 允许的值。如果为空则表示未声明。
     * @property multiple 是否可以重复。
     */
    @MustBeDocumented
    @Target(AnnotationTarget.PROPERTY)
    annotation class Property(
        val value: String,
        val defaultValue: String = "",
        val allowedValues: Array<String> = [],
        val multiple: Boolean = false
    )

    /**
     * 用于标记此属性对应CWT规则文件中的特定规则的特定选项。
     * @property value 选项的声明，格式为`{name}: {type}`。如果是单独的值，则格式为`{name}`。
     * @property defaultValue 默认值。如果为空则表示未声明。
     * @property allowedValues 允许的值。如果为空则表示未声明。
     * @property multiple 是否可以重复。
     */
    @MustBeDocumented
    @Target(AnnotationTarget.PROPERTY)
    annotation class Option(
        val value: String,
        val defaultValue: String = "",
        val allowedValues: Array<String> = [],
        val multiple: Boolean = false
    )
}
