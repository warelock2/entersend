package com.example.entersend

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.KeyEvent

class EnterSendAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "EnterSend"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (packageName != "org.thoughtcrime.securesms" && packageName != "com.android.messaging") return

        Log.d(TAG, "Event type: ${event.eventType}, pkg: $packageName, text: ${event.text}")

        // Software keyboards often do not dispatch onKeyEvent to accessibility
        // services. Instead, pressing Enter inserts a newline into the text field,
        // which fires a TYPE_VIEW_TEXT_CHANGED event containing "\n".
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val newText = event.text.joinToString("")
            if (newText.contains("\n")) {
                Log.d(TAG, "Enter detected via text change: $newText")
                rootInActiveWindow?.let { root ->
                    if (findAndClickSendButton(root)) {
                        Log.d(TAG, "Successfully clicked send button on enter.")
                    } else {
                        Log.d(TAG, "Failed to click send button.")
                    }
                    root.recycle()
                }
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        Log.d(TAG, "KeyEvent: keyCode=${event.keyCode}, action=${event.action}")
        if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
            rootInActiveWindow?.let { root ->
                dumpNode(root)
                val clicked = findAndClickSendButton(root)
                root.recycle()
                if (clicked) {
                    Log.d(TAG, "Successfully clicked send button on key event.")
                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun dumpNode(node: AccessibilityNodeInfo, indent: String = "") {
        val viewId = node.viewIdResourceName ?: "null"
        val contentDesc = node.contentDescription ?: "null"
        val text = node.text ?: "null"
        val className = node.className ?: "null"
        Log.d(TAG, "$indent node: class=$className, id=$viewId, desc=$contentDesc, text=$text, clickable=${node.isClickable}")
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            dumpNode(child, "$indent  ")
            child.recycle()
        }
    }

    private fun findAndClickSendButton(node: AccessibilityNodeInfo): Boolean {
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        val text = node.text?.toString()?.lowercase() ?: ""

        val isSendButtonCandidate = (contentDesc.contains("send") || 
                                    viewId.contains("send") || 
                                    viewId.contains("button") ||
                                    text.contains("send")) && 
                                    !node.isEditable

        if (isSendButtonCandidate) {
            Log.d(TAG, "Found potential send button: viewId=$viewId, contentDesc=$contentDesc, text=$text, clickable=${node.isClickable}")

            // Click the matched node directly if it is clickable
            if (node.isClickable) {
                val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (success) {
                    Log.d(TAG, "ACTION_CLICK performed successfully on send button!")
                    return true
                }
            }

            // Walk up to nearest clickable ancestor.
            // Jetpack Compose often renders the label ("Send Message") on a non-clickable
            // leaf node while the real touch target is a clickable parent container.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                var ancestor: AccessibilityNodeInfo? = node.parent
                while (ancestor != null) {
                    if (ancestor.isClickable) {
                        Log.d(TAG, "Found clickable ancestor: viewId=${ancestor.viewIdResourceName}, desc=${ancestor.contentDescription}")
                        val success = ancestor.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        ancestor.recycle()
                        if (success) {
                            Log.d(TAG, "ACTION_CLICK performed successfully on clickable ancestor!")
                            return true
                        }
                        break
                    }
                    val nextAncestor = ancestor.parent
                    ancestor.recycle()
                    ancestor = nextAncestor
                }
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClickSendButton(child)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
}
