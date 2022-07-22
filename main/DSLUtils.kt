package main

import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionListener
import java.io.*
import javax.swing.*
import kotlin.system.exitProcess

object DSLUtils
{
    inline fun JMenuBar.menu(text : String, mnemonic : Char? = null, settings : JMenu.() -> Unit) : JMenu =
        add(JMenu(text).apply(settings).apply { mnemonic?.let { setMnemonic(it) } })

    fun JMenu.item(text : String, mnemonic : Char? = null, actionListener : ActionListener) : JMenuItem =
        add(JMenuItem(text).apply {
            addActionListener(actionListener)
            mnemonic?.let { setMnemonic(it) }
        })

    inline fun JFrame.menuBar(settings : JMenuBar.() -> Unit)
    {
        jMenuBar = JMenuBar().apply(settings)
    }

    fun Screen.openNew()
    {
        askForSavingCurrentState { textArea.text = "" }
        title = "untitled"
        originalText = ""
    }

    fun Screen.openFile() = with(fileChooser) {
        askForSavingCurrentState { textArea.text = "" }

        if(textArea.text.isEmpty() && showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            selectedFile.apply {
                title = name

                textArea.append(BufferedReader(FileReader(absolutePath)).run { readText().also { close() } })
            }

            originalText = textArea.text
        }
    }

    fun Screen.createFile() = with(fileChooser) {
        if(showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            PrintWriter(BufferedWriter(FileWriter(selectedFile.absolutePath))).run {
                write(textArea.text)
                close()
            }

            title = selectedFile.name
            originalText = textArea.text
        }
    }

    fun Screen.close() =
        askForSavingCurrentState { exitProcess(0) }

    private inline fun Screen.askForSavingCurrentState(originalAction : () -> Unit)
    {
        if(textArea.text != originalText)
        {
            when(JOptionPane.showConfirmDialog(textArea, "저장하시겠습니까?", "저장", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE ))
            {
                0 -> createFile()
                1 -> originalAction()
            }
        }

        else originalAction()
    }

    fun JTextArea.copyTo(clipboard: Clipboard) = StringSelection(selectedText).apply {
        clipboard.setContents(this, this)
    }

    fun cut(textArea : JTextArea, clipboard : Clipboard) = with(textArea) {
        copyTo(clipboard)
        replaceSelection("")
    }

    fun JTextArea.pasteFrom(clipboard: Clipboard) =
        try
        {
            replaceSelection(clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor).toString())
        }

        catch(e : NullPointerException)
        {
            e.printStackTrace()
        }
}