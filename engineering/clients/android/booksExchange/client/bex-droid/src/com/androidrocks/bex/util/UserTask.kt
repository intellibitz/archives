/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Romain Guy Shelves project to suit Books-Exchange requirements.
 * Original source from Shelves - http://code.google.com/p/shelves/
 */

/*
 * Copyright (C) 2008 The Android Open Source Project, Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidrocks.bex.util

import android.os.Handler
import android.os.Message
import android.os.Process
import android.util.Log
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

abstract class UserTask<Params, Progress, Result> {

    private val mWorker: WorkerRunnable<Params, Result>
    private val mFuture: FutureTask<Result>

    @Volatile
    private var mStatus = Status.PENDING

    /**
     * Indicates the current status of the task. Each status will be set only once
     * during the lifetime of a task.
     */
    enum class Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,

        /**
         * Indicates that the task is running.
         */
        RUNNING,

        /**
         * Indicates that [UserTask.onPostExecute] has finished.
         */
        FINISHED
    }

    /**
     * Creates a new user task. This constructor must be invoked on the UI thread.
     */
    init {
        mWorker = object : WorkerRunnable<Params, Result>() {
            @Throws(Exception::class)
            override fun call(): Result {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                return doInBackground(*mParams!!)
            }
        }

        mFuture = object : FutureTask<Result>(mWorker) {
            override fun done() {
                var message: Message
                var result: Result? = null

                try {
                    result = get()
                } catch (e: InterruptedException) {
                    Log.w(LOG_TAG, e)
                } catch (e: ExecutionException) {
                    throw RuntimeException(
                        "An error occured while executing doInBackground()",
                        e.cause
                    )
                } catch (e: CancellationException) {
                    message = sHandler.obtainMessage(
                        MESSAGE_POST_CANCEL,
                        UserTaskResult(this@UserTask, null as Array<Result>?)
                    )
                    message.sendToTarget()
                    return
                } catch (t: Throwable) {
                    throw RuntimeException(
                        "An error occured while executing "
                                + "doInBackground()", t
                    )
                }

                message = sHandler.obtainMessage(
                    MESSAGE_POST_RESULT,
                    UserTaskResult(this@UserTask, result)
                )
                message.sendToTarget()
            }
        }
    }

    /**
     * Returns the current status of this task.
     *
     * @return The current status.
     */
    val status: Status
        get() = mStatus

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to [.execute]
     * by the caller of this task.
     *
     * This method can call [.publishProgress] to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     *
     * @return A result, defined by the subclass of this task.
     *
     * @see .onPreExecute
     * @see .onPostExecute
     * @see .publishProgress
     */
    abstract fun doInBackground(vararg params: Params): Result

    /**
     * Runs on the UI thread before [.doInBackground].
     *
     * @see .onPostExecute
     * @see .doInBackground
     */
    open fun onPreExecute() {
    }

    /**
     * Runs on the UI thread after [.doInBackground]. The
     * specified result is the value returned by [.doInBackground]
     * or null if the task was cancelled or an exception occured.
     *
     * @param result The result of the operation computed by [.doInBackground].
     *
     * @see .onPreExecute
     * @see .doInBackground
     */
    open fun onPostExecute(result: Result) {
    }

    /**
     * Runs on the UI thread after [.publishProgress] is invoked.
     * The specified values are the values passed to [.publishProgress].
     *
     * @param values The values indicating progress.
     *
     * @see .publishProgress
     * @see .doInBackground
     */
    open fun onProgressUpdate(vararg values: Progress) {
    }

    /**
     * Runs on the UI thread after [.cancel] is invoked.
     *
     * @see .cancel
     * @see .isCancelled
     */
    open fun onCancelled() {
    }

    /**
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally.
     *
     * @return <tt>true</tt> if task was cancelled before it completed
     *
     * @see .cancel
     */
    val isCancelled: Boolean
        get() = mFuture.isCancelled

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when <tt>cancel</tt> is called,
     * this task should never run.  If the task has already started,
     * then the <tt>mayInterruptIfRunning</tt> parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     * task should be interrupted; otherwise, in-progress tasks are allowed
     * to complete.
     *
     * @return <tt>false</tt> if the task could not be cancelled,
     * typically because it has already completed normally;
     * <tt>true</tt> otherwise
     *
     * @see .isCancelled
     * @see .onCancelled
     */
    fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return mFuture.cancel(mayInterruptIfRunning)
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return The computed result.
     *
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException If the computation threw an exception.
     * @throws InterruptedException If the current thread was interrupted
     * while waiting.
     */
    @Throws(InterruptedException::class, ExecutionException::class)
    fun get(): Result {
        return mFuture.get()
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result.
     *
     * @param timeout Time to wait before cancelling the operation.
     * @param unit The time unit for the timeout.
     *
     * @return The computed result.
     *
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException If the computation threw an exception.
     * @throws InterruptedException If the current thread was interrupted
     * while waiting.
     * @throws TimeoutException If the wait timed out.
     */
    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    fun get(timeout: Long, unit: TimeUnit?): Result {
        return mFuture.get(timeout, unit)
    }

    /**
     * Executes the task with the specified parameters. The task returns
     * itself (this) so that the caller can keep a reference to it.
     *
     * This method must be invoked on the UI thread.
     *
     * @param params The parameters of the task.
     *
     * @return This instance of UserTask.
     *
     * @throws IllegalStateException If [.getStatus] returns either
     * [UserTask.Status.RUNNING] or [UserTask.Status.FINISHED].
     */
    fun execute(vararg params: Params): UserTask<Params, Progress, Result> {
        if (mStatus != Status.PENDING) {
            when (mStatus) {
                Status.RUNNING -> throw IllegalStateException(
                    "Cannot execute task:"
                            + " the task is already running."
                )

                Status.FINISHED -> throw IllegalStateException(
                    "Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)"
                )
                
                else -> {}
            }
        }

        mStatus = Status.RUNNING

        onPreExecute()

        mWorker.mParams = params as Array<Params>
        sExecutor.execute(mFuture)

        return this
    }

    /**
     * This method can be invoked from [.doInBackground] to
     * publish updates on the UI thread while the background computation is
     * still running. Each call to this method will trigger the execution of
     * [.onProgressUpdate] on the UI thread.
     *
     * @param values The progress values to update the UI with.
     *
     * @see .onProgressUpdate
     * @see .doInBackground
     */
    protected fun publishProgress(vararg values: Progress) {
        sHandler.obtainMessage(
            MESSAGE_POST_PROGRESS,
            UserTaskResult(this, *values)
        ).sendToTarget()
    }

    private fun finish(result: Result) {
        onPostExecute(result)
        mStatus = Status.FINISHED
    }

    private class InternalHandler : Handler() {
        override fun handleMessage(msg: Message) {
            val result = msg.obj as UserTaskResult<*>
            when (msg.what) {
                MESSAGE_POST_RESULT ->                     // There is only one result
                    result.mTask.finish(result.mData!![0])

                MESSAGE_POST_PROGRESS -> result.mTask.onProgressUpdate(*(result.mData as Array<Any>))
                MESSAGE_POST_CANCEL -> result.mTask.onCancelled()
            }
        }
    }

    private abstract class WorkerRunnable<Params, Result> : Callable<Result> {
        var mParams: Array<Params>? = null
    }

    private class UserTaskResult<Data> {
        val mTask: UserTask<*, *, *>
        val mData: Array<out Data>?

        constructor(task: UserTask<*, *, *>, vararg data: Data) {
            mTask = task
            mData = data
        }
    }

    companion object {
        private const val LOG_TAG = "UserTask"

        private const val CORE_POOL_SIZE = 1
        private const val MAXIMUM_POOL_SIZE = 10
        private const val KEEP_ALIVE = 10

        private val sWorkQueue: BlockingQueue<Runnable> = LinkedBlockingQueue(MAXIMUM_POOL_SIZE)

        private val sThreadFactory: ThreadFactory = object : ThreadFactory {
            private val mCount = AtomicInteger(1)

            override fun newThread(r: Runnable): Thread {
                return Thread(r, "UserTask #" + mCount.getAndIncrement())
            }
        }

        private val sExecutor = ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE, KEEP_ALIVE.toLong(), TimeUnit.SECONDS, sWorkQueue, sThreadFactory
        )

        private const val MESSAGE_POST_RESULT = 0x1
        private const val MESSAGE_POST_PROGRESS = 0x2
        private const val MESSAGE_POST_CANCEL = 0x3

        private val sHandler = InternalHandler()
    }
}
