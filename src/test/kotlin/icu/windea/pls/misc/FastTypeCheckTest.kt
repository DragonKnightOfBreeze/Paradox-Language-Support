package icu.windea.pls.misc

import icu.windea.pls.misc.FastTypeCheckTest.*
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class FastTypeCheckTest {
    enum class MemberType {
        Property, Value
    }

    sealed interface Member {
        val value: String
        val memberType: MemberType
    }

    class Property(val key: String, override val value: String) : Member {
        override val memberType: MemberType get() = MemberType.Property
    }

    class Value(override val value: String) : Member {
        override val memberType: MemberType get() = MemberType.Value
    }

    @Test
    fun testUsePredicateFunction() {
        // 存在强制类型转换（并且强转到类型不对？）

        val list = listOf(Property("key", "value"), Value("value"))
        for (m in list) {
            if (m.isProperty()) {
                val m = m as Property
                Assert.assertEquals("key = value", "${m.key} = ${m.value}")
            }
            if (m.isValue()) {
                Assert.assertEquals("value", m.value)
            }
        }

        // for(Member m : CollectionsKt.listOf(var2)) {
        //    int $i$f$isProperty = 0;
        //    if (m.getMemberType() == FastTypeCheckTest.MemberType.Property) {
        //       Intrinsics.checkNotNull(m, "null cannot be cast to non-null type icu.windea.pls.misc.FastTypeCheckTest.Property");
        //       Property m = (Property)m;
        //       Assert.assertEquals("key = value", m.getKey() + " = " + m.getValue());
        //    }
        //
        //    $i$f$isProperty = 0;
        //    if (m.getMemberType() == FastTypeCheckTest.MemberType.Value) {
        //       Assert.assertEquals("value", m.getValue());
        //    }
        // }
    }

    @Ignore
    @Test
    fun testUseContractsFunction() {
        // 存在强制类型转换（并且强转到类型不对？）

        val list = listOf(Property("key", "value"), Value("value"))
        for (m in list) {
            if (m.asProperty()) {
                Assert.assertEquals("key = value", "${m.key} = ${m.value}")
            }
            if (m.asValue()) {
                Assert.assertEquals("value", m.value)
            }
        }

        // for(Member m : CollectionsKt.listOf(var2)) {
        //    int $i$f$isProperty = 0;
        //    if (m.getMemberType() == FastTypeCheckTest.MemberType.Property) {
        //       Assert.assertEquals("key = value", ((Property)m).getKey() + " = " + ((Property)m).getValue());
        //    } else {
        //       $i$f$isProperty = 0;
        //       if (m.getMemberType() == FastTypeCheckTest.MemberType.Value) {
        //          Assert.assertEquals("value", ((Property)m).getValue());
        //       }
        //    }
        // }
    }

    @Test
    fun testUseScopeFunction() {
        val list = listOf(Property("key", "value"), Value("value"))
        for (m in list) {
            whenProperty(m) { m ->
                Assert.assertEquals("key = value", "${m.key} = ${m.value}")
            }
            whenValue(m) { m ->
                Assert.assertEquals("value", m.value)
            }
        }

        // for(Member m : CollectionsKt.listOf(var2)) {
        //    int $i$f$whenProperty = 0;
        //    if (m.getMemberType() == FastTypeCheckTest.MemberType.Property) {
        //       Intrinsics.checkNotNull(m, "null cannot be cast to non-null type icu.windea.pls.misc.FastTypeCheckTest.Property");
        //       Property m = (Property)m;
        //       int var7 = 0;
        //       Assert.assertEquals("key = value", m.getKey() + " = " + m.getValue());
        //    }
        //
        //    $i$f$whenProperty = 0;
        //    if (m.getMemberType() == FastTypeCheckTest.MemberType.Value) {
        //       Intrinsics.checkNotNull(m, "null cannot be cast to non-null type icu.windea.pls.misc.FastTypeCheckTest.Value");
        //       Value m = (Value)m;
        //       int var11 = 0;
        //       Assert.assertEquals("value", m.getValue());
        //    }
        // }
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Member.isProperty(): Boolean {
    return memberType == MemberType.Property
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Member.isValue(): Boolean {
    return memberType == MemberType.Value
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
private inline fun Member.asProperty(): Boolean {
    contract {
        returns() implies (this@asProperty is Property)
    }
    return memberType == MemberType.Property
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
private inline fun Member.asValue(): Boolean {
    contract {
        returns() implies (this@asValue is Value)
    }
    return memberType == MemberType.Value
}

private inline fun <R> whenProperty(m: Member, block: (Property) -> R): R? {
    if (m.memberType != MemberType.Property) return null
    m as Property
    return block(m)
}

private inline fun <R> whenValue(m: Member, block: (Value) -> R): R? {
    if (m.memberType != MemberType.Value) return null
    m as Value
    return block(m)
}
