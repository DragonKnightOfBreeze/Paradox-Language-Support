package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.CodeInjectorSupport
import icu.windea.pls.inject.annotations.InlinedDelegateField
import javassist.CtClass
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import kotlin.reflect.full.findAnnotations

/**
 * 为基于 [InlinedDelegateField] 的代码注入器提供支持。
 *
 * 这类代码注入器可以将 Kotlin 属性委托生成的实例字段（例如：`name$delegate`）的读取替换为静态委托表达式，
 * 并移除该实例字段，从而减少实例内存占用。
 */
class InlinedDelegateFieldCodeInjectorSupport : CodeInjectorSupport {
    private val logger = thisLogger()

    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorScope.targetClassKey) ?: return
        val infos = codeInjector::class.findAnnotations<InlinedDelegateField>()
        if (infos.isEmpty()) return

        for (info in infos) {
            applyForOne(targetClass, info)
        }
    }

    private fun applyForOne(targetClass: CtClass, info: InlinedDelegateField) {
        val propertyName = info.value
        val delegateFieldName = "${propertyName}\$delegate"
        val delegateExpression = info.delegateExpression

        val field = runCatching { targetClass.getDeclaredField(delegateFieldName) }.getOrNull()
        if (field == null) {
            logger.warn("Delegate field $delegateFieldName not found in ${targetClass.name}")
            return
        }
        if (javassist.Modifier.isStatic(field.modifiers)) {
            logger.warn("Skip inlining delegate field $delegateFieldName in ${targetClass.name}: static field")
            return
        }

        val editor = object : ExprEditor() {
            override fun edit(f: FieldAccess) {
                if (f.className == targetClass.name && f.fieldName == delegateFieldName && !f.isStatic) {
                    when {
                        f.isReader -> {
                            // 用静态委托表达式替换字段读取。
                            // 使用 ($r) 进行返回类型兼容转换。
                            f.replace("{ \$_ = (\$r) ($delegateExpression); }")
                        }
                        f.isWriter -> {
                            // 删除对该字段的写入（通常发生在构造函数中）。
                            // 这里忽略写入即可。
                            f.replace("{ }")
                        }
                    }
                }
            }
        }

        // 先替换所有字段访问，再移除字段本体。
        for (constructor in targetClass.declaredConstructors) {
            runCatching {
                constructor.instrument(editor)
            }.onFailure { e ->
                logger.warn("Failed to instrument constructor ${constructor.name} in ${targetClass.name} for field $delegateFieldName: ${e.message}", e)
            }
        }
        for (method in targetClass.declaredMethods) {
            runCatching {
                method.instrument(editor)
            }.onFailure { e ->
                logger.warn("Failed to instrument method ${method.name} in ${targetClass.name} for field $delegateFieldName: ${e.message}", e)
            }
        }

        runCatching {
            targetClass.removeField(field)
        }.onFailure { e ->
            logger.warn("Failed to remove field $delegateFieldName in ${targetClass.name}: ${e.message}", e)
        }
    }
}
