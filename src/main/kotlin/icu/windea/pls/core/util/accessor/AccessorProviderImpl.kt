package icu.windea.pls.core.util.accessor

import com.jetbrains.rd.util.*
import icu.windea.pls.core.*
import java.lang.reflect.*
import kotlin.reflect.*
import kotlin.reflect.full.*

class AccessorProviderImpl<T : Any>(
    override val targetClass: KClass<T>
) : AccessorProvider<T> {
    private val allMemberProperties by lazy {
        buildSet {
            addAll(targetClass.declaredMemberProperties)
            addAll(targetClass.memberProperties)
        }
    }
    private val allStaticProperties by lazy { targetClass.staticProperties }
    private val allMemberFunctions by lazy {
        buildSet {
            addAll(targetClass.declaredMemberFunctions)
            addAll(targetClass.memberFunctions)
        }
    }
    private val allStaticFunctions by lazy { targetClass.staticFunctions }
    private val allJavaFields by lazy {
        buildSet {
            addAll(targetClass.java.declaredFields)
            addAll(targetClass.java.fields)
        }
    }
    private val allJavaMethods by lazy {
        buildSet {
            addAll(targetClass.java.declaredMethods)
            addAll(targetClass.java.methods)
        }
    }

    private val readAccessorCache = ConcurrentHashMap<String, ReadAccessorDelegate<T, *>>()
    private val writeAccessorCache = ConcurrentHashMap<String, WriteAccessorDelegate<T, *>>()
    private val invokeAccessorsCache = ConcurrentHashMap<String, List<InvokeAccessorDelegate<T>>>()

    override fun <V> get(target: T?, propertyName: String): V {
        validateTargetClass(target)
        return AccessorRunner.runInAccessorProvider {
            val accessor = findReadAccessor<V>(target, propertyName)
            accessor.get(target)
        }
    }

    override fun <V> set(target: T?, propertyName: String, value: V) {
        validateTargetClass(target)
        AccessorRunner.runInAccessorProvider {
            val accessor = findWriteAccessor<V>(target, propertyName)
            accessor.set(target, value)
        }
    }

    override fun invoke(target: T?, functionName: String, vararg args: Any?): Any? {
        validateTargetClass(target)
        return AccessorRunner.runInAccessorProvider block@{
            val accessors = findInvokeAccessors(target, functionName, *args)
            for (accessor in accessors) {
                try {
                    return@block accessor.invoke(target, *args)
                } catch (_: IllegalArgumentException) {
                    // ignore
                }
            }
            throw UnsupportedAccessorException()
        }
    }

    private fun validateTargetClass(target: T?) {
        if (target == null) return
        if (targetClass.isInstance(target)) return
        val message = "Target class is mismatched (expect: ${targetClass.qualifiedName}, actual: ${target::class.qualifiedName})"
        throw ClassCastException(message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <V> findReadAccessor(target: T?, propertyName: String): ReadAccessorDelegate<T, V> {
        val cacheKey = doGetReadCacheKey(target, propertyName)
        return readAccessorCache.getOrPut(cacheKey) { doFindReadAccessor<V>(target, propertyName) } as ReadAccessorDelegate<T, V>
    }

    private fun doGetReadCacheKey(target: T?, propertyName: String): String {
        return if (target != null) propertyName else "#$propertyName"
    }

    private fun <V> doFindReadAccessor(target: T?, propertyName: String): ReadAccessorDelegate<T, V> {
        if (target != null) {
            allMemberProperties
                .filter { it.name == propertyName }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Read.fromProperty<T, V>(it).takeIfAccessible() }
                ?.let { return it }
            allMemberFunctions
                .filter { it.isGetter(propertyName) }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Read.fromGetter<T, V>(it).takeIfAccessible() }
                ?.let { return it }
            allJavaFields
                .filter { it.name == propertyName && !Modifier.isStatic(it.modifiers) }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Read.fromJavaField<T, V>(it).takeIfAccessible() }
                ?.let { return it }
        } else {
            allStaticProperties
                .filter { it.name == propertyName }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Read.fromProperty<T, V>(it).takeIfAccessible() }
                ?.let { return it }
            allStaticFunctions
                .filter { it.isGetter(propertyName) }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Read.fromGetter<T, V>(it).takeIfAccessible() }
                ?.let { return it }
            allJavaFields
                .filter { it.name == propertyName && Modifier.isStatic(it.modifiers) }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Read.fromJavaField<T, V>(it).takeIfAccessible() }
                ?.let { return it }
        }
        return AccessorDelegateBuilder.Read.empty()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <V> findWriteAccessor(target: T?, propertyName: String): WriteAccessorDelegate<T, V> {
        val cacheKey = doGetWriteCacheKey(target, propertyName)
        return writeAccessorCache.getOrPut(cacheKey) { doFindWriteAccessor<V>(target, propertyName) } as WriteAccessorDelegate<T, V>
    }

    private fun doGetWriteCacheKey(target: T?, propertyName: String): String {
        return if (target != null) propertyName else "#$propertyName"
    }

    private fun <V> doFindWriteAccessor(target: T?, propertyName: String): WriteAccessorDelegate<T, V> {
        if (target != null) {
            allMemberProperties
                .filter { it.name == propertyName }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Write.fromProperty<T, V>(it)?.takeIfAccessible() }
                ?.let { return it }
            allMemberFunctions
                .filter { it.isGetter(propertyName) }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Write.fromSetter<T, V>(it).takeIfAccessible() }
                ?.let { return it }
            allJavaFields
                .filter { it.name == propertyName && !Modifier.isStatic(it.modifiers) }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Write.fromJavaField<T, V>(it).takeIfAccessible() }
                ?.let { return it }
        } else {
            allStaticProperties
                .filter { it.name == propertyName }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Write.fromProperty<T, V>(it)?.takeIfAccessible() }
                ?.let { return it }
            allStaticFunctions
                .filter { it.isGetter(propertyName) }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Write.fromSetter<T, V>(it).takeIfAccessible() }
                ?.let { return it }
            allJavaFields
                .filter { it.name == propertyName && Modifier.isStatic(it.modifiers) }
                .firstNotNullOfOrNull { AccessorDelegateBuilder.Write.fromJavaField<T, V>(it).takeIfAccessible() }
                ?.let { return it }
        }
        return AccessorDelegateBuilder.Write.empty()
    }

    private fun findInvokeAccessors(target: T?, functionName: String, vararg args: Any?): List<InvokeAccessorDelegate<T>> {
        val cacheKey = doGetInvokeCacheKey(target, functionName, *args)
        return invokeAccessorsCache.getOrPut(cacheKey) { doFindInvokeAccessors(target, functionName, *args) } as List<InvokeAccessorDelegate<T>>
    }

    private fun doGetInvokeCacheKey(target: T?, functionName: String, vararg args: Any?): String {
        return if (target != null) "${args.size}#$functionName" else "#${args.size}#$functionName"
    }

    private fun doFindInvokeAccessors(target: T?, functionName: String, vararg args: Any?): List<InvokeAccessorDelegate<T>> {
        if (target != null) {
            val expectedArgsSize = args.size + 1
            val result = mutableListOf<InvokeAccessorDelegate<T>>()
            allMemberFunctions
                .filter { it.name == functionName && it.parameters.size == expectedArgsSize }
                .mapNotNullTo(result) { AccessorDelegateBuilder.Invoke.fromFunction<T>(it).takeIfAccessible() }
            allJavaMethods
                .filter { it.name == functionName && it.parameterTypes.size == expectedArgsSize && !Modifier.isStatic(it.modifiers) }
                .mapNotNullTo(result) { AccessorDelegateBuilder.Invoke.fromMethod<T>(it).takeIfAccessible() }
            return result
        } else {
            val expectedArgsSize = args.size
            val result = mutableListOf<InvokeAccessorDelegate<T>>()
            allStaticFunctions
                .filter { it.name == functionName && it.parameters.size == expectedArgsSize }
                .mapNotNullTo(result) { AccessorDelegateBuilder.Invoke.fromFunction<T>(it).takeIfAccessible() }
            allJavaMethods
                .filter { it.name == functionName && it.parameterTypes.size == expectedArgsSize && Modifier.isStatic(it.modifiers) }
                .mapNotNullTo(result) { AccessorDelegateBuilder.Invoke.fromMethod<T>(it).takeIfAccessible() }
            return result
        }
    }

    private fun <T : AccessorDelegate> T.takeIfAccessible(): T? = takeIf { it.setAccessible() }
}
