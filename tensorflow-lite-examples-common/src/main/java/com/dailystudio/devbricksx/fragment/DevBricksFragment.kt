package com.dailystudio.devbricksx.fragment

import androidx.fragment.app.Fragment

open class DevBricksFragment: Fragment() {

    open fun showChildFragment(fragmentId: Int) {
        showChildFragment(fragmentId, 0)
    }

    open fun showChildFragment(fragment: Fragment?) {
        showChildFragment(fragment, 0)
    }

    open fun showChildFragment(fragmentId: Int, enterAnim: Int) {
        showChildFragment(findChildFragment(fragmentId), enterAnim)
    }

    open fun showChildFragment(
        fragment: Fragment?,
        enterAnim: Int
    ) {
        showFragment(fragment, enterAnim, true)
    }

    open fun showFragment(fragmentId: Int) {
        showFragment(fragmentId, 0)
    }

    open fun showFragment(fragment: Fragment?) {
        showFragment(fragment, 0)
    }

    open fun showFragment(fragmentId: Int, enterAnim: Int) {
        showFragment(findFragment(fragmentId), enterAnim)
    }

    open fun showFragment(fragment: Fragment?, enterAnim: Int) {
        showFragment(fragment, enterAnim, false)
    }

    private fun showFragment(
        fragment: Fragment?,
        enterAnim: Int,
        isChild: Boolean) {
        if (fragment == null || fragment.isVisible) {
            return
        }
        val fm =
            if (isChild) childFragmentManager else parentFragmentManager
        val ft = fm.beginTransaction()
        if (enterAnim > 0) {
            ft.setCustomAnimations(enterAnim, 0)
        }
        ft.show(fragment)
        ft.commitAllowingStateLoss()
    }

    open fun hideChildFragment(fragmentId: Int) {
        hideChildFragment(fragmentId, 0)
    }

    open fun hideChildFragment(fragment: Fragment?) {
        hideChildFragment(fragment, 0)
    }

    open fun hideChildFragment(fragmentId: Int, enterAnim: Int) {
        hideChildFragment(findChildFragment(fragmentId), enterAnim)
    }

    open fun hideChildFragment(
        fragment: Fragment?,
        exitAnim: Int
    ) {
        hideFragment(fragment, exitAnim, true)
    }

    open fun hideFragment(fragmentId: Int) {
        hideFragment(fragmentId, 0)
    }

    open fun hideFragment(fragment: Fragment?) {
        hideFragment(fragment, 0)
    }

    open fun hideFragment(fragmentId: Int, enterAnim: Int) {
        hideFragment(findFragment(fragmentId), enterAnim)
    }

    open fun hideFragment(fragment: Fragment?, exitAnim: Int) {
        hideFragment(fragment, exitAnim, false)
    }

    private fun hideFragment(
        fragment: Fragment?,
        exitAnim: Int,
        isChild: Boolean
    ) {
        if (fragment == null || !fragment.isVisible) {
            return
        }
        val fm =
            if (isChild) childFragmentManager else parentFragmentManager
        val ft = fm.beginTransaction()
        if (exitAnim > 0) {
            ft.setCustomAnimations(0, exitAnim)
        }
        ft.hide(fragment)
        ft.commit()
    }

    open fun hideChildFragmentOnCreate(fragmentId: Int) {
        hideFragmentOnCreate(findChildFragment(fragmentId))
    }

    open fun hideChildFragmentOnCreate(fragment: Fragment?) {
        hideFragmentOnCreate(fragment, true)
    }

    open fun hideFragmentOnCreate(fragmentId: Int) {
        hideFragmentOnCreate(findFragment(fragmentId))
    }

    open fun hideFragmentOnCreate(fragment: Fragment?) {
        hideFragmentOnCreate(fragment, false)
    }

    open fun hideFragmentOnCreate(
        fragment: Fragment?,
        isChild: Boolean
    ) {
        if (fragment == null) {
            return
        }
        val fm =
            if (isChild) childFragmentManager else parentFragmentManager
        val ft = fm.beginTransaction()
        ft.hide(fragment)
        ft.commit()
    }

    open fun isFragmentVisible(fragmentId: Int): Boolean {
        return isFragmentVisible(findFragment(fragmentId))
    }

    open fun isChildFragmentVisible(fragmentId: Int): Boolean {
        return isFragmentVisible(findChildFragment(fragmentId))
    }

    open fun isFragmentVisible(fragment: Fragment?): Boolean {
        return fragment?.isVisible ?: false
    }

    open fun findChildFragment(fragmentId: Int): Fragment? {
        val frgmgr = childFragmentManager ?: return null
        return frgmgr.findFragmentById(fragmentId)
    }

    open fun findFragment(fragmentId: Int): Fragment? {
        val frgmgr = parentFragmentManager
        return frgmgr.findFragmentById(fragmentId)
    }

}