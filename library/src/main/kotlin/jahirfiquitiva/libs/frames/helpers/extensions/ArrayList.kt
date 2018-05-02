package jahirfiquitiva.libs.frames.helpers.extensions

inline fun <reified T> ArrayList<T>.jfilter(evaluate: (T) -> Boolean): ArrayList<T> {
    val newList = ArrayList<T>()
    for (item in this) if (evaluate(item)) newList.add(item)
    return newList
}