package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.application
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorService
import icu.windea.pls.inject.CodeInjectorSupport
import icu.windea.pls.inject.annotations.InjectOptimizedField
import javassist.CtField
import javassist.Modifier
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import javassist.expr.MethodCall
import kotlin.reflect.full.findAnnotations

/**
 * 为基于 [InjectOptimizedField] 的代码注入器提供支持。
 *
 * 这里代码注入器可以修改指定成员字段的类型和初始化逻辑，从而优化性能或内存占用。
 */
class OptimizedFieldCodeInjectorSupport : CodeInjectorSupport {
    private val logger = thisLogger()

    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorService.targetClassKey) ?: return
        val infos = codeInjector::class.findAnnotations<InjectOptimizedField>()
        if (infos.isEmpty()) return

        val classPool = application.getUserData(CodeInjectorService.classPoolKey) ?: return

        for (info in infos) {
            val fieldName = info.value
            val oldField = runCatching { targetClass.getDeclaredField(fieldName) }.getOrNull()
            if (oldField == null) {
                logger.warn("Field $fieldName not found in ${targetClass.name}")
                continue
            }
            val modifiers = oldField.modifiers
            val static = Modifier.isStatic(modifiers)
            if (static) {
                logger.warn("Skip optimizing field $fieldName in ${targetClass.name}: static field")
                continue
            }
            val oldFieldType = oldField.type
            val oldTypeName = oldFieldType.name
            val typeName = info.type.java.name ?: continue
            val initTypeName = info.initType.java.name ?: continue
            val sameDeclaredType = typeName == oldTypeName
            if (sameDeclaredType && typeName == initTypeName) {
                logger.warn("Field $fieldName in ${targetClass.name} is already declared with type $typeName")
                continue
            }
            val type = runCatching { classPool.get(typeName) }.getOrNull()
            if (type == null) {
                logger.warn("New type $typeName is not resolvable in class pool")
                continue
            }
            val initType = if (typeName == initTypeName) type else runCatching { classPool.get(initTypeName) }.getOrNull()
            if (initType == null) {
                logger.warn("New init type $initTypeName is not resolvable in class pool")
                continue
            }
            val assignable = runCatching { type.subclassOf(oldFieldType) }.getOrElse { false }
            val private = Modifier.isPrivate(oldField.modifiers)
            if (!private && !assignable) {
                logger.warn("Skip optimizing field $fieldName in ${targetClass.name}: $typeName is not assignable to $oldTypeName and field is not private")
                continue
            }


            // 修改字段类型和初始化逻辑
            val fieldCode = "$typeName $fieldName = new $initTypeName();"
            val field = CtField.make(fieldCode, targetClass)
            field.modifiers = modifiers
            targetClass.removeField(oldField)
            targetClass.addField(field)

            if (!private) continue

            // 修改构造函数中的初始化
            for (constructor in targetClass.declaredConstructors) {
                runCatching {
                    constructor.instrument(object : ExprEditor() {
                        override fun edit(f: FieldAccess) {
                            if (f.className == targetClass.name && f.fieldName == fieldName) {
                                if (f.isReader) {
                                    // 用新的字段签名重新生成读取字节码
                                    f.replace("{ \$_ = this.$fieldName; }")
                                } else if (f.isWriter) {
                                    // 不使用原始右值，直接重建为新实现，避免类型不匹配
                                    f.replace("{ this.$fieldName = new $initTypeName(); }")
                                }
                            }
                        }
                    })
                }.onFailure { e ->
                    logger.warn("Failed to instrument constructor ${constructor.name} in ${targetClass.name} for field $fieldName: ${e.message}", e)
                }
            }

            // 修改方法中的成员调用（包括对其方法的调用）
            for (method in targetClass.declaredMethods) {
                runCatching {
                    method.instrument(object : ExprEditor() {
                        override fun edit(f: FieldAccess) {
                            if (f.className == targetClass.name && f.fieldName == fieldName) {
                                if (f.isReader) {
                                    // 用新的字段签名重新生成读取字节码
                                    f.replace("{ \$_ = this.$fieldName; }")
                                } else if (f.isWriter) {
                                    // 不使用原始右值，直接重建为新实现，避免类型不匹配
                                    f.replace("{ this.$fieldName = new $initTypeName(); }")
                                }
                            }
                        }

                        override fun edit(m: MethodCall) {
                            if (sameDeclaredType) return
                            if (m.className == oldTypeName) {
                                m.replace("{ \$_ = (($typeName) this.$fieldName).${m.methodName}($$); }")
                            }
                        }
                    })
                }.onFailure { e ->
                    logger.warn("Failed to instrument method ${method.name} in ${targetClass.name} for field $fieldName: ${e.message}", e)
                }
            }
        }

        targetClass.writeFile("D:\\Documents\\Projects\\__Pinned\\Paradox-Language-Support\\tmp")
    }
}
