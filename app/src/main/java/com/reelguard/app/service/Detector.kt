package com.reelguard.app.service

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Looks for the rule's view IDs in the current window. It only asks "does a visible node
 * with this ID exist?" — it never reads text, content descriptions or password fields.
 */
object Detector {

    /** Returns the first matching feed view ID that is visible on screen, or null. */
    @Suppress("DEPRECATION")
    fun findFeedMatch(root: AccessibilityNodeInfo, rule: AppRule): String? {
        for (id in rule.feedViewIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id) ?: continue
            var visible = false
            for (node in nodes) {
                if (node.isVisibleToUser) visible = true
                node.recycle()
            }
            if (visible) return id
        }
        return null
    }

    /** Tries to click the app's Home tab. Returns true if a click was performed. */
    @Suppress("DEPRECATION")
    fun clickHomeTab(root: AccessibilityNodeInfo, rule: AppRule): Boolean {
        for (id in rule.homeTabViewIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id) ?: continue
            var clicked = false
            for (node in nodes) {
                if (!clicked) {
                    var target: AccessibilityNodeInfo? = node
                    // Walk up to the nearest clickable ancestor (the node itself is often a label).
                    var hops = 0
                    while (target != null && !target.isClickable && hops < 4) {
                        target = target.parent
                        hops++
                    }
                    if (target != null && target.isClickable) {
                        clicked = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
                node.recycle()
            }
            if (clicked) return true
        }
        return false
    }
}
