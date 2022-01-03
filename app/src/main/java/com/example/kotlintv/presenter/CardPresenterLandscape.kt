package com.example.kotlintv.presenter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.Presenter
import com.example.kotlintv.R
import com.example.kotlintv.listeners.progressBarCallback
import com.example.kotlintv.model.ContinueWatch
import com.example.kotlintv.model.Movie


class CardPresenterLandscape : Presenter(), progressBarCallback {

    private var mContext: Context? = null
    var progressBar1: ProgressBar? = null
    private var mcontinueWatchList: List<ContinueWatch>? = null
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.landscape_layout, parent, false)
        mContext = parent.context
        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                super.setSelected(selected)
            }
        }
        cardView.requestFocus()
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return LandscapeCardViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val movie = item as Movie
        val viewHolder1 = viewHolder as LandscapeCardViewHolder
        if (movie.title != null) {
            viewHolder1.title.text = movie.title
        }

        Log.d("Monisha", "onBindViewHolder: ${item.id}")
        viewHolder.mainImage.setBackgroundResource(R.drawable.frozen)
        setProgressBar(viewHolder.progressBar)
        mcontinueWatchList?.let{
            if(mcontinueWatchList!!.isNotEmpty()) {
                for (continueWatch in mcontinueWatchList!!) {
                    if(continueWatch.contentId == item.id.toInt()){
//                    var continueWatch: ContinueWatch = mcontinueWatchList!!.get(item.id.toInt())
                        Log.d("Monisha", "onBindViewHolder: continue watch value" + continueWatch.contentId)
                        setWatchedDuration(viewHolder.progressBar, item.id.toInt(), continueWatch)
                    }
                }
            }
        }
    }

    fun setProgressBar(progressBar: ProgressBar) {
        progressBar1 = progressBar
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    private inner class LandscapeCardViewHolder(view: View) : ViewHolder(view) {
        var mainImage: ImageView
        var title: TextView
        var cardView: CardView
        var progressBar: ProgressBar

        init {
            cardView = view.findViewById(R.id.cardview)
            mainImage = view.findViewById(R.id.main_image)
            title = view.findViewById(R.id.title)
            progressBar = view.findViewById(R.id.card_content_progress)
        }
    }

    fun setWatchedDuration(progressBar: ProgressBar,index:Int,continueWatch: ContinueWatch){
        Log.d("Monisha", "setWatchedDuration: inside")
        if(mcontinueWatchList != null && mcontinueWatchList!!.size>0) {
        /*val itr = mcontinueWatchList!!.listIterator()    // or, use `iterator()

        while (itr.hasNext()) {
            var iteratorValue = itr.next()
            if (iteratorValue.contentId == index) {
                if (iteratorValue.totalDuration > 0 && iteratorValue.lastWatchDuration > 0) {
                    progressValue = iteratorValue.totalDuration / iteratorValue.lastWatchDuration
                    progressBar1?.visibility = View.VISIBLE
                    progressBar1?.setProgress(progressValue.toInt())
                } else if (iteratorValue.lastWatchDuration == iteratorValue.totalDuration) {
                    progressValue = 100
                    progressBar1?.visibility = View.VISIBLE
                    progressBar1?.setProgress(progressValue.toInt())
                } else {
                    progressValue = 0
                    progressBar1!!.visibility = View.INVISIBLE
                    progressBar1?.setProgress(progressValue.toInt())
                }
            }
        }*/
            Log.d("Monisha", "setWatchedDuration: inside if")
            var progressValue:Long=0
            if(continueWatch.contentId==index){
                Log.d("Monisha", "setWatchedDuration: inside condition check")
                if (continueWatch.totalDuration > 0 && continueWatch.lastWatchDuration > 0) {
                    progressValue = (continueWatch.lastWatchDuration * 100 ) / continueWatch.totalDuration
                    progressBar?.visibility = View.VISIBLE
                    Log.d("Monisha", "setWatchedDuration: inside progress value if"+progressValue)
                    progressBar?.setProgress(progressValue.toInt())
                } else if (continueWatch.lastWatchDuration == continueWatch.totalDuration) {
                    progressValue = 100
                    progressBar?.visibility = View.VISIBLE
                    Log.d("Monisha", "setWatchedDuration: inside progress value else if"+progressValue)
                    progressBar?.setProgress(progressValue.toInt())
                } else {
                    progressValue = 0
                    progressBar!!.visibility = View.INVISIBLE
                    Log.d("Monisha", "setWatchedDuration: inside progress value else "+progressValue)
                    progressBar?.setProgress(progressValue.toInt())
                }
            }
        }
    }

    override fun getWatchedDuration(continueWatchList:List<ContinueWatch>) {
        /*if (duration > 0) {
            progressBar1?.visibility = View.VISIBLE
            progressBar1?.setProgress(duration)
            Log.d("Monisha", "getWatchedDuration: inside if")
        } else {
            //progressBar!!.visibility=View.INVISIBLE
            Log.d("Monisha", "getWatchedDuration: inside else")
        }*/

        mcontinueWatchList=continueWatchList
        Log.d("Monisha", "getWatchedDuration: mcontinueWatchList value"+mcontinueWatchList.toString())


       /* for (i in 0 until mAdapter.size()) {
            val listRow = mAdapter.get(i) as ListRow
            val listRowAdapter = listRow.adapter as ArrayObjectAdapter
            if (listRowAdapter.size() > 0) {
                listRowAdapter.notifyArrayItemRangeChanged(0, listRowAdapter.size())
            }
        }*/

    }
}