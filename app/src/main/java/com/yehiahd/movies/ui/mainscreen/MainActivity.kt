package com.yehiahd.movies.ui.mainscreen

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.androidnetworking.error.ANError
import com.yehiahd.movies.R
import com.yehiahd.movies.callback.OnMovieClickListener
import com.yehiahd.movies.model.Movie
import com.yehiahd.movies.ui.base.BaseActivity
import com.yehiahd.movies.ui.detailscreen.DetailScreen
import com.yehiahd.movies.util.Constant
import kotlinx.android.synthetic.main.activity_main.*
import org.reactivestreams.Subscription
import javax.inject.Inject

class MainActivity : BaseActivity(), OnMovieClickListener {

    private lateinit var mMainViewModel: MainViewModel

    @Inject
    lateinit var mainViewModelProvider: MainViewModelProvider

    @Inject
    lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var adapter: MoviesAdapter

    private lateinit var subscription: Subscription

    private var moviesType: String = Constant.Api.POPULAR

    override fun onCreate(savedInstanceState: Bundle?) {
        getActivityComponent().inject(mainActivity = this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }


    private fun init() {
        mMainViewModel = ViewModelProviders.of(this, mainViewModelProvider).get(MainViewModel::class.java)
        recyclerMovies.layoutManager = gridLayoutManager
        adapter = MoviesAdapter(this, ArrayList())
        adapter.onMovieClickListener = this
        recyclerMovies.adapter = adapter

        if (mMainViewModel.movies == null) {
            getMoviesByType(moviesType)
        } else {
            adapter.update(mMainViewModel.movies as ArrayList<Movie>)
        }

        tryAgainBtn.setOnClickListener { getMoviesByType(moviesType) }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return menu?.let {
            menuInflater.inflate(R.menu.menu_main, menu)
            true
        } ?: false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.top_rated -> {
                moviesType = Constant.Api.TOP_RATED
                getMoviesByType(moviesType)
                true
            }

            R.id.popular -> {
                moviesType = Constant.Api.POPULAR
                getMoviesByType(moviesType)
                true
            }

            else -> false
        }
    }


    private fun getMoviesByType(type: String) {
        progressBar.visibility = View.VISIBLE
        mMainViewModel.getMoviesByType(type)
                .doOnSubscribe { subscription = it }
                .subscribe({
                    if (it.isEmpty()) {
                        dontKnowLayout.visibility = View.VISIBLE
                    } else {
                        dontKnowLayout.visibility = View.GONE
                        adapter.update(it as ArrayList<Movie>)
                        recyclerMovies.scrollToPosition(0)
                    }
                    subscription.cancel()
                    progressBar.visibility = View.GONE
                }) {
                    progressBar.visibility = View.GONE
                    if (it is ANError) {
                        if (it.errorCode == 0)
                            dontKnowLayout.visibility = View.VISIBLE
                    } else
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
    }

    override fun onMovieClicked(movie: Movie) {
        startActivity(Intent(this, DetailScreen::class.java)
                .putExtra(Constant.Extra.MOVIE, movie))
    }

}
