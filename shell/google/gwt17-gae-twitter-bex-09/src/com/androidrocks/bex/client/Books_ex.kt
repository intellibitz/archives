package com.androidrocks.bex.client

import com.google.gwt.core.client.EntryPoint
import com.google.gwt.core.client.GWT
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.dom.client.ClickHandler
import com.google.gwt.event.dom.client.KeyCodes
import com.google.gwt.event.dom.client.KeyUpEvent
import com.google.gwt.event.dom.client.KeyUpHandler
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.ui.Button
import com.google.gwt.user.client.ui.DialogBox
import com.google.gwt.user.client.ui.HTML
import com.google.gwt.user.client.ui.Label
import com.google.gwt.user.client.ui.RootPanel
import com.google.gwt.user.client.ui.TextBox
import com.google.gwt.user.client.ui.VerticalPanel

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
class Books_ex : EntryPoint {
    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private val greetingService = GWT.create(GreetingService::class.java) as GreetingServiceAsync

    /**
     * This is the entry point method.
     */
    override fun onModuleLoad() {
        val sendButton = Button("Send")
        val nameField = TextBox()
        nameField.text = "GWT User"

        // We can add style names to widgets
        sendButton.addStyleName("sendButton")

        // Add the nameField and sendButton to the RootPanel
        // Use RootPanel.get() to get the entire body element
        RootPanel.get("nameFieldContainer").add(nameField)
        RootPanel.get("sendButtonContainer").add(sendButton)

        // Focus the cursor on the name field when the app loads
        nameField.setFocus(true)
        nameField.selectAll()

        // Create the popup dialog box
        val dialogBox = DialogBox()
        dialogBox.setText("Remote Procedure Call")
        dialogBox.isAnimationEnabled = true
        val closeButton = Button("Close")
        // We can set the id of a widget by accessing its Element
        closeButton.element.id = "closeButton"
        val textToServerLabel = Label()
        val serverResponseLabel = HTML()
        val dialogVPanel = VerticalPanel()
        dialogVPanel.addStyleName("dialogVPanel")
        dialogVPanel.add(HTML("<b>Sending name to the server:</b>"))
        dialogVPanel.add(textToServerLabel)
        dialogVPanel.add(HTML("<br><b>Server replies:</b>"))
        dialogVPanel.add(serverResponseLabel)
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT)
        dialogVPanel.add(closeButton)
        dialogBox.widget = dialogVPanel

        // Add a handler to close the DialogBox
        closeButton.addClickHandler(ClickHandler {
            dialogBox.hide()
            sendButton.isEnabled = true
            sendButton.setFocus(true)
        })

        // Create a handler for the sendButton and nameField
        class MyHandler : ClickHandler, KeyUpHandler {
            /**
             * Fired when the user clicks on the sendButton.
             */
            override fun onClick(event: ClickEvent) {
                sendNameToServer()
            }

            /**
             * Fired when the user types in the nameField.
             */
            override fun onKeyUp(event: KeyUpEvent) {
                if (event.nativeKeyCode.toInt() == KeyCodes.KEY_ENTER) {
                    sendNameToServer()
                }
            }

            /**
             * Send the name from the nameField to the server and wait for a response.
             */
            private fun sendNameToServer() {
                sendButton.isEnabled = false
                val textToServer = nameField.text
                textToServerLabel.setText(textToServer)
                serverResponseLabel.setText("")
                greetingService.greetServer(textToServer,
                    object : AsyncCallback<String> {
                        override fun onFailure(caught: Throwable) {
                            // Show the RPC error message to the user
                            dialogBox.setText("Remote Procedure Call - Failure")
                            serverResponseLabel.addStyleName("serverResponseLabelError")
                            serverResponseLabel.html = SERVER_ERROR
                            dialogBox.center()
                            closeButton.setFocus(true)
                        }

                        override fun onSuccess(result: String) {
                            dialogBox.setText("Remote Procedure Call")
                            serverResponseLabel.removeStyleName("serverResponseLabelError")
                            serverResponseLabel.html = result
                            dialogBox.center()
                            closeButton.setFocus(true)
                        }
                    })
            }
        }

        // Add a handler to send the name to the server
        val handler = MyHandler()
        sendButton.addClickHandler(handler)
        nameField.addKeyUpHandler(handler)
    }

    companion object {
        /**
         * The message displayed to the user when the server cannot be reached or
         * returns an error.
         */
        private const val SERVER_ERROR = "An error occurred while " +
                "attempting to contact the server. Please check your network " +
                "connection and try again."
    }
}
