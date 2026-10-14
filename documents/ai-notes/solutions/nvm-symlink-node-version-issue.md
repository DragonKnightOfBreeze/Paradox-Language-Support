# nvm use 后 node -v 仍输出旧版本（NVM_SYMLINK 非符号链接导致）

> - 模块：前端（参考文档）

- **现象**
  - 执行 `nvm use 22.12.0` 后，`node -v` 仍显示 `v20.10.0`。

- **诊断**
  - `where node` 输出：`D:\Program Files\nodejs\node.exe`。
  - `fsutil reparsepoint query "D:\\Program Files\\nodejs"`：返回“不是重解析点（not a reparse point）”。
  - 环境变量：
    - `NVM_HOME=C:\Users\29442\AppData\Roaming\nvm`（存在 `v22.12.0/` 目录）。
    - `NVM_SYMLINK=D:\Program Files\nodejs`。
    - PATH 同时包含 `C:\Users\29442\AppData\Roaming\nvm` 与 `D:\Program Files\nodejs`，Node 实际解析为后者。

- **根因**
  - `NVM_SYMLINK` 指向的 `D:\Program Files\nodejs` 是一个普通目录而非符号链接/联接目录，导致 nvm 无法通过切换目标来更新 `node.exe` 指向的版本。

- **修复方案（推荐）**
  1) 以管理员权限打开 PowerShell/CMD，关闭占用 Node 的进程（IDE 终端等）。
  2) 备份原目录：
     ```powershell
     ren "D:\Program Files\nodejs" nodejs.bak
     ```
  3) 让 nvm 重建符号链接/联接：
     ```powershell
     nvm use 22.12.0
     ```
  4) 校验：
     ```powershell
     fsutil reparsepoint query "D:\Program Files\nodejs"   # 现在应为重解析点
     where node                                              # 仍是 D:\Program Files\nodejs\node.exe
     node -v                                                 # 预期 v22.12.0
     ```

- **替代方案**
  - 如 nvm 仍无法创建链接，可手动建立目录联接（需管理员）：
    ```cmd
    mklink /J "D:\Program Files\nodejs" "C:\Users\29442\AppData\Roaming\nvm\v22.12.0"
    ```
  - 临时绕过（仅当前会话）：将目标版本目录临时置于 PATH 前缀：
    ```powershell
    $env:Path = "C:\Users\29442\AppData\Roaming\nvm\v22.12.0;" + $env:Path
    node -v
    ```

- **注意事项**
  - 第三方工具（npm/pnpm/yarn）在 `nodejs` 目录下的包装脚本可能被替换为新版本发行包自带的脚本，如需可重新安装或 `corepack enable`。
  - 若遇到“Access is denied”，请确认以管理员身份运行，并排除安全软件阻拦对 `D:\Program Files\nodejs` 的链接写入。

- **验证通过后**
  - 执行：
    ```powershell
    pnpm.cmd run docs:dev
    ```
    预期不再出现 `crypto.hash is not a function` 报错。
