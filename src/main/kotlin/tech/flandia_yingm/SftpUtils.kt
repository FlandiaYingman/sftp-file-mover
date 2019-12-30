package tech.flandia_yingm

object SftpUtils {

    fun getFilename(path: String): String {
        val index = path.lastIndexOf("/")
        return if (index == -1) path else path.substring(index + 1)
    }

    fun getRoot(path: String): String {
        val index = path.indexOf("/")
        return if (index == -1) path else path.substring(0, index)
    }

}