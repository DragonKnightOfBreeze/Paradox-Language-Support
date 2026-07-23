package icu.windea.pls.test

object ChronicleTestCapacities {
    fun includeAll() = System.getProperty("chronicle.test.include.all").toBoolean()

    fun includeBenchmark() = System.getProperty("chronicle.test.include.benchmark").toBoolean()

    fun includeAi() = System.getProperty("chronicle.test.include.ai").toBoolean()

    fun includeLocalDev() = System.getProperty("chronicle.test.include.local.env").toBoolean()

    fun includeConfigGenerator() = System.getProperty("chronicle.test.include.config.generator").toBoolean()
}
