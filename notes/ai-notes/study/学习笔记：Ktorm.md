# 学习笔记：Storm

> 目标：聚焦“快速上手”与“集成 Spring Boot”等通用场景；示例使用 SQLite 作为数据库。

---

## 快速开始（Kotlin + SQLite）

- **Gradle 依赖（Kotlin DSL）**：
```kotlin
dependencies {
  implementation("org.ktorm:ktorm-core:<ver>")
  implementation("org.ktorm:ktorm-support-sqlite:<ver>")
  implementation("org.xerial:sqlite-jdbc:<ver>")
}
```

- **连接数据库**：
```kotlin
import org.ktorm.database.Database
import org.ktorm.support.sqlite.SqliteDialect

val db = Database.connect(
  url = "jdbc:sqlite:./data/app.db",
  driver = "org.sqlite.JDBC",
  dialect = SqliteDialect()
)
```

- **建表（原生 DDL，一次性执行）**：
```kotlin
db.useConnection { conn ->
  conn.createStatement().use { st ->
    st.executeUpdate(
      """
      CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        created_at TEXT NOT NULL
      )
      """.trimIndent()
    )
  }
}
```

- **表定义与 CRUD（Ktorm DSL）**：
```kotlin
import org.ktorm.schema.*
import java.time.LocalDateTime

object Users: Table<Nothing>("users") {
  val id = int("id").primaryKey()
  val name = varchar("name")
  val createdAt = datetime("created_at")
}

// Insert
val newId = db.insertAndGenerateKey(Users) {
  set(Users.name, "Alice")
  set(Users.createdAt, LocalDateTime.now())
}

// Query
val rows = db.from(Users)
  .select()
  .where { Users.name like "%A%" }
  .orderBy(Users.id.asc())
  .limit(10)

// Update
val updated = db.update(Users) {
  set(Users.name, "Alice-2")
  where { Users.id eq 1 }
}

// Delete
val deleted = db.delete(Users) { Users.id eq 1 }

// 事务
val ok = db.useTransaction { /* 多个写操作 */ true }
```

- **分页**：
```kotlin
db.from(Users).select().orderBy(Users.id.asc()).limit(20).offset(40)
```

---

## 集成 Spring Boot（SQLite）

- **依赖**：同上；如需配置文件绑定，可加入 `spring-boot-starter`。
- **DataSource 与 Database Bean**：
```kotlin
@Configuration
class DbConfig {
  @Bean
  fun dataSource(): javax.sql.DataSource = org.springframework.jdbc.datasource.DriverManagerDataSource().apply {
    setDriverClassName("org.sqlite.JDBC")
    url = "jdbc:sqlite:./data/app.db"
  }

  @Bean
  fun database(ds: javax.sql.DataSource): org.ktorm.database.Database =
    org.ktorm.database.Database.connect(ds, dialect = org.ktorm.support.sqlite.SqliteDialect())
}
```

- **Repository 与 REST 示例**：
```kotlin
@Service
class UserRepo(private val db: Database) {
  fun list(): List<Map<String, Any?>> = db.from(Users).select().map { row ->
    mapOf(
      "id" to row[Users.id],
      "name" to row[Users.name],
      "createdAt" to row[Users.createdAt]
    )
  }

  fun create(name: String): Long = db.insertAndGenerateKey(Users) {
    set(Users.name, name)
    set(Users.createdAt, java.time.LocalDateTime.now())
  } as Long
}

@RestController
@RequestMapping("/users")
class UserController(private val repo: UserRepo) {
  @GetMapping fun list() = repo.list()
  @PostMapping fun create(@RequestParam name: String) = mapOf("id" to repo.create(name))
}
```

- **测试（内存库）**：
  - URL：`jdbc:sqlite::memory:`（单连接生存期内有效）；多连接共享可用：`jdbc:sqlite:file:memdb1?mode=memory&cache=shared`。
  - Spring 测试建议使用单连接数据源（如 `SingleConnectionDataSource`）或连接池最大连接数设为 1。

---

## 常见问题与实践

- **SQLite 并发**：默认写锁限制并发；可启用 WAL 提升并发读：
  - `PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL;`
- **时间类型**：SQLite 存为 TEXT/INTEGER；Ktorm `datetime` 映射 `LocalDateTime`，序列化为 ISO8601 文本即可。
- **自增主键**：`INTEGER PRIMARY KEY AUTOINCREMENT`；插入时用 `insertAndGenerateKey` 获取主键。
- **迁移**：Ktorm 不负责迁移；可配合 Flyway/Liquibase 维护 DDL。
- **性能**：合理建立索引；批量写入用事务；查询加 `limit/offset` 或 keyset 分页。

---

## 与本项目的关联（Paradox Language Support）

- **本地缓存/索引元数据**：将分析产物（如文件指纹、规则快照）落地到 SQLite，便于增量更新与回放。
- **用户偏好与统计**：记录功能使用与配置偏好（离线、可控）。
- **AI 特性落地**：缓存翻译/解释结果与引用来源，形成可复用知识库。

---

## 可扩展内容

- **迁移与版本**：Flyway/Liquibase 与 Ktorm 协作策略；灰度迁移与回滚。
- **对比与选型**：Ktorm vs Exposed vs jOOQ 的场景差异与基准评测。
- **异步与并发**：与协程/通道结合的访问模式；读写分离与批处理。
- **与 Spring 生态**：事务传播、校验（Bean Validation）、错误映射与统一响应。

---

## 参考链接

- **Ktorm 文档**：https://www.ktorm.org/
- **ktorm-support-sqlite**：https://github.com/kotlin-orm/ktorm/tree/master/ktorm-support-sqlite
- **SQLite JDBC（Xerial）**：https://github.com/xerial/sqlite-jdbc
- **Spring Boot DataSource**：https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data
