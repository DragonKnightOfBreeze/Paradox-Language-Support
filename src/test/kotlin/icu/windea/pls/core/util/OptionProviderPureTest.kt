package icu.windea.pls.core.util

import org.junit.Assert.*
import org.junit.Test

/**
 * 纯 Kotlin 单元测试：OptionProvider
 *
 * 覆盖分支：
 * - fromEnv = false：优先使用非空 value，其次 defaultValue
 * - fromEnv = true：优先读取 env，其次 defaultEnv；若均不可用，回退到 value / defaultValue
 * - fromEnv(...) 两个重载方法的语义
 */
class OptionProviderPureTest {
    private fun nonExistingEnvKey(): String = "NON_EXISTING_PLS_" + System.nanoTime()

    // fromEnv = false：优先返回非空 value
    @Test
    fun testGet_fromEnvFalse_valuePriority() {
        val p = OptionProvider.from("X", "D")
        assertEquals("X", p.get())
    }

    // fromEnv = false：空字符串视为 null，回退到 defaultValue
    @Test
    fun testGet_fromEnvFalse_emptyValueFallbackToDefault() {
        val p = OptionProvider.from("", "D")
        assertEquals("D", p.get())
    }

    // fromEnv = true：env 存在且非空，返回 env 值（使用跨平台存在的 PATH 变量）
    @Test
    fun testGet_fromEnvTrue_envPresent_returnsEnvValue() {
        val p = OptionProvider.from<String?>(null, "D").fromEnv(true, "PATH", null)
        val v = p.get()
        assertNotNull(v)
        assertTrue(v!!.isNotEmpty())
    }

    // fromEnv = true：env 为空字符串，使用 defaultEnv（PATH）读取
    @Test
    fun testGet_fromEnvTrue_envBlank_usesDefaultEnv() {
        val p = OptionProvider.from<String?>(null, "D").fromEnv(true, "", "PATH")
        val v = p.get()
        assertNotNull(v)
        assertTrue(v!!.isNotEmpty())
    }

    // fromEnv = true：env 不存在，回退到 value
    @Test
    fun testGet_fromEnvTrue_envMissing_fallbackToValue() {
        val p = OptionProvider.from("X", "D").fromEnv(true, nonExistingEnvKey(), null)
        assertEquals("X", p.get())
    }

    // fromEnv = true：env 不存在且 value 为空，最终回退到 defaultValue
    @Test
    fun testGet_fromEnvTrue_envMissing_andEmptyValue_fallbackToDefault() {
        val p = OptionProvider.from("", "D").fromEnv(true, nonExistingEnvKey(), null)
        assertEquals("D", p.get())
    }

    // 验证 fromEnv(env, defaultEnv) 与 fromEnv(fromEnv, env, defaultEnv) 两个重载的行为
    @Test
    fun testFromEnvOverloads_behavior() {
        val original = OptionProvider.from("X", "D")

        // 仅设置 env/defaultEnv，不改变 fromEnv 标志（默认 false），仍应返回 value
        val p1 = original.fromEnv("PATH")
        assertEquals("X", p1.get())

        // 同时设置 fromEnv = true，应返回 env 值（PATH）
        val p2 = original.fromEnv(true, "PATH", null)
        val v2 = p2.get()
        assertNotNull(v2)
        assertTrue(v2!!.isNotEmpty())
    }
}
