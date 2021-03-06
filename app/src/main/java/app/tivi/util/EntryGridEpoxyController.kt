/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.util

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.StringRes
import app.tivi.HeaderBindingModel_
import app.tivi.PosterGridItemBindingModel_
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.emptyState
import app.tivi.header
import app.tivi.infiniteLoading
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.epoxy.EpoxyModelProperty
import app.tivi.ui.epoxy.TotalSpanOverride
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController

open class EntryGridEpoxyController<LI : EntryWithShow<out Entry>>(
    @StringRes private val titleRes: Int
) : PagedListEpoxyController<LI>(
        modelBuildingHandler = Handler(Looper.getMainLooper()),
        diffingHandler = EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {
    internal var callbacks: Callbacks<LI>? = null

    var isLoading by EpoxyModelProperty { false }
    var tmdbImageUrlProvider by EpoxyModelProperty<TmdbImageUrlProvider?> { null }

    interface Callbacks<in LI> {
        fun onItemClicked(item: LI)
    }

    @Suppress("UselessCallOnCollection")
    override fun addModels(models: List<EpoxyModel<*>>) {
        // Need to do this due to https://github.com/airbnb/epoxy/issues/567
        val modelsFiltered = models.filterNotNull()

        if (modelsFiltered.isNotEmpty()) {

            header {
                id("header")
                title(titleRes)
                spanSizeOverride(TotalSpanOverride)
            }

            super.addModels(modelsFiltered)
        } else {
            emptyState {
                id("item_placeholder")
                spanSizeOverride(TotalSpanOverride)
            }
        }
        if (isLoading) {
            infiniteLoading {
                id("loading_view")
                spanSizeOverride(TotalSpanOverride)
            }
        }
    }

    override fun buildItemModel(currentPosition: Int, item: LI?): EpoxyModel<*> {
        return if (item != null) buildItemModel(item) else buildItemPlaceholder(currentPosition)
    }

    protected open fun buildItemModel(item: LI): PosterGridItemBindingModel_ {
        return PosterGridItemBindingModel_()
                .id(item.generateStableId())
                .tmdbImageUrlProvider(tmdbImageUrlProvider)
                .tiviShow(item.show)
                .transitionName(item.show.homepage)
                .clickListener(View.OnClickListener { callbacks?.onItemClicked(item) })
    }

    protected open fun buildItemPlaceholder(index: Int): PosterGridItemBindingModel_ {
        return PosterGridItemBindingModel_()
                .id("placeholder_$index")
    }

    open fun isHeader(model: EpoxyModel<*>): Boolean {
        return model is HeaderBindingModel_
    }
}