Checkstyle 配置与在 VS Code 中使用说明

我已在本项目中添加了一个简化的 Checkstyle 配置文件：

- 配置文件路径：`config/checkstyle/checkstyle.xml`
 - 配置文件路径（简化）：`config/checkstyle/checkstyle.xml`
 - Google 风格配置（已添加）：`config/checkstyle/google_checks.xml`

如何在 VS Code 中启用并使用 Checkstyle：

1. 安装扩展
   - 打开 VS Code 扩展（侧边栏 Extensions），搜索并安装：
     - `Extension Pack for Java`（标识 `vscjava.vscode-java-pack`）
     - `Java Test Runner`（通常包含在 Java Pack 中）
     - `Checkstyle for Java`（市场上常见扩展，标识 `shengchen.vscode-checkstyle`）

2. 指定本地 Checkstyle 配置文件（两种方式）
   - 方法 A（通过扩展设置）：
     - 打开命令面板（`Ctrl+Shift+P`），输入并选择 `Checkstyle: Set Checkstyle Configuration`（或在扩展设置中查找“Configuration files”选项）。
      - 选择 `Use a local file`，然后浏览到项目中的 `config/checkstyle/google_checks.xml`（或 `checkstyle.xml`）并确认。
   - 方法 B（手动在工作区设置中添加路径）：
     - 打开工作区设置（`.vscode/settings.json`），在扩展对应的配置键里添加或设置本地文件路径（注意：不同 Checkstyle 扩展的设置键名可能不同，通常在扩展文档中说明）。

3. 启用保存时检查（可选）
   - 在扩展设置中启用 `Scan on the fly` 或 `Scan on save`（不同版本命名略有差异）。

4. 运行检查
   - 在命令面板中执行 `Checkstyle: Check Project` 或在编辑器中使用右键菜单/工具栏的 Checkstyle 操作来检查当前文件或整个项目。

5. 结果与修复
   - 检查结果会显示在 Problems 面板或 Checkstyle 工具面板，双击可以跳转到对应代码位置，然后按规则修复或调整配置文件。

如果你希望我进一步：
- 我可以把 `maven-checkstyle-plugin` 配置写入项目的 `pom.xml`（将使 CI 与命令行检查可复现）；
- 或根据 Google 官方完整规则替换当前的简化 `checkstyle.xml`。
