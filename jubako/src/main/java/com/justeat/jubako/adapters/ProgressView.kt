package com.justeat.jubako.adapters

/**
 * When providing a custom progress [RecyclerView.ViewHolder] they
 * must implement this interface so Jubako can communicate state to your view holder
 */
interface ProgressView {
    /**
     * WHen called you should show a progress indicator
     */
    fun onProgress()

    /**
     * When called you should show an error button or similar
     */
    fun onError(error: Throwable)

    /**
     * Gives you a callback that you can invoke with `retry.invoke()`  when you
     * want to try loading again
     */
    fun setRetryCallback(retry: () -> Unit)
}
