package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.core.orNull
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.CodeInjectorSupport
import icu.windea.pls.inject.annotations.InlinedDelegateField
import icu.windea.pls.inject.annotations.InlinedDelegateFields
import javassist.CtClass
import javassist.bytecode.CodeIterator
import javassist.bytecode.Descriptor
import javassist.bytecode.Opcode
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations

/**
 * 为基于 [InlinedDelegateField] 的代码注入器提供支持。
 *
 * 这类代码注入器可以将 Kotlin 属性委托生成的实例字段（例如：`name$delegate`）的读取替换为静态委托表达式，
 * 并移除该实例字段，从而减少实例内存占用。
 */
class InlinedDelegateFieldCodeInjectorSupport : CodeInjectorSupport {
    private val logger = thisLogger()

    private data class Instruction(val index: Int, val opcode: Int, val operand: Int?)

    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorScope.targetClassKey) ?: return
        val infos = codeInjector::class.findAnnotations<InlinedDelegateField>()

        val inlineAll = codeInjector::class.findAnnotation<InlinedDelegateFields>() != null
        val candidates: List<Pair<String, String?>> = when {
            inlineAll -> {
                targetClass.declaredFields
                    .asSequence()
                    .filter { it.name.endsWith("\$delegate") }
                    .map { field ->
                        val propertyName = field.name.removeSuffix("\$delegate")
                        propertyName to null
                    }
                    .toList()
            }
            infos.isNotEmpty() -> infos.map { it.value to it.delegateExpression.orNull() }
            else -> emptyList()
        }
        if (candidates.isEmpty()) return

        for ((propertyName, explicitExpression) in candidates) {
            val delegateFieldName = "${propertyName}\$delegate"
            val delegateExpression = explicitExpression ?: inferDelegateExpression(targetClass, delegateFieldName)
            if (delegateExpression == null) {
                logger.warn("Cannot infer delegate expression for field $delegateFieldName in ${targetClass.name}, skipped")
                return
            }

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

    private fun inferDelegateExpression(targetClass: CtClass, delegateFieldName: String): String? {
        val constPool = targetClass.classFile2.constPool
        val constructors = targetClass.declaredConstructors
        for (constructor in constructors) {
            val codeAttribute = constructor.methodInfo2.codeAttribute ?: continue
            val iterator = codeAttribute.iterator()
            val instructions = mutableListOf<Instruction>()
            while (iterator.hasNext()) {
                val index = iterator.next()
                val op = iterator.byteAt(index)
                val operand = readOperandIfAny(iterator, index, op)
                instructions.add(Instruction(index, op, operand))
                if (op != Opcode.PUTFIELD) continue
                val refIndex = operand ?: continue
                val owner = constPool.getFieldrefClassName(refIndex)
                val name = constPool.getFieldrefName(refIndex)
                if (owner != targetClass.name || name != delegateFieldName) continue

                val putPos = instructions.lastIndex
                return inferValueExpressionFromInstructions(constPool, instructions, putPos)
            }
        }
        return null
    }

    private fun inferValueExpressionFromInstructions(constPool: javassist.bytecode.ConstPool, instructions: List<Instruction>, putPos: Int): String? {
        fun prevIndex(from: Int): Int? {
            var i = from
            while (i >= 0) {
                val op = instructions[i].opcode
                if (op != Opcode.NOP) return i
                i--
            }
            return null
        }

        val valuePos = prevIndex(putPos - 1) ?: return null
        val valueInstruction = instructions[valuePos]
        val valueOp = valueInstruction.opcode

        // ALOAD_0 <value> PUTFIELD
        return when (valueOp) {
            Opcode.GETSTATIC -> {
                val index = valueInstruction.operand ?: return null
                val owner = constPool.getFieldrefClassName(index)
                val name = constPool.getFieldrefName(index)
                "$owner.$name"
            }
            Opcode.INVOKESTATIC -> {
                val index = valueInstruction.operand ?: return null
                val owner = constPool.getMethodrefClassName(index)
                val name = constPool.getMethodrefName(index)
                val desc = constPool.getMethodrefType(index)
                if (Descriptor.numOfParameters(desc) != 0) return null
                "$owner.$name()"
            }
            Opcode.INVOKEVIRTUAL -> {
                val index = valueInstruction.operand ?: return null
                // val owner = constPool.getMethodrefClassName(index)
                val name = constPool.getMethodrefName(index)
                val desc = constPool.getMethodrefType(index)
                if (Descriptor.numOfParameters(desc) != 0) return null

                val receiverPos = prevIndex(valuePos - 1) ?: return null
                val receiverInstruction = instructions[receiverPos]
                val receiverOp = receiverInstruction.opcode
                if (receiverOp != Opcode.GETSTATIC) return null
                val receiverIndex = receiverInstruction.operand ?: return null
                val receiverOwner = constPool.getFieldrefClassName(receiverIndex)
                val receiverName = constPool.getFieldrefName(receiverIndex)

                // 只接受静态 receiver（例如 Kotlin object 的 INSTANCE 字段）。
                "$receiverOwner.$receiverName.$name()"
            }
            Opcode.INVOKESPECIAL -> {
                val index = valueInstruction.operand ?: return null
                // val owner = constPool.getMethodrefClassName(index)
                val name = constPool.getMethodrefName(index)
                val desc = constPool.getMethodrefType(index)
                if (name != "<init>") return null
                if (Descriptor.numOfParameters(desc) != 0) return null

                val dupPos = prevIndex(valuePos - 1) ?: return null
                if (instructions[dupPos].opcode != Opcode.DUP) return null
                val newPos = prevIndex(dupPos - 1) ?: return null
                val newInstruction = instructions[newPos]
                if (newInstruction.opcode != Opcode.NEW) return null
                val newTypeIndex = newInstruction.operand ?: return null
                val newTypeName = constPool.getClassInfo(newTypeIndex)
                "new $newTypeName()"
            }
            else -> null
        }
    }

    private fun readOperandIfAny(iterator: CodeIterator, index: Int, op: Int): Int? {
        return when (op) {
            Opcode.GETSTATIC,
            Opcode.PUTSTATIC,
            Opcode.GETFIELD,
            Opcode.PUTFIELD,
            Opcode.INVOKESTATIC,
            Opcode.INVOKEVIRTUAL,
            Opcode.INVOKESPECIAL,
            Opcode.INVOKEINTERFACE,
            Opcode.NEW -> iterator.u16bitAt(index + 1)
            else -> null
        }
    }
}
