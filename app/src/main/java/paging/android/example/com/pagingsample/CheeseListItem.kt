package paging.android.example.com.pagingsample

/**
 * Common UI model between the [Cheese] data class and separators.
 * 在ui上使用的model，根據不同型別，在viewHolder裡面判斷要呈現item或者分隔線
 */
sealed class CheeseListItem(val name: String) {
    data class Item(val cheese: Cheese) : CheeseListItem(cheese.name)
    data class Separator(private val letter: Char) : CheeseListItem(letter.toUpperCase().toString())
}