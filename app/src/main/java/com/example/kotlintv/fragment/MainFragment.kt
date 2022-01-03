package com.example.kotlintv.fragment

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.example.kotlinroomdatabase.data.ContinueWatchDatabase
import com.example.kotlintv.model.CategoryList
import com.example.kotlintv.model.ContinueWatch
import com.example.kotlintv.model.Movie
import com.example.kotlintv.player.online.*
import com.example.kotlintv.player.player.PlayerActivity
import com.example.kotlintv.presenter.*
import com.example.kotlintv.viewModel.ContinueWatchViewModel
import com.example.kotlintv.viewModel.WatchApplication
import com.example.kotlintv.viewModel.WatchViewModelFactory
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.lang.Exception
import java.util.*


class MainFragment : RowsSupportFragment() {
    private val mHandler = Handler()
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null
    val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private lateinit var routinesViewModelFactory: WatchApplication
    private lateinit var viewModel:ContinueWatchViewModel
    lateinit var cardPresenterLandscape : CardPresenterLandscape
    lateinit var arrayadapter: ArrayObjectAdapter
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onActivityCreated(savedInstanceState)
        //mContinueWatchViewModel = ViewModelProvider(this,viewM).get(ContinueWatchViewModel::class.java)
        //mContinueWatchViewModel = ViewModelProvider(requireActivity()).get(ContinueWatchViewModel::class.java)
        //this.viewModel = viewModel
        loadFiveDifferentRows()
        setupEventListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString())
            mBackgroundTimer!!.cancel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        var exampleMap: MutableMap<Any, Any> = mutableMapOf()
        exampleMap = loadMap(requireActivity()) //load map
        exampleMap.forEach { key, value -> println("-----> example map $key = $value") }

        /*val applicationScope = CoroutineScope(SupervisorJob())
        val database by lazy { ContinueWatchDatabase.getDatabase((requireActivity()?.application) as WatchApplication, applicationScope) }
        CoroutineScope(Dispatchers.IO).launch {
            val mContinueWatch: List<ContinueWatch> =
                database.continueWatchDao().getAllData()
            //getWatchedDuration(mContinueWatchList)
            //cardPresenterLandscape.getWatchedDuration(mContinueWatchList)
            CoroutineScope(Dispatchers.Main).launch{
                Log.d("------->","dummy log $mContinueWatch")
                cardPresenterLandscape.getWatchedDuration(mContinueWatch)
            }
        }*/
        val watchViewModel: ContinueWatchViewModel by requireActivity()?.viewModels {
            WatchViewModelFactory(((requireActivity()?.application) as WatchApplication).repository)
        }
        watchViewModel.mContinueWatch.observe(owner = this) {
            Log.d(TAG, "onResume:all data : \n  " + it.toString() + "\n")
            cardPresenterLandscape.getWatchedDuration(it)
            val size = arrayadapter.size()
            arrayadapter.notifyArrayItemRangeChanged(0, size)
        }
    }
    private fun loadFiveDifferentRows() {
        val list = CategoryList.setupMovies()
        val cardPresenterPortrait = CardPresenterPortrait()
        cardPresenterLandscape = CardPresenterLandscape()
        arrayadapter = ArrayObjectAdapter(cardPresenterLandscape)
        val cardPresenterSquare = CardPresenterSquare()
        val bigCardPresenterLand = BigCardPresenterLand(requireContext())
        val bigCardPresenterPortrait = BigCardPresenterPortrait(requireContext())
        var i = 0
        while (i < NUM_ROWS) {
            val header = HeaderItem(i.toLong(), CategoryList.MOVIE_CATEGORY[i])
            var listRowAdapter: ArrayObjectAdapter = when (i) {
                0 -> ArrayObjectAdapter(cardPresenterPortrait)
                1 -> arrayadapter
                2 -> ArrayObjectAdapter(bigCardPresenterLand)
                3 -> ArrayObjectAdapter(cardPresenterSquare)
                4 -> ArrayObjectAdapter(bigCardPresenterPortrait)
                else -> ArrayObjectAdapter(cardPresenterPortrait)
            }
            for (j in 0 until NUM_COLS) {
                listRowAdapter.add(list!![j % 5])
            }
            rowsAdapter.add(ListRow(header, listRowAdapter))
            i++
        }

        adapter = rowsAdapter
    }

    private fun setupEventListeners() {

        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder ?, item: Any ?,
            rowViewHolder: RowPresenter.ViewHolder ?, row: Row ?) {
            Log.d(TAG, "onItemClicked: ")
            val indexOfRow = rowsAdapter.indexOf(row)
            val indexOfItem = ((row as ListRow).adapter as ArrayObjectAdapter).indexOf(item)
            Toast.makeText(context, "$indexOfItem-$indexOfRow", Toast.LENGTH_SHORT).show()

            if(indexOfRow != 1){
                if ((rowViewHolder as ListRowPresenter.ViewHolder).selectedPosition/2 == 1) {
                    startActivity(
                        Intent(context, PlayerActivity::class.java)
                            .putExtra(BUNDLE_TITLE, "Big Bunny")
                            .putExtra(BUNDLE_URL,
                                //"https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8")
                                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                            .putExtra(BUNDLE_MIME_TYPES, MimeTypes.APPLICATION_MP4))
                } else {
                    startActivity(
                        Intent(context, PlayerActivity::class.java)
                            .putExtra(BUNDLE_TITLE, "Big Bunny")
                            .putExtra(BUNDLE_URL,
                                //"https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8")
                                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                            .putExtra(BUNDLE_MIME_TYPES, MimeTypes.APPLICATION_MP4))
                }
            }
            else{
                var lastWatchedDuration : String? = null
                var exampleMap1: MutableMap<Any, Any> = mutableMapOf()
                exampleMap1 = loadMap(requireContext())
                //exampleMap1.forEach { (key, value) -> println("-----> $key = $value") }
                val itr = exampleMap1.keys.iterator()
                while (itr.hasNext()) {
                    val key = itr.next()
                    val value = exampleMap1[key]
                    Log.d("----->","key -$key value -$value")
                    Log.d("----->","indexOfItem -$indexOfItem")
                    if(indexOfItem.toString().equals(key) && value != null){
                        lastWatchedDuration = value.toString()
                        Log.d("----->","key -$key value -$value")
                        Log.d("----->","lastWatchedDuration -$lastWatchedDuration")
                        break
                    }
                }
                Log.d("----->","found value $lastWatchedDuration")
                startActivity(
                    Intent(context, PlayerActivity::class.java)
                        .putExtra(BUNDLE_TITLE, "Big Bunny")
                        .putExtra(CONTENT_ID, "$indexOfItem")
                        .putExtra(LAST_WATCHED_DURATION, "$lastWatchedDuration" )
                        .putExtra(BUNDLE_URL,
                            //"https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8")
                            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                        .putExtra(BUNDLE_MIME_TYPES, MimeTypes.APPLICATION_MP4))
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder ?,
            item: Any ?,
            rowViewHolder: RowPresenter.ViewHolder ?,
            row: Row ?
        ) {
            if (item is Movie) {
                mBackgroundUri = item.backgroundImageUrl
                //startBackgroundTimer()
            }
        }
    }
    fun loadMap(context: Context): MutableMap<Any, Any> {
        return try {
            val fos: FileInputStream = context.openFileInput("map")
            val os = ObjectInputStream(fos)
            val map: MutableMap<Any, Any> = os.readObject() as MutableMap<Any, Any>
            os.close()
            fos.close()
            map
        } catch (e: Exception) {
            mutableMapOf()
        }
    }
    companion object {
        private const val TAG = "MainFragment"
        private const val BACKGROUND_UPDATE_DELAY = 300
        private const val GRID_ITEM_WIDTH = 200
        private const val GRID_ITEM_HEIGHT = 200
        private const val NUM_ROWS = 6
        private const val NUM_COLS = 5
    }
}