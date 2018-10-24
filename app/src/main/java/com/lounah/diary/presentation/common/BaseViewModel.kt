package com.lounah.diary.presentation.common

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

open class BaseViewModel : ViewModel() {

    val disposables = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        if (!disposables.isDisposed) {
            disposables.clear()
        }
    }
}