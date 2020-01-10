package dev.jahir.frames.utils

import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2core.DownloadBlock

interface BaseFetchListener : FetchListener {
    override fun onAdded(download: Download) {
        // TO
    }

    override fun onCancelled(download: Download) {
        // TO
    }

    override fun onCompleted(download: Download) {
        // TO
    }

    override fun onDeleted(download: Download) {
        // TO
    }

    override fun onDownloadBlockUpdated(
        download: Download,
        downloadBlock: DownloadBlock,
        totalBlocks: Int
    ) {
        // TO
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        // TO
    }

    override fun onPaused(download: Download) {
        // TO
    }

    override fun onProgress(
        download: Download,
        etaInMilliSeconds: Long,
        downloadedBytesPerSecond: Long
    ) {
        // TO
    }

    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
        // TO
    }

    override fun onRemoved(download: Download) {
        // TO
    }

    override fun onResumed(download: Download) {
        // TO
    }

    override fun onStarted(
        download: Download,
        downloadBlocks: List<DownloadBlock>,
        totalBlocks: Int
    ) {
        // TO
    }

    override fun onWaitingNetwork(download: Download) {
        // TO
    }
}