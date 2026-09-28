package com.mobeegal.android.activity

import android.app.ListActivity
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.model.IconifiedText
import com.mobeegal.android.view.IconifiedTextListAdapter
import java.io.File
import java.util.Collections
import java.util.logging.Logger

class AndroidBrowser : ListActivity() {

    private enum class DISPLAYMODE {
        ABSOLUTE, RELATIVE
    }

    private var position: Int = 0
    var str1: String? = null
    var str2: String? = null
    private val displayMode = DISPLAYMODE.RELATIVE
    private val directoryEntries: MutableList<IconifiedText> = ArrayList()
    private var currentDirectory = File("/")
    private var ch: String? = null
    private var ch1: Int = 0
    private var myIntent: Intent? = null

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setTheme(android.R.style.Theme_Black)

        browseToRoot()
        this.setSelection(0)

        val bundleobj = this.intent.extras
        if (bundleobj != null) {
            str1 = bundleobj.getString("value1")
        }
    }

    private fun browseToRoot() {
        browseTo(File("/"))
    }

    private fun upOneLevel() {
        if (this.currentDirectory.parent != null) {
            this.browseTo(this.currentDirectory.parentFile)
        }
    }

    private fun browseTo(aDirectory: File) {
        if (this.displayMode == DISPLAYMODE.RELATIVE) {
            this.title = aDirectory.absolutePath + " :: "
        }
        if (aDirectory.isDirectory) {
            this.currentDirectory = aDirectory
            fill(aDirectory.listFiles())
        } else {
            val okButtonListener = OnClickListener { _, _ ->
                this@AndroidBrowser.openFile(aDirectory)
            }
            val viewButtonListener = OnClickListener { _, _ ->
                this@AndroidBrowser.playFile(aDirectory)
//					Intent playIntent = new Intent(AndroidFileBrowser.this, PlayMedia.class);
//                                        startActivityForResult(playIntent, 0);
            }

            val cancelButtonListener = OnClickListener { _, _ ->
                // Do nothing ^^
            }
//            AlertDialog.show(this, "Upload", position, " Do you want to Upload ?\n", "Upload", okButtonListener, "cancel", cancelButtonListener, "view", viewButtonListener, false, null);
        }
    }

    private fun openFile(aFile: File) {
        val filename = aFile.name

        if (checkEndsWithInStringArray(
                filename,
                resources.getStringArray(R.array.fileEndingImage)
            )
        ) {
            val uploadingFile = aFile.absolutePath
            val uploadfile = Bundle()
            val myIntent =
                Intent(this@AndroidBrowser, Uploadmultimedia::class.java)
            uploadfile.putString("key", uploadingFile)
            uploadfile.putString("key1", str1)
            logger.info("count = $str1")
            myIntent.putExtras(uploadfile)
            startActivityForResult(myIntent, 0)
        } else if (checkEndsWithInStringArray(
                filename,
                resources.getStringArray(R.array.fileEndingVideo)
            )
        ) {
            val uploadingFile = aFile.absolutePath
            val uploadfile = Bundle()
            val myIntent =
                Intent(this@AndroidBrowser, Uploadmultimedia::class.java)
            uploadfile.putString("key", uploadingFile)
            uploadfile.putString("key1", str1)
            logger.info("count = $str1")
            myIntent.putExtras(uploadfile)
            startActivityForResult(myIntent, 0)
        } else if (checkEndsWithInStringArray(
                filename,
                resources.getStringArray(R.array.fileEndingAudio)
            )
        ) {
            val uploadingFile = aFile.absolutePath
            val uploadfile = Bundle()
            val myIntent =
                Intent(this@AndroidBrowser, Uploadmultimedia::class.java)
            uploadfile.putString("key", uploadingFile)
            uploadfile.putString("key1", str1)
            logger.info("count = $str1")
            myIntent.putExtras(uploadfile)
            startActivityForResult(myIntent, 0)
        } else {
            Toast.makeText(
                this@AndroidBrowser, "FileFormat not Supported",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun playFile(aFile: File) {
        val filename = aFile.name
        if (checkEndsWithInStringArray(
                filename,
                resources.getStringArray(R.array.fileEndingImage)
            )
        ) {
            val uploadingimage = "file://" + aFile.absolutePath
            val uploadimage = Bundle()
            val myIntent1 =
                Intent(this@AndroidBrowser, UploadGallery::class.java)
            uploadimage.putString("key", uploadingimage)
            uploadimage.putString("key1", str1)
            logger.info("count = $str1")
            myIntent1.putExtras(uploadimage)
            startActivityForResult(myIntent1, 0)
        } else if (checkEndsWithInStringArray(
                filename,
                resources.getStringArray(R.array.fileEndingVideo)
            )
        ) {
            val uploadingFile = "file://" + aFile.absolutePath
            val uploadfile = Bundle()
            val myIntent1 = Intent(this@AndroidBrowser, PlayMedia::class.java)
            uploadfile.putString("key", uploadingFile)
            uploadfile.putString("key1", "Video File")
            myIntent1.putExtras(uploadfile)
            startActivityForResult(myIntent1, 0)
        } else if (checkEndsWithInStringArray(
                filename,
                resources.getStringArray(R.array.fileEndingAudio)
            )
        ) {
            val uploadingFile = "file://" + aFile.absolutePath
            val uploadfile = Bundle()
            val myIntent1 = Intent(this@AndroidBrowser, PlayMedia::class.java)
            uploadfile.putString("key", uploadingFile)
            uploadfile.putString("key1", "Audio File")
            myIntent1.putExtras(uploadfile)
            startActivityForResult(myIntent1, 0)
        } else {
            Toast.makeText(
                this@AndroidBrowser, "FileFormat not Supported",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun fill(files: Array<File>?) {
        this.directoryEntries.clear()

        // Add the "." == "current directory"
        this.directoryEntries.add(
            IconifiedText(
                ".",
                resources.getDrawable(R.drawable.folder)
            )
        )
        // and the ".." == 'Up one level'
        if (this.currentDirectory.parent != null) {
            this.directoryEntries.add(
                IconifiedText(
                    "..",
                    resources.getDrawable(R.drawable.uponelevel)
                )
            )
        }

        var currentIcon: Drawable? = null
        if (files != null) {
            for (currentFile in files) {
                if (currentFile.isDirectory) {
                    currentIcon = resources.getDrawable(R.drawable.folder)
                } else {
                    val fileName = currentFile.name

                    if (checkEndsWithInStringArray(
                            fileName, resources
                                .getStringArray(R.array.fileEndingImage)
                        )
                    ) {
                        currentIcon = resources.getDrawable(R.drawable.image)
                    } else if (checkEndsWithInStringArray(
                            fileName, resources
                                .getStringArray(R.array.fileEndingWebText)
                        )
                    ) {
                        currentIcon =
                            resources.getDrawable(R.drawable.webtext)
                    } else if (checkEndsWithInStringArray(
                            fileName, resources
                                .getStringArray(R.array.fileEndingPackage)
                        )
                    ) {
                        currentIcon = resources.getDrawable(R.drawable.packed)
                    } else if (checkEndsWithInStringArray(
                            fileName, resources
                                .getStringArray(R.array.fileEndingVideo)
                        )
                    ) {
                        currentIcon = resources.getDrawable(R.drawable.video)
                    } else if (checkEndsWithInStringArray(
                            fileName, resources
                                .getStringArray(R.array.fileEndingAudio)
                        )
                    ) {
                        currentIcon = resources.getDrawable(R.drawable.audio)
                    } else {
                        currentIcon = resources.getDrawable(R.drawable.text)
                    }
                }
                when (this.displayMode) {
                    DISPLAYMODE.ABSOLUTE ->
                        /* On absolute Mode, we show the full path */
                        this.directoryEntries.add(
                            IconifiedText(
                                currentFile.path, currentIcon
                            )
                        )
                    DISPLAYMODE.RELATIVE -> {
                        /* On relative Mode, we have to cut the
                         * current-path at the beginning */
                        val currentPathStringLenght =
                            this.currentDirectory.absolutePath.length
                        this.directoryEntries.add(
                            IconifiedText(
                                currentFile.absolutePath
                                    .substring(currentPathStringLenght),
                                currentIcon
                            )
                        )
                    }
                }
            }
        }
        Collections.sort(this.directoryEntries)

        val itla = IconifiedTextListAdapter(this)
        itla.setListItems(this.directoryEntries)
        this.listAdapter = itla
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        //int selectionRowID = (int) this.getSelectionRowID();
        val selectedFileString =
            this.directoryEntries[position].getText()
        if (selectedFileString == ".") {
            // Refresh
            this.browseTo(this.currentDirectory)
        } else if (selectedFileString == "..") {
            this.upOneLevel()
        } else if (selectedFileString == "data") {
            this.browseTo(File("/data/misc/"))
        } else {
            var clickedFile: File? = null
            when (this.displayMode) {
                DISPLAYMODE.RELATIVE ->
                    clickedFile = File(
                        this.currentDirectory
                            .absolutePath +
                            this.directoryEntries[position].getText()
                    )
                DISPLAYMODE.ABSOLUTE ->
                    clickedFile = File(
                        this.directoryEntries[position].getText()
                    )
            }
            if (clickedFile != null) {
                this.browseTo(clickedFile)
            }
        }
    }

    private fun checkEndsWithInStringArray(
        checkItsEnd: String,
        fileEndings: Array<String>
    ): Boolean {
        for (aEnd in fileEndings) {
            if (checkItsEnd.endsWith(aEnd)) {
                return true
            }
        }
        return false
    }

    companion object {
        private val logger = Logger.getLogger("Testcatalogs")
        protected const val SUB_ACTIVITY_REQUEST_CODE = 1337
    }
    //added image view
}
