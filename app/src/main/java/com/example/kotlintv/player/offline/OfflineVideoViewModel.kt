package com.example.kotlintv.player.offline

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kotlintv.player.download.DownloadUtil
import com.google.android.exoplayer2.offline.Download
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class OfflineVideoViewModel(application: Application): AndroidViewModel(application) {

    private val _downloads: MutableLiveData<List<Download>> = MutableLiveData()
    val downloads: LiveData<List<Download>>
        get() = _downloads

    private var job: CompletableJob = SupervisorJob()
    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + job)

    fun startFlow(context: Context) {
        coroutineScope.launch {
            DownloadUtil.getDownloadTracker(context).getAllDownloadProgressFlow().collect {
                _downloads.postValue(it)
            }
        }
    }

    fun stopFlow() {
        coroutineScope.cancel()
    }
}
