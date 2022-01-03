package com.example.kotlintv.presenter

import android.graphics.drawable.Drawable
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.example.kotlintv.*
import com.example.kotlintv.model.Channels
import com.example.kotlintv.model.Home
import com.example.kotlintv.model.Movie
import com.example.kotlintv.model.Sport
import kotlin.properties.Delegates

/**
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an ImageCardView.
 */
class CardPresenter(pageName: String) : Presenter() {
    private var mDefaultCardImage: Drawable?= null
    private var sSelectedBackgroundColor: Int by Delegates.notNull()
    private var sDefaultBackgroundColor: Int by Delegates.notNull()
    private var itemName: String = pageName

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        Log.d(TAG, "onCreateViewHolder")

        sDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background)
        sSelectedBackgroundColor = ContextCompat.getColor(parent.context,
            R.color.selected_background
        )
        mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.movie)

        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }
        cardView.requestFocus()
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        Log.d(TAG, "onBindViewHolder")
        val cardView = viewHolder.view as ImageCardView
        if (itemName == "Sports") {
            val sports = item as Sport
            if (sports.cardImageUrl != null) {
                cardView.titleText = sports.title
                cardView.contentText = sports.studio
                cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
                Glide.with(viewHolder.view.context)
                    .load(sports.cardImageUrl)
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(cardView.mainImageView)
            }
        } else if (itemName == "Movies"){
            val movie = item as Movie
            if (movie.cardImageUrl != null) {
                cardView.titleText = movie.title
                cardView.contentText = movie.studio
                cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
                Glide.with(viewHolder.view.context)
                    .load(movie.cardImageUrl)
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(cardView.mainImageView)
            }
        } else if (itemName == "Channels") {
            val channels = item as Channels
            if (channels.cardImageUrl != null) {
                cardView.titleText = channels.title
                cardView.contentText = channels.studio
                cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
                Glide.with(viewHolder.view.context)
                    .load(channels.cardImageUrl)
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(cardView.mainImageView)
            }
        } else {
            val home = item as Home
            if (home.cardImageUrl != null) {
                cardView.titleText = home.title
                cardView.contentText = home.studio
                cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
                Glide.with(viewHolder.view.context)
                    .load(home.cardImageUrl)
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(cardView.mainImageView)
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        Log.d(TAG, "onUnbindViewHolder")
        val cardView = viewHolder.view as ImageCardView
        // Remove references to images so that the garbage collector can free up memory
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val color = if (selected) sSelectedBackgroundColor else sDefaultBackgroundColor
        // Both background colors should be set because the view"s background is temporarily visible
        // during animations.
        view.setBackgroundColor(color)
        view.setInfoAreaBackgroundColor(color)
    }

    companion object {
        private const val TAG = "CardPresenter"
        private const val CARD_WIDTH = 500
        private const val CARD_HEIGHT = 250
    }
}