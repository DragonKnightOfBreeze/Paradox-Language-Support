package icu.windea.pls.gradle.benchmark

import me.champeau.jmh.JmhParameters
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.process.CommandLineArgumentProvider

/**
 * `chronicle.benchmark` 插件。
 *
 * 负责应用 JMH 插件、注册 `benchmark` / `benchmarkReport`，并隐藏 classpath 与报告细节。
 */
class ChronicleBenchmarkPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("me.champeau.jmh")

        val extension = project.extensions.create("chronicleBenchmark", ChronicleBenchmarkExtension::class.java)
        extension.jmhVersion.convention("1.37")
        extension.launcherClass.convention("icu.windea.pls.test.ChronicleBenchmarkLauncher")
        extension.resultDir.convention(project.layout.buildDirectory.dir("reports/benchmarks"))
        extension.reportOutputDir.convention(project.layout.projectDirectory.dir("documents/reports/benchmarks"))

        project.extensions.configure(JmhParameters::class.java) {
            jmhVersion.set(extension.jmhVersion)
            includeTests.set(false)
        }

        project.tasks.register("benchmark", ChronicleBenchmarkTask::class.java) {
            group = "verification"
            description = "Run JMH benchmarks. Use --benchmarks to filter benchmarks, like --tests."

            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val mainSourceSet = sourceSets.getByName("main")
            val jmhSourceSet = sourceSets.getByName("jmh")

            dependsOn("jmhCompileGeneratedClasses")
            mainClass.set(extension.launcherClass)
            classpath = project.files(
                mainSourceSet.output,
                jmhSourceSet.output,
                project.layout.buildDirectory.dir("jmh-generated-classes"),
                project.layout.buildDirectory.dir("jmh-generated-resources"),
                jmhSourceSet.runtimeClasspath,
            )

            System.getProperties().stringPropertyNames()
                .filter { it.startsWith("chronicle.") }
                .forEach { key -> systemProperty(key, System.getProperty(key)) }
            systemProperty(
                "chronicle.benchmark.classesDir",
                System.getProperty("chronicle.benchmark.classesDir")
                    ?: jmhSourceSet.output.classesDirs.asPath,
            )
            systemProperty(
                "chronicle.benchmark.reportDir",
                System.getProperty("chronicle.benchmark.reportDir")
                    ?: extension.resultDir.get().asFile.absolutePath,
            )

            argumentProviders.add(CommandLineArgumentProvider { benchmarks.getOrElse(emptyList()) })
            outputs.upToDateWhen { false }
        }

        project.tasks.register("benchmarkReport", ChronicleBenchmarkReportTask::class.java) {
            group = "verification"
            description = "Run benchmarks and generate timestamped JSON/TEXT reports."
            dependsOn("benchmark")

            val reportDirProperty = System.getProperty("chronicle.benchmark.reportDir")
            if (reportDirProperty != null) {
                inputDir.set(project.file(reportDirProperty))
            } else {
                inputDir.set(extension.resultDir)
            }
            outputBaseDir.set(extension.reportOutputDir)
            jmhVersion.set(extension.jmhVersion)
            gradleVersion.set(project.gradle.gradleVersion)
            command.set(project.gradle.startParameter.taskRequests.joinToString(" ") { it.args.joinToString(" ") })
            outputs.upToDateWhen { false }
        }
    }
}
