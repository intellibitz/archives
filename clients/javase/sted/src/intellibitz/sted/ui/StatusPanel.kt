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
 * $Id: StatusPanel.kt 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/StatusPanel.kt $
 */

package intellibitz.sted.ui


import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.event.IStatusListener
import intellibitz.sted.event.StatusEvent
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.GCButton
import intellibitz.sted.widgets.MemoryBar
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.ListSelectionModel
import javax.swing.Timer
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.util.StringTokenizer

class StatusPanel : JPanel() {
    private var progressBar: JProgressBar? = null
    private var counter: JLabel? = null
    private var flag: JLabel? = null
    private var lock: JLabel? = null
    private var status: JLabel? = null
    private val memoryBar: MemoryBar
    private var stedWindow: STEDWindow? = null
    /**
         * This fine grain notification tells listeners the exact range of cells,
         * rows, or columns that changed.
         */
    /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
    constructor(stedWindow: STEDWindow) : super() {
            this.stedWindow = stedWindow
            setBorder(BorderFactory.createLineBorder(getForeground()))
            val gridBagLayout: GridBagLayout = GridBagLayout()
            setLayout(gridBagLayout)
            val gridBagConstraints: GridBagConstraints = GridBagConstraints()
            gridBagConstraints.fill = GridBagConstraints.BOTH
            gridBagConstraints.weighty = 1
            gridBagConstraints.weightx = 1
            gridBagConstraints.gridheight = 1
            gridBagConstraints.gridwidth = 1
            gridBagConstraints.ipadx = 1
            status = JLabel()
            gridBagLayout.setConstraints(status, gridBagConstraints)
            add(status)
            progressBar = JProgressBar(gridBagConstraints as JProgressBar.HORIZONTAL).weightx = 0
            gridBagLayout.setConstraints(progressBar, gridBagConstraints)
            add(progressBar)
            val panel1: JPanel = JPanel()
            panel1.setBorder(BorderFactory.createLineBorder(getForeground()))
            counter = JLabel()
            initCounter()
            panel1.add(counter)
            gridBagLayout.setConstraints(panel1, gridBagConstraints)
            add(panel1)
            val panel2: JPanel = JPanel()
            panel2.setBorder(BorderFactory.createLineBorder(getForeground()))
            flag = JLabel()
            panel2.add(flag)
            gridBagLayout.setConstraints(panel2, gridBagConstraints)
            add(panel2)
            val panel3: JPanel = JPanel()
            panel3.setBorder(BorderFactory.createLineBorder(getForeground()))
            lock = JLabel()
            panel3.add(lock)
            gridBagLayout.setConstraints(panel3, gridBagConstraints)
            add(panel3)
            memoryBar = MemoryBar()
            gridBagLayout.setConstraints(memoryBar, gridBagConstraints)
            add(memoryBar)
            val imageIcon: Icon = Resources
                    .getSystemResourceIcon(Resources.getSetting(Resources.ICON_GC))
            val gcButton: GCButton = GCButton(imageIcon, Resources.getSystemResourceIcon(
                            Resources.getSetting(Resources.ICON_GC2)))
            gridBagLayout.setConstraints(gcButton, gridBagConstraints)
            add(gcButton)
        }
    fun runMemoryBar() {
            // update memory status every 2 seconds
            val timer: Timer = Timer(2000, memoryBar)
            timer.start()
        }
    fun clear() {
            clearStatus()
            clearProgress()
            initCounter()
            setCleanFlag()
            setLockFlag(true)
        }
    fun setStatus(msg: String) {
            status.setText(Resources.SPACE + msg)
        }
    private fun clearStatus() {
            status.setText(Resources.EMPTY_STRING)
        }
    fun clearProgress() {
            progressBar.setStringPainted(false)
            progressBar.setMinimum(0)
            progressBar.setMaximum(0)
            progressBar.setValue(0)
        }
    private fun initCounter() {
            counter.setText(COUNTER_INIT)
        }
    fun getProgressBar(): JProgressBar {
            return progressBar
        }
    private fun setTotalCount(total: Int) {
            if (total < 1)
            {
                initCounter()
            }
            else
            {
                val stringTokenizer: StringTokenizer =
                        StringTokenizer(counter.getText(), Resources.COLON)
                val stringBuffer: StringBuffer = StringBuffer()
                if (stringTokenizer.hasMoreTokens())
                {
                    stringBuffer.append(stringTokenizer.nextToken())
                }
                else
                {
                    stringBuffer.append(String.valueOf(0))
                }
                stringBuffer.append(stringBuffer as Resources.COLON).append(String.valueOf(total))
                counter.setText(stringBuffer.toString())
            }
        
        }
    private fun setCurrentCount(curr: Int) {
            if (curr >= 0)
            {
                val stringBuffer: StringBuffer = StringBuffer()
                stringBuffer.append(String.valueOf(curr + 1))
                stringBuffer.append(val as Resources.COLON) stringTokenizer: StringTokenizer =
                        StringTokenizer(counter.getText(), Resources.COLON)
                if (stringTokenizer.hasMoreTokens())
                {
                    // skip the current count
                    stringTokenizer.nextToken()
                    // keep the total count
                    stringBuffer.append(stringTokenizer.nextToken())
                }
                counter.setText(stringBuffer.toString())
            }
        
        }
    fun setNeatness(fontMap: FontMap) {
            clearProgress()
            setLockFlag(!fontMap.isFileWritable())
            if (fontMap.isDirty())
            {
                setDirtyFlag()
            }
            else
            {
                setCleanFlag()
            }
        
        }
    private fun setCleanFlag() {
            flag.setIcon(Resources.getCleanIcon())
        }
    private fun setDirtyFlag() {
            flag.setIcon(Resources.getDirtyIcon())
        }
    fun setLockFlag(flag: Boolean) {
            if (flag)
            {
                lock.setIcon(Resources.getLockIcon())
            }
            else
            {
                lock.setIcon(Resources.getUnLockIcon())
            }
        
        }
    override fun tableChanged(e: TableModelEvent) {
            val tableModel: TableModel = (e.getSource() as TableModel)
            setTotalCount(tableModel.getRowCount())
            setNeatness(stedWindow.getDesktop()
                    .getFontMap())
        }
    override fun valueChanged(e: ListSelectionEvent) {
            val listSelectionModel: ListSelectionModel =
                    (e.getSource() as ListSelectionModel)
            setCurrentCount(listSelectionModel.getMaxSelectionIndex())
        }
    override fun stateChanged(e: FontMapChangeEvent) {
            setNeatness(e.getFontMap())
        }
    override fun statusPosted(event: StatusEvent) {
            setStatus(event.getStatus())
        }

    companion object {
        private val COUNTER_INIT: String = "0:0"
    }
}
