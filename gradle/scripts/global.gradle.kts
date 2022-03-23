import java.util.*

loadAppProperties("app.properties")

fun loadAppProperties(from: String) {
    val props = Properties()
    val propsFile = File(obtainRootDir(), from)
    propsFile.inputStream().use(props::load)
    props.forEach { key, value -> extra.set(key.toString(), value.toString()) }
}

fun obtainRootDir(): File {
    if ("MDSYandexProject" !in rootProject.name) {
        return rootProject.rootDir.parentFile
    }

    return rootProject.rootDir
}