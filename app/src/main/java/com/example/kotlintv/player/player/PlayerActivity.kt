package com.example.kotlintv.player.player

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.*
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar


import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.kotlintv.DemoConstant.ACTION_CONTROL_TYPE_KEY
import com.example.kotlintv.DemoConstant.ACTION_PIP
import com.example.kotlintv.DemoConstant.ACTION_TYPE_PAUSE
import com.example.kotlintv.DemoConstant.ACTION_TYPE_PLAY
import com.example.kotlintv.DemoConstant.PAUSE
import com.example.kotlintv.DemoConstant.PLAY
import com.example.kotlintv.R
import com.example.kotlintv.player.download.DownloadTracker
import com.example.kotlintv.player.download.DownloadUtil
import com.example.kotlintv.player.download.MediaItemTag
import com.example.kotlintv.player.download.PieProgressDrawable
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.room.Query
import com.example.kotlintv.model.ContinueWatch
import com.example.kotlintv.player.online.*
import com.example.kotlintv.viewModel.ContinueWatchViewModel
import com.example.kotlintv.viewModel.WatchApplication
import com.example.kotlintv.viewModel.WatchViewModelFactory

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinroomdatabase.data.ContinueWatchDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


const val STATE_RESUME_WINDOW = "resumeWindow"
const val STATE_RESUME_POSITION = "resumePosition"
const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
const val STATE_PLAYER_PLAYING = "playerOnPlay"

class PlayerActivity : AppCompatActivity(), DownloadTracker.Listener {


   private val watchViewModel: ContinueWatchViewModel by viewModels {
        WatchViewModelFactory((application as WatchApplication).repository)
    }

    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var progressDrawable: ImageView
    private lateinit var playerView: PlayerView
    private lateinit var mProgressBar : ProgressBar
    private lateinit var playImage : ImageView
    private lateinit var pauseImage : ImageView
    private lateinit var rewindImage : ImageView
    private lateinit var forwardImage : ImageView
    private lateinit var seekBar : DefaultTimeBar
    private lateinit var focusedView : View
    private var lastWatchedDuration : Long = 0
    private lateinit var contentId : String
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isFullscreen = false
    private var isPlayerPlaying = true
    private val sharedPrefFile = "kotlinsharedpreference"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var mContinueWatchViewModel: ContinueWatchViewModel
    private val mediaItem: MediaItem by lazy {
        MediaItem.Builder()
            .setUri(intent.getStringExtra(BUNDLE_URL))
            .setMimeType(intent.getStringExtra(BUNDLE_MIME_TYPES))
            .setMediaMetadata(MediaMetadata.Builder().setTitle(intent.getStringExtra(BUNDLE_TITLE)).build())
            .setTag(MediaItemTag(-1, intent.getStringExtra(BUNDLE_TITLE)!!))
            .build()
    }
    private val playerViewModel by lazy {
        ViewModelProvider(this).get(PlayerViewModel::class.java)
    }
    private val ContinueWatchViewModel by lazy {
        ViewModelProvider(this).get(ContinueWatchViewModel::class.java)
    }
    private val pieProgressDrawable: PieProgressDrawable by lazy {
        PieProgressDrawable().apply {
            setColor(ContextCompat.getColor(this@PlayerActivity, R.color.teal_700))
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_video_player)
        playerView = findViewById(R.id.player_view)
        playImage = findViewById(R.id.exo_play)
        pauseImage = findViewById(R.id.exo_pause)
        forwardImage = findViewById(R.id.exo_ffwd)
        rewindImage = findViewById(R.id.exo_rew)
        seekBar = findViewById(R.id.exo_progress)
        focusedView = pauseImage
       // mContinueWatchViewModel = ViewModelProviders.of(this).get(ContinueWatchViewModel::class.java)
       // mContinueWatchViewModel = ViewModelProvider(this).get(ContinueWatchViewModel::class.java)
       /* val watchDuration : String = (intent.getStringExtra(LAST_WATCHED_DURATION).toString())
        if(watchDuration!=null)
        {
            lastWatchedDuration = watchDuration.toLongOrNull() ?: 0
        }
        else{
            lastWatchedDuration = 0
        }*/
        contentId = intent.getStringExtra(CONTENT_ID).toString()
        if (savedInstanceState != null) {
            currentWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
        }
        /*if(contentId != null && contentId.length>0 && !contentId.equals("null")) {
            lastWatchedDuration = getPositionOfPlayer(contentId.toInt()).also {
                Log.d("------->","lastWatchDuration: $lastWatchedDuration")

            }

        }*/

        DownloadUtil.getDownloadTracker(this).addListener(this)

        progressDrawable = findViewById<ImageView>(R.id.download_state).apply {
            setOnClickListener {
                if(DownloadUtil.getDownloadTracker(context).isDownloaded(mediaItem)) {
                    Snackbar.make(this.rootView, "You've already downloaded the video", Snackbar.LENGTH_SHORT)
                        .setAction("Delete") {
                            DownloadUtil.getDownloadTracker(this@PlayerActivity).removeDownload(mediaItem.playbackProperties?.uri)
                        }.show()
                } else {
                    val item = mediaItem.buildUpon()
                        .setTag((mediaItem.playbackProperties?.tag as MediaItemTag).copy(duration = exoPlayer.duration))
                        .build()
                    if(!DownloadUtil.getDownloadTracker(this@PlayerActivity).hasDownload(item.playbackProperties?.uri)) {
                        DownloadUtil.getDownloadTracker(this@PlayerActivity).toggleDownloadDialogHelper(this@PlayerActivity, item)
                    } else {
                        DownloadUtil.getDownloadTracker(this@PlayerActivity)
                            .toggleDownloadPopupMenu(this@PlayerActivity, this, item.playbackProperties?.uri)
                    }
                }
            }
        }

        playerViewModel.downloadPercent.observe(this) {
            it?.let {
                pieProgressDrawable.level = it.roundToInt()
                progressDrawable.invalidate()
            }
        }

        playerView.setControllerVisibilityListener(object : PlayerControlView.VisibilityListener{
            override fun onVisibilityChange(visibility: Int) {
                if (visibility == View.VISIBLE || visibility == View.GONE) {
                    focusedView.requestFocus()
                }
            }
        })

        setListeners()

       // getAllDbData()
    }

