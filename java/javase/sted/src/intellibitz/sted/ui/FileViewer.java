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
 * $Id:FileViewer.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/FileViewer.java $
 */

package intellibitz.sted.ui;

import intellibitz.sted.event.IThreadListener;
import intellibitz.sted.event.ThreadEvent;
import intellibitz.sted.io.FileReaderThread;
import intellibitz.sted.util.Resources;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Container;
import java.awt.Font;
import java.io.File;

public class FileViewer
        extends JInternalFrame
        implements IThreadListener
{
    private JEditorPane editorPane;
    private File file;
    private FileReaderThread fileReaderThread;

    public FileViewer()
    {
        super(Resources.EMPTY_STRING, false, false, false, false);
        init();
    }

    public FileViewer(Icon icon)
    {
        this();
        setFrameIcon(icon);
    }


    public void init()
    {
        setBorder(BorderFactory.createEtchedBorder());
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        final JScrollPane scroller = new JScrollPane();
        scroller.getViewport().add(editorPane);
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(scroller);
        fileReaderThread = new FileReaderThread();
        fileReaderThread.addThreadListener(this);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
    }

    /**
     * Sets the font for this component.
     *
     * @param font the desired <code>Font</code> for this component
     * @see java.awt.Component#getFont
     *      <p/>
     *      preferred: true bound: true attribute: visualUpdate true
     *      description: The font for the component.
     */
    public void setFont(Font font)
    {
        super.setFont(
                font);    //To change body of overriden methods use Options | File Templates.
        if (editorPane != null)
        {
            editorPane.setFont(font);
        }
    }

    public void setFileName(String fileName)
    {
        setTitle(fileName);
        file = new File(fileName);
    }

    public void addThreadListener(IThreadListener threadListener)
    {
        fileReaderThread.addThreadListener(threadListener);
    }

    public void readFile()
    {
        fileReaderThread.setFile(file);
        SwingUtilities.invokeLater(fileReaderThread);
    }

    public void threadRunStarted(ThreadEvent e)
    {
    }

    public void threadRunning(ThreadEvent e)
    {
    }

    public void threadRunFailed(ThreadEvent e)
    {
    }

    public void threadRunFinished(ThreadEvent e)
    {
        editorPane.setText(e.getEventSource().getResult().toString());
    }

}
