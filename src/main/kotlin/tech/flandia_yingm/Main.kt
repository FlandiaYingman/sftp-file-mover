@file:Suppress("SpellCheckingInspection")

package tech.flandia_yingm

import java.io.File
import java.net.InetSocketAddress

fun main() {
    val sftpFileMover = SftpFileMover(
        File("""D:\Gloomy\Luo\downloading-new"""),
        """0xC0DEiJqdnJ6S""",
        InetSocketAddress("149.28.146.81", 22),
        "gregortech",
        "gregortech",
        16000,
        3000,
        16
    )
    sftpFileMover.start()

    readLine()
}