    private fun getAllDbData(){
        watchViewModel.mContinueWatch.observe(owner = this) { words ->
            // Update the cached copy of the words in the adapter.
            words.let {it
                println("Print Size"+it.size)
                if(it.size>0) {
                    println("Print 2nd Rows" + it[0])
                    println("Print 3rd Rows" + it[1])
                }

            }
        }
        //watchViewModel.insert()
    }



    private fun setListeners() {
        pauseImage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                pauseImage.background = application.getDrawable(R.drawable.focus_border)
                focusedView = pauseImage
            } else {
                pauseImage.background = application.getDrawable(R.drawable.unfocus_border)
            }
        }

        playImage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusedView = playImage
                playImage.background = application.getDrawable(R.drawable.focus_border)
            } else {
                playImage.background = application.getDrawable(R.drawable.unfocus_border)
            }
        }

        rewindImage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusedView = rewindImage
                rewindImage.background = application.getDrawable(R.drawable.focus_border)
            } else {
                rewindImage.background = application.getDrawable(R.drawable.unfocus_border)
            }
        }

        forwardImage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusedView = forwardImage
                forwardImage.background = application.getDrawable(R.drawable.focus_border)
            } else {
                forwardImage.background = application.getDrawable(R.drawable.unfocus_border)
            }
        }

        seekBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                focusedView = seekBar
                seekBar.background = application.getDrawable(R.drawable.focus_border)
            } else {
                seekBar.background = application.getDrawable(R.drawable.unfocus_border)
            }
        }

