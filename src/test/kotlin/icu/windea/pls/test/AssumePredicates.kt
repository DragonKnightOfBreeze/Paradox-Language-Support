package icu.windea.pls.test

import org.junit.Assume

@Suppress("unused")
object AssumePredicates {
    fun includeTool() = assumeInclude("tool", "Tool tests are not included")
    fun includeAi() = assumeInclude("ai", "AI tests are not included")
    fun includeBenchmark() = assumeInclude("benchmark", "Benchmarks are not included")
    fun includeLocalEnv() = assumeInclude("local.env", "Local environment only tests are not included")

    private fun includeAll() = System.getProperty("pls.test.include.all")

    private fun include(key: String) = System.getProperty("pls.test.include.$key")

    private fun assumeInclude(key: String, message: String) {
        val b = includeAll().toBoolean() || include(key).toBoolean()
        Assume.assumeTrue("message", b)
    }
}
