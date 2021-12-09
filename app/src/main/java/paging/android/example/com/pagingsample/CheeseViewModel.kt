/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package paging.android.example.com.pagingsample

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A simple [AndroidViewModel] that provides a [Flow]<[PagingData]> of delicious cheeses.
 * 提供 [Flow]<[PagingData]>給UI，這裡建構可以傳入dao，需要用ViewModelProvider.Factory 改寫
 */
class CheeseViewModel(private val dao: CheeseDao) : ViewModel() {
    /**
     * We use the Kotlin [Flow] property available on [Pager]. Java developers should use the
     * RxJava or LiveData extension properties available in `PagingRx` and `PagingLiveData`.
     * 重點就是提供這個資料給adapter 使用[Flow]的型別
     */
    val allCheeses: Flow<PagingData<CheeseListItem>> = Pager(config = PagingConfig(/**
     * A good page size is a value that fills at least a few screens worth of content on a
     * large device so the User is unlikely to see a null item.
     * You can play with this constant to observe the paging behavior.
     *
     * It's possible to vary this with list device size, but often unnecessary, unless a
     * user scrolling on a large device is expected to scroll through items more quickly
     * than a small device, such as when the large device uses a grid layout of items.
     *
     */
        pageSize = 60,

        /**
         * If placeholders are enabled, PagedList will report the full size but some items might
         * be null in onBind method (PagedListAdapter triggers a rebind when data is loaded).
         *
         * If placeholders are disabled, onBind will never receive null but as more pages are
         * loaded, the scrollbars will jitter as new pages are loaded. You should probably
         * disable scrollbars if you disable placeholders.
         * 開啟站位符的設定
         */
        enablePlaceholders = true,

        /**
         * Maximum number of items a PagedList should hold in memory at once.
         *
         * This number triggers the PagedList to start dropping distant pages as more are loaded.
         */
        maxSize = 200)) {
        dao.allCheesesByName()//拉取資料，注意這邊需要用PagingSource型別，此專案沒有自定義PagingSource。
    }.flow.map { pagingData ->
        pagingData
            // Map cheeses to common UI model.
            .map { cheese -> CheeseListItem.Item(cheese) }.insertSeparators { before: CheeseListItem?, after: CheeseListItem? ->
                if (before == null && after == null) {
                    // List is empty after fully loaded; return null to skip adding separator.
                    // 前面是null，後面是null ，列表是空的，回傳null
                    null
                } else if (after == null) {
                    // Footer; return null here to skip adding a footer.
                    // 後面是null ，頁尾，這邊忽略頁尾，回傳null
                    null
                } else if (before == null) {
                    // Header
                    // 前面是null ，頁首，回傳Separator型別，將後面的字串的第一個字串入Separator建構式
                    CheeseListItem.Separator(after.name.first())
                } else if (!before.name.first().equals(after.name.first(), ignoreCase = true)) {
                    // Between two items that start with different letters.
                    // 前後的字串的開頭字母不相同，回傳Separator型別，將後面的字串的第一個字串入Separator建構式
                    CheeseListItem.Separator(after.name.first())
                } else {
                    // Between two items that start with the same letter.
                    null
                }
            }
    }.cachedIn(viewModelScope)

    /**新增至ＤＢ*/
    fun insert(text: CharSequence) = ioThread {
        dao.insert(Cheese(id = 0, name = text.toString()))
    }

    /**從ＤＢ移除*/
    fun remove(cheese: Cheese) = ioThread {
        dao.delete(cheese)
    }
}
