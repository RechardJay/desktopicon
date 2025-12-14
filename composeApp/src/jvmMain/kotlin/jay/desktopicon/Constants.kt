package jay.desktopicon

import java.io.File

// 定义存储 .desktop 文件的标准目录
val iconDirectory: List<String> = listOf(
    "/usr/share/applications",
    // 用户个人的 application 目录
    System.getProperty("user.home") + File.separator + ".local/share/applications"
)
