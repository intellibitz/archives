/**
 * Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 * 168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 * http://www.intellibitz.com
 * training@intellibitz.com
 * +91 44 2247 5106
 * http://groups.google.com/group/etoe
 * http://sted.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * STED, Copyright (C) 2007 IntelliBitz Technologies
 * STED comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome
 * to redistribute it under the GNU GPL conditions;
 *
 * Visit http://www.gnu.org/ for GPL License terms.
 */

/**
 * $Id:ThreadEventSourceBase.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/event/ThreadEventSourceBase.kt $
 */

package intellibitz.sted.event

import javax.swing.event.EventListenerList

open class ThreadEventSourceBase : Thread(), IThreadEventSource {
    private val eventListenerList = EventListenerList()
    var threadEvent: ThreadEvent? = null
        protected set
    override var message: Any? = null
    override var result: Any? = null
    override var progress: Int = 0
    override var progressMaximum: Int = 0

    protected fun createThreadEvent() {
        if (threadEvent == null) {
            threadEvent = ThreadEvent(this)
        }
    }

    override fun fireThreadRunStarted() {
        val listeners = eventListenerList.listenerList
        createThreadEvent()
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IThreadListener::class.java) {
                (listeners[i + 1] as IThreadListener).threadRunStarted(threadEvent!!)
            }
        }
    }

    override fun fireThreadRunning() {
        val listeners = eventListenerList.listenerList
        createThreadEvent()
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IThreadListener::class.java) {
                (listeners[i + 1] as IThreadListener).threadRunning(threadEvent!!)
            }
        }
    }

    override fun fireThreadRunFailed() {
        val listeners = eventListenerList.listenerList
        createThreadEvent()
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IThreadListener::class.java) {
                (listeners[i + 1] as IThreadListener).threadRunFailed(threadEvent!!)
            }
        }
    }

    override fun fireThreadRunFinished() {
        val listeners = eventListenerList.listenerList
        createThreadEvent()
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IThreadListener::class.java) {
                (listeners[i + 1] as IThreadListener).threadRunFinished(threadEvent!!)
            }
        }
    }

    override fun addThreadListener(threadListener: IThreadListener) {
        eventListenerList.add(IThreadListener::class.java, threadListener)
    }
}
