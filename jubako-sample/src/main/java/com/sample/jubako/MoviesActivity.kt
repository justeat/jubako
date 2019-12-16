package com.sample.jubako

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.justeat.jubako.Jubako
import com.justeat.jubako.adapters.JubakoRecyclerViewHolder
import com.justeat.jubako.data.InstantLiveData
import com.justeat.jubako.extensions.addRecyclerView
import com.justeat.jubako.extensions.addView
import com.justeat.jubako.extensions.loadAsync
import com.justeat.jubako.extensions.withJubako
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_jubako_recycler.loadingIndicator
import kotlinx.android.synthetic.main.activity_jubako_recycler.recyclerView
import kotlin.random.Random

class MoviesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        Jubako.logger.enabled = true

        recyclerView.withJubako(
            activity = this,
            onAssembling = ::showLoading,
            onAssembled = { showContent() }).loadAsync {

            val repository = MovieRepository(application)

            //
            // Add a movie Hero Image
            //
            addView(
                data = repository.getHeroMovie(),
                viewBinder = { data, view ->
                    data?.let {
                        Picasso.get().load(it.assetUri).into(view.findViewById<ImageView>(R.id.movie))
                    }
                },
                view = { layoutInflater.inflate(R.layout.item_movie_hero, it, false) })

            val groups = repository.getMovies()

            for (i in 0..100) {
                groups.forEach { group ->
                    addRecyclerView(
                        viewHolder = {
                            CustomCarouselHolder(
                                layoutInflater.inflate(R.layout.carousel_movies, it, false)
                            )
                        },
                        recyclerViewId = R.id.jubakoCarousel,
                        viewBinder = { holder ->
                            holder.heading.text = group.title
                        },
                        data = InstantLiveData(group),
                        itemData = { data, position -> data.movies[position] },
                        itemCount = { data -> data.movies.size },
                        itemViewHolder = { inflater, parent ->
                            MovieItemViewHolder(inflater, parent)
                        },
                        itemBinder = { holder, data -> holder.bind(data) }
                    )
                }
            }
        }
    }

    private fun showContent() {
        loadingIndicator.isGone = true
        recyclerView.isGone = false
    }

    private fun showLoading() {
        loadingIndicator.isGone = false
        recyclerView.isGone = true
    }

    class MovieItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.carousel_item_movie,
            parent,
            false
        )
    ) {

        private val movieImageView = itemView.findViewById<ImageView>(R.id.movie)

        fun bind(data: Movie?) {
            data?.apply {
                Picasso.get().load(assetUri).into(movieImageView)
            }
        }
    }

    data class MovieGroup(val title: String, val movies: List<Movie>)
    data class Movie(val assetUri: Uri)

    class MovieRepository(val app: Application) {
        private val list1 = app.assets.list("1").map { Movie("${ASSETS_FILE_PATH}1/$it".toUri()) }
        private val list2 = app.assets.list("2").map { Movie("${ASSETS_FILE_PATH}2/$it".toUri()) }
        private val list3 = app.assets.list("3").map { Movie("${ASSETS_FILE_PATH}3/$it".toUri()) }

        fun getMovies(): List<MovieGroup> {
            return listOf(
                MovieGroup("Trending", list1 + list1),
                MovieGroup("Popular on Jubako Movies", list2 + list1),
                MovieGroup("Recently Added", list3 + list2),
                MovieGroup("Watch Again", list1 + list3),
                MovieGroup("Leaving Soon", list2),
                MovieGroup("Watch Later", list3)
            )
        }

        fun getHeroMovie(): LiveData<Movie> {
            return object : LiveData<Movie>() {
                override fun onActive() {
                    val all = list1 + list2 + list3
                    val movie = all[random.nextInt(0, all.size)]
                    postValue(movie)
                }
            }
        }
    }

    class CustomCarouselHolder(itemView: View) :
        JubakoRecyclerViewHolder<MovieGroup, Movie, MovieItemViewHolder>(itemView) {
        val heading: TextView = itemView.findViewById(R.id.heading)
    }
}

private val ASSETS_FILE_PATH = "file:///android_asset/"
private val random = Random(System.currentTimeMillis())
