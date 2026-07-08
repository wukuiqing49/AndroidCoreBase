package com.wkq.base.dialog

import android.content.Context
import com.wkq.base.R

object LoadingDialog {

    fun show(
        context: Context,
        message: String = context.getString(R.string.base_loading),
        cancelable: Boolean = false,
        onDismiss: (() -> Unit)? = null
    ): PopupHandle {
        return DialogKit.loading(
            context = context,
            message = message,
            cancelable = cancelable,
            onDismiss = onDismiss
        )
    }
}
