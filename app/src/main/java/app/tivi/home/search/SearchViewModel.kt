/*
 * Copyright 2019 Google LLC
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

package app.tivi.home.search

import androidx.lifecycle.viewModelScope
import app.tivi.interactors.SearchShows
import app.tivi.interactors.launchInteractor
import app.tivi.tmdb.TmdbManager
import app.tivi.util.TiviMvRxViewModel
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class SearchViewModel @AssistedInject constructor(
    @Assisted initialState: SearchViewState,
    private val searchShows: SearchShows,
    tmdbManager: TmdbManager
) : TiviMvRxViewModel<SearchViewState>(initialState) {
    private val searchQuery = BehaviorSubject.create<String>()

    init {
        disposables += searchQuery.debounce(300, TimeUnit.MILLISECONDS)
                .subscribe({
                    viewModelScope.launchInteractor(searchShows, SearchShows.Params(it))
                }, {
                    // TODO: onError
                })

        tmdbManager.imageProviderObservable
                .execute { copy(tmdbImageUrlProvider = it() ?: tmdbImageUrlProvider) }

        searchShows.observe().execute {
            copy(searchResults = it())
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.onNext(query)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: SearchViewState): SearchViewModel
    }

    companion object : MvRxViewModelFactory<SearchViewModel, SearchViewState> {
        override fun create(viewModelContext: ViewModelContext, state: SearchViewState): SearchViewModel? {
            val fragment: SearchFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.searchViewModelFactory.create(state)
        }
    }
}
