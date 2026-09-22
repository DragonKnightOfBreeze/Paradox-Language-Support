package icu.windea.pls.test

object ChronicleTestCapacities {
    fun includeAll() = model.includeAll

    fun includeBenchmark() = model.includeBenchmark

    fun includeAi() = model.includeAi

    fun includeRemote() = model.includeRemote

    fun includeLocalEnv() = model.includeLocalEnv

    fun includeConfigGenerator() = model.includeConfigGenerator

    // region Implementations

    @Volatile private var model = Model()

    private class Model {
        val includeAll = System.getProperty("chronicle.test.include.all").toBoolean()
        val includeBenchmark = System.getProperty("chronicle.test.include.benchmark").toBoolean()
        val includeAi = System.getProperty("chronicle.test.include.ai").toBoolean()
        val includeRemote = System.getProperty("chronicle.test.include.remote").toBoolean()
        val includeLocalEnv = System.getProperty("chronicle.test.include.local.env").toBoolean()
        val includeConfigGenerator = System.getProperty("chronicle.test.include.config.generator").toBoolean()
    }

    // endregion
}