//        selectTracksButton.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(view: View?) {
//                openTrackSelectorDialog()
//            }
//        })
    }


    private fun initPlayer(){
        val downloadRequest: DownloadRequest? =
            DownloadUtil.getDownloadTracker(this).getDownloadRequest(mediaItem.playbackProperties?.uri)
        val mediaSource = if(downloadRequest == null) {
            // Online content
                if(mediaItem.mediaMetadata.title == "Big Bunny"){
                    // Create a data source factory.
                    val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }else {
                    HlsMediaSource.Factory(DownloadUtil.getHttpDataSourceFactory(this))
                        .createMediaSource(mediaItem)
                }
        } else {
            // Offline content
            DownloadHelper.createMediaSource(downloadRequest, DownloadUtil.getReadOnlyDataSourceFactory(this))
        }
        if(lastWatchedDuration > 0){
            exoPlayer = SimpleExoPlayer.Builder(this).build()
                .apply {
                    playWhenReady = isPlayerPlaying
                    seekTo(currentWindow, lastWatchedDuration)
                    setMediaSource(mediaSource, false)
                    prepare()
                }

        }else {
            exoPlayer = SimpleExoPlayer.Builder(this).build()
                .apply {
                    playWhenReady = isPlayerPlaying
                    seekTo(currentWindow, playbackPosition)
                    setMediaSource(mediaSource, false)
                    prepare()
                }
        }
        playerView.player = exoPlayer

    }
    
    private fun releasePlayer(){
        isPlayerPlaying = exoPlayer.playWhenReady
        playbackPosition = exoPlayer.currentPosition
        Log.d("-----> "," current position :"+ playbackPosition)
        currentWindow = exoPlayer.currentWindowIndex
        exoPlayer.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_RESUME_WINDOW, exoPlayer.currentWindowIndex)
        outState.putLong(STATE_RESUME_POSITION, exoPlayer.currentPosition)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        DownloadUtil.getDownloadTracker(this).downloads[mediaItem.playbackProperties?.uri!!]?.let {
            // Not so clean, used to set the right drawable on the ImageView
            // And start the Flow if the download is in progress
            onDownloadsChanged(it)
        }
        if(contentId != null && contentId.length>0 && !contentId.equals("null")) {
            lastWatchedDuration = getPositionOfPlayer(contentId.toInt()).also {
                Log.d("------->","lastWatchDuration: $lastWatchedDuration")
                if (Util.SDK_INT > 23) {
                    initPlayer()
                    playerView.onResume()
                }
            }

        }else {
            if (Util.SDK_INT > 23) {
                initPlayer()
                playerView.onResume()
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter(ACTION_PIP))
    }

    override fun onResume() {
        super.onResume()
        if(contentId != null && contentId.length>0 && !contentId.equals("null")) {
            lastWatchedDuration = getPositionOfPlayer(contentId.toInt()).also {
                Log.d("------->","lastWatchDuration: $lastWatchedDuration")
                if (Util.SDK_INT > 23) {
                    Log.d("Monisha", "onResume: inside if")
                    initPlayer()
                    playerView.onResume()
                }
            }

        }else {
            if (Util.SDK_INT > 23) {
                Log.d("Monisha", "onResume: inside else")
                initPlayer()
                playerView.onResume()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        if(contentId != null && contentId.length>0 && !contentId.equals("null")) {
            saveCurrentPosition()
        }
        if (Util.SDK_INT <= 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        playerViewModel.stopFlow()
        super.onStop()
        if (Util.SDK_INT > 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DownloadUtil.getDownloadTracker(this).removeListener(this)
        unregisterReceiver(broadcastReceiver)
    }

    override fun onDownloadsChanged(download: Download) {
        when(download.state) {
            Download.STATE_DOWNLOADING -> {
                if(progressDrawable.drawable !is PieProgressDrawable) progressDrawable.setImageDrawable(pieProgressDrawable)
                playerViewModel.startFlow(this, download.request.uri)
            }
            Download.STATE_QUEUED, Download.STATE_STOPPED -> {
                progressDrawable.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_pause))
            }
            Download.STATE_COMPLETED -> {
                progressDrawable.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_download_done))
            }
            Download.STATE_REMOVING -> {
                progressDrawable.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_download))
            }
            Download.STATE_FAILED, Download.STATE_RESTARTING -> { }
        }
    }


    @SuppressLint("NewApi")
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val param = PictureInPictureParams.Builder()
                .setActions(getAction())
                .setAspectRatio(getAspectRatio())
                .build()
            enterPictureInPictureMode(param)
        }
    }


    private fun getAspectRatio() : Rational {
        val width :Int = window.decorView.width
        val height :Int = window.decorView.height
        return Rational(width, height)
    }

    @SuppressLint("NewApi")
    private fun getAction() : List<RemoteAction> {
        var remoteAction : RemoteAction? = null
        if (playerView.player!!.isPlaying){
            val intent : PendingIntent = PendingIntent.getBroadcast(
                this,
                ACTION_TYPE_PAUSE,
                Intent(ACTION_PIP).putExtra(ACTION_CONTROL_TYPE_KEY, ACTION_TYPE_PAUSE),
                PendingIntent.FLAG_IMMUTABLE)
            val icon : Icon = Icon.createWithResource(this, R.drawable.ic_pause_24dp)
            remoteAction = RemoteAction(icon, PAUSE, PAUSE, intent)
        } else{
            val intent : PendingIntent = PendingIntent.getBroadcast(
                this,
                ACTION_TYPE_PLAY,
                Intent(ACTION_PIP).putExtra(ACTION_CONTROL_TYPE_KEY, ACTION_TYPE_PLAY),
                PendingIntent.FLAG_IMMUTABLE)
            val icon : Icon = Icon.createWithResource(this, R.drawable.ic_play_arrow_24dp)
            remoteAction = RemoteAction(icon, PLAY, PLAY, intent)
        }

        return listOf(remoteAction)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent != null && ACTION_PIP.equals(intent.action,true)){
                var type = intent.getIntExtra(ACTION_CONTROL_TYPE_KEY, 0)
                when(type){
                    ACTION_TYPE_PAUSE -> {
                        if (playerView != null ) playerView.player!!.pause()
                        updatePipAction(PLAY, ACTION_TYPE_PLAY, R.drawable.ic_play_arrow_24dp)
                    }
                    ACTION_TYPE_PLAY -> {
                        if (playerView != null ) playerView.player!!.play()
                        updatePipAction(PAUSE, ACTION_TYPE_PAUSE, R.drawable.ic_pause_24dp)
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun updatePipAction(actionTypeToShow : String, actionType: Int, resId : Int){

        val intent : PendingIntent = PendingIntent.getBroadcast(
            this,
            actionType,
            Intent(ACTION_PIP).putExtra(ACTION_CONTROL_TYPE_KEY, actionType),
            PendingIntent.FLAG_IMMUTABLE)
        val icon : Icon = Icon.createWithResource(this, resId)
        val remoteAction = RemoteAction(icon, actionTypeToShow, actionTypeToShow, intent)
        setPictureInPictureParams(PictureInPictureParams.Builder()
            .setActions(listOf(remoteAction)).build())

    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode){
            playerView.hideController()
        }else{
            playerView.showController()
        }
    }
    //@RequiresApi(Build.VERSION_CODES.N)
    fun saveCurrentPosition() {
        // Create User Object
        if(exoPlayer != null) {
            val updatedContent = ContinueWatch(
                0,
                contentId.toInt(),
                exoPlayer.currentPosition,
                exoPlayer.contentDuration
            )
            Log.d("------>", "contentId:$contentId")
            Log.d("------>", "crnt position:${exoPlayer.currentPosition}")
            Log.d("------>", "contentDuration:${exoPlayer.contentDuration}")

            watchViewModel.insert(updatedContent)
        }


    }
    fun getPositionOfPlayer(index:Int): Long {
        var currentPosition : Long = 0
        val applicationScope = CoroutineScope(SupervisorJob())
        val database by lazy { ContinueWatchDatabase.getDatabase(application as WatchApplication, applicationScope) }
        CoroutineScope(Dispatchers.IO).launch {
            val mContinueWatch: List<ContinueWatch> =
                database.continueWatchDao().getAllData()
            //getWatchedDuration(mContinueWatchList)
            //cardPresenterLandscape.getWatchedDuration(mContinueWatchList)
            CoroutineScope(Dispatchers.Main).launch{
                Log.d("------->","in player $mContinueWatch")
                if(mContinueWatch!=null && mContinueWatch.size>index) {
                    currentPosition = mContinueWatch.get(index).lastWatchDuration
                    lastWatchedDuration=currentPosition
                    initPlayer()
                    playerView.onResume()

                }
                //cardPresenterLandscape.getWatchedDuration(mContinueWatch)
            }

        }
        return currentPosition;
    }
}

