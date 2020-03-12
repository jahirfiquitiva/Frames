package dev.jahir.frames.data.listeners

import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2core.DownloadBlock

interface BaseFetchListener : FetchListener {
    override fun onAdded(download: Download) {
        // Do nothing
    }

    override fun onCancelled(download: Download) {
        // Do nothing
    }

    override fun onCompleted(download: Download) {
        // Do nothing
    }

    override fun onDeleted(download: Download) {
        // Do nothing
    }

    override fun onDownloadBlockUpdated(
        download: Download,
        downloadBlock: DownloadBlock,
        totalBlocks: Int
    ) {
        // Do nothing
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        // Do nothing
    }

    override fun onPaused(download: Download) {
        // Do nothing
    }

    override fun onProgress(
        download: Download,
        etaInMilliSeconds: Long,
        downloadedBytesPerSecond: Long
    ) {
        // Do nothing
    }

    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
        // Do nothing
    }

    override fun onRemoved(download: Download) {
        // Do nothing
    }

    override fun onResumed(download: Download) {
        // Do nothing
    }

    override fun onStarted(
        download: Download,
        downloadBlocks: List<DownloadBlock>,
        totalBlocks: Int
    ) {
        // Do nothing
    }

    override fun onWaitingNetwork(download: Download) {
        // Do nothing
    }
}