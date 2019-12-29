package tech.flandia_yingm

fun getFilename(path: String): String {
    val index = path.lastIndexOf("/")
    return if (index == -1) path else path.substring(index + 1)
}