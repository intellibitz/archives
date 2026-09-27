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
 * $Id:ThreadEventSourceBase.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/event/ThreadEventSourceBase.java $
 */

package intellibitz.sted.event;

import javax.swing.event.EventListenerList;

public class ThreadEventSourceBase
        extends Thread
        implements IThreadEventSource
{
    private EventListenerList eventListenerList;
    private ThreadEvent threadEvent;
    private Object message;
    private Object result;
    private int progress;
    private int progressMaximum;

    //
    public ThreadEventSourceBase()
    {
        super();
        eventListenerList = new EventListenerList();
    }

    protected void createThreadEvent()
    {
        if (threadEvent == null)
        {
            threadEvent = new ThreadEvent(this);
        }
    }

    protected ThreadEvent getThreadEvent()
    {
        return threadEvent;
    }

    protected void setThreadEvent(ThreadEvent threadEvent)
    {
        this.threadEvent = threadEvent;
    }

    public void fireThreadRunStarted()
    {
        // Guaranteed to return a non-null array
        final Object[] listeners = eventListenerList.getListenerList();
        createThreadEvent();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == IThreadListener.class)
            {
                ((IThreadListener) listeners[i + 1])
                        .threadRunStarted(threadEvent);
            }
        }
    }

    public void fireThreadRunning()
    {
        // Guaranteed to return a non-null array
        final Object[] listeners = eventListenerList.getListenerList();
        createThreadEvent();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == IThreadListener.class)
            {
                ((IThreadListener) listeners[i + 1]).threadRunning(threadEvent);
            }
        }
    }

    public void fireThreadRunFailed()
    {
        // Guaranteed to return a non-null array
        final Object[] listeners = eventListenerList.getListenerList();
        createThreadEvent();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == IThreadListener.class)
            {
                ((IThreadListener) listeners[i + 1])
                        .threadRunFailed(threadEvent);
            }
        }
    }

    public void fireThreadRunFinished()
    {
        // Guaranteed to return a non-null array
        final Object[] listeners = eventListenerList.getListenerList();
        createThreadEvent();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == IThreadListener.class)
            {
                ((IThreadListener) listeners[i + 1])
                        .threadRunFinished(threadEvent);
            }
        }
    }

    public void addThreadListener(IThreadListener threadListener)
    {
        eventListenerList.add(IThreadListener.class, threadListener);
    }

    public Object getMessage()
    {
        return message;
    }

    public Object getResult()
    {
        return result;
    }

    public int getProgressMaximum()
    {
        return progressMaximum;
    }

    public void setProgressMaximum(int progressMaximum)
    {
        this.progressMaximum = progressMaximum;
    }

    public int getProgress()
    {
        return progress;
    }

    public void setProgress(int progress)
    {
        this.progress = progress;
    }

    public void setMessage(Object message)
    {
        this.message = message;
    }

    public void setResult(Object result)
    {
        this.result = result;
    }
}
