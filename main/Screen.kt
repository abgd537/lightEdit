package main

import main.DSLUtils.close
import main.DSLUtils.copyTo
import main.DSLUtils.createFile
import main.DSLUtils.cut
import main.DSLUtils.item
import main.DSLUtils.menu
import main.DSLUtils.menuBar
import main.DSLUtils.openFile
import main.DSLUtils.openNew
import main.DSLUtils.pasteFrom
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.properties.Delegates

object Screen : JFrame("lightEdit v1.0a")
{
    private val scrSize = Dimension(1080, 720)
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    val fileChooser = JFileChooser().apply {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
        SwingUtilities.updateComponentTreeUI(this)

        isMultiSelectionEnabled = false
    }

    val textArea = JTextArea().apply {
        var fontSize by Delegates.observable(13) { _, _, new ->
            font = Font(font.name, font.style, new)
        }
        
        addKeyListener(object : KeyAdapter() {
            private var zoomMode by Delegates.observable(false) { _, _, new ->
                isEditable = !new
            }

            override fun keyPressed(e : KeyEvent)
            {
                when(e.keyCode) {
                    KeyEvent.VK_SHIFT -> zoomMode = true
                    KeyEvent.VK_ADD -> if(zoomMode) fontSize = floor(fontSize * 1.11).toInt()
                    KeyEvent.VK_SUBTRACT -> if(zoomMode) fontSize = ceil(fontSize / 1.11).toInt()
                }
            }

            override fun keyReleased(e : KeyEvent)
            {
                if(e.keyCode == KeyEvent.VK_SHIFT)
                    zoomMode = false
            }
        })

        lineWrap = true
    }
    
    var originalText = ""

    @JvmStatic
    fun main(args : Array<String>)
    {
        minimumSize = scrSize
        size = scrSize
        isResizable = true

        menuBar {
            menu("File", 'f') {
                item("new") { openNew() }
                item("open") { openFile() }
                item("save as") { createFile() }
                item("exit") { close() }
            }

            menu("Edit", 'e') {
                item("cut") { cut(textArea, clipboard) }
                item("copy") { textArea.copyTo(clipboard) }
                item("paste") { textArea.pasteFrom(clipboard) }
                item("selectAll") { textArea.selectAll() }
            }
        }

        contentPane.run {
            layout = BorderLayout()

            add(JScrollPane(textArea), BorderLayout.CENTER)
        }

        defaultCloseOperation = DO_NOTHING_ON_CLOSE

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) = close()
        })

        setLocationRelativeTo(null)
        isVisible = true
    }
}