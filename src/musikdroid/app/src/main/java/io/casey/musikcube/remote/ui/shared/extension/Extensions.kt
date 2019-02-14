package io.casey.musikcube.remote.ui.shared.extension

import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import io.casey.musikcube.remote.Application
import io.casey.musikcube.remote.R
import io.casey.musikcube.remote.ui.settings.constants.Prefs
import io.casey.musikcube.remote.ui.shared.activity.IFilterable
import io.casey.musikcube.remote.ui.shared.activity.IMenuProvider
import io.casey.musikcube.remote.ui.shared.constant.Shared
import io.casey.musikcube.remote.ui.shared.fragment.BaseFragment
import io.casey.musikcube.remote.ui.shared.fragment.TransportFragment
import io.casey.musikcube.remote.util.Strings

/*
 *
 * SharedPreferences
 *
 */

fun SharedPreferences.getString(key: String): String? =
    when (!this.contains(key)) {
        true -> null
        else -> this.getString(key, "")
    }

/*
 *
 * Toolbar
 *
 */

fun Toolbar.initSearchMenu(activity: AppCompatActivity, filterable: IFilterable?): Boolean =
        activity.initSearchMenu(this.menu, filterable)

fun Toolbar.setTitleFromIntent(defaultTitle: String) {
    val extras = (context as? AppCompatActivity)?.intent?.extras ?: Bundle()
    val title = extras.getString(Shared.Extra.TITLE_OVERRIDE)
    this.title = if (Strings.notEmpty(title)) title else defaultTitle
}

/*
 *
 * AppCompatActivity
 *
 */

fun AppCompatActivity.setupDefaultRecyclerView(
        recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>)
{
    val layoutManager = LinearLayoutManager(this)
    val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
    recyclerView.layoutManager = LinearLayoutManager(this)
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(dividerItemDecoration)
}


val AppCompatActivity.toolbar: Toolbar?
    get() = findViewById(R.id.toolbar)

fun AppCompatActivity.enableUpNavigation() {
    val ab = this.supportActionBar
    ab?.setDisplayHomeAsUpEnabled(true)
}

fun AppCompatActivity.addTransportFragment(
        listener: ((TransportFragment) -> Unit)? = null): TransportFragment
{
    val root = findViewById<View>(android.R.id.content)
    if (root != null) {
        if (root.findViewById<View>(R.id.transport_container) != null) {
            val fragment = TransportFragment.create()

            this.supportFragmentManager
                    .beginTransaction()
                    .add(R.id.transport_container, fragment, TransportFragment.TAG)
                    .commit()

            fragment.modelChangedListener = listener
            return fragment
        }
    }
    throw IllegalArgumentException("could not find content view")
}

fun AppCompatActivity.setTitleFromIntent(defaultId: Int) =
        this.setTitleFromIntent(getString(defaultId))

fun AppCompatActivity.setTitleFromIntent(defaultTitle: String) {
    val title = this.intent.getStringExtra(Shared.Extra.TITLE_OVERRIDE)
    this.title = if (Strings.notEmpty(title)) title else defaultTitle
}

fun AppCompatActivity.initSearchMenu(menu: Menu, filterable: IFilterable?): Boolean {
    this.menuInflater.inflate(R.menu.search_menu, menu)

    val searchMenuItem = menu.findItem(R.id.action_search)
    val searchView = MenuItemCompat.getActionView(searchMenuItem) as SearchView

    searchView.maxWidth = Integer.MAX_VALUE

    if (filterable != null) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = false

            override fun onQueryTextChange(newText: String): Boolean {
                filterable.setFilter(newText)
                return false
            }
        })

        searchView.setOnCloseListener {
            filterable.setFilter("")
            false
        }
    }

    val searchManager = this.getSystemService(Context.SEARCH_SERVICE) as SearchManager
    val searchableInfo = searchManager.getSearchableInfo(this.componentName)

    searchView.setSearchableInfo(searchableInfo)
    searchView.setIconifiedByDefault(true)

    return true
}

fun AppCompatActivity.dpToPx(dp: Float): Float = dp * this.resources.displayMetrics.density


fun AppCompatActivity.dialogVisible(tag: String): Boolean =
        this.supportFragmentManager.findFragmentByTag(tag) != null

fun AppCompatActivity.showDialog(dialog: DialogFragment, tag: String) {
    dialog.show(this.supportFragmentManager, tag)
}

fun AppCompatActivity.slideNextUp() = overridePendingTransition(R.anim.slide_up, R.anim.stay_put)

fun AppCompatActivity.slideThisDown() = overridePendingTransition(R.anim.stay_put, R.anim.slide_down)

fun AppCompatActivity.slideNextLeft() = overridePendingTransition(R.anim.slide_left, R.anim.slide_left_bg)

fun AppCompatActivity.slideThisRight() = overridePendingTransition(R.anim.slide_right_bg, R.anim.slide_right)

inline fun <reified T> AppCompatActivity.findFragment(tag: String): T {
    return this.supportFragmentManager.find(tag)
}

/*
 *
 * Bundle
 *
 */

fun Bundle.withElevation(fm: FragmentManager): Bundle {
    this.putFloat(Shared.Extra.ELEVATION, (fm.backStackEntryCount + 1) * 16.0f)
    return this
}

val Bundle.elevation: Float
    get() = this.getFloat(Shared.Extra.ELEVATION, 0.0f)

/*
 *
 * BaseFragment
 *
 */

val BaseFragment.pushContainerId: Int
    get() = this.extras.getInt(Shared.Extra.PUSH_CONTAINER_ID, -1)

inline fun <reified T: BaseFragment> T.pushTo(containerId: Int): T {
    if (containerId > 0) {
        this.extras.putInt(Shared.Extra.PUSH_CONTAINER_ID, containerId)
    }
    return this
}

inline fun <reified T: BaseFragment> T.pushTo(other: BaseFragment): T {
    this.pushTo(other.pushContainerId)
    return this
}

fun BaseFragment.pushWithToolbar(containerId: Int, backstackId: String, fragment: BaseFragment) {
    appCompatActivity.supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(
            R.anim.slide_left, R.anim.slide_left_bg,
            R.anim.slide_right_bg, R.anim.slide_right)
        .replace(
            containerId,
            fragment
                .withToolbar()
                .addElevation(appCompatActivity.supportFragmentManager))
        .addToBackStack(backstackId)
        .commit()
}

inline fun <reified T: BaseFragment> T.withToolbar(): T {
    this.arguments?.putBoolean(Shared.Extra.WITH_TOOLBAR, true)
    return this
}

inline fun <reified T: BaseFragment> T.withTitleOverride(activity: AppCompatActivity): T {
    activity.intent?.getStringExtra(Shared.Extra.TITLE_OVERRIDE)?.let {
        if (it.isNotEmpty()) {
            this.extras.putString(Shared.Extra.TITLE_OVERRIDE, it)
        }
    }
    return this
}

fun BaseFragment.initSearchMenu(menu: Menu, filterable: IFilterable?): Boolean =
    (activity as AppCompatActivity).initSearchMenu(menu, filterable)

fun BaseFragment.initToolbarIfNecessary(activity: AppCompatActivity, view: View, searchMenu: Boolean = true) {
    view.findViewById<Toolbar>(R.id.toolbar)?.let {
        it.navigationIcon = appCompatActivity.getDrawable(R.drawable.ic_back)
        it.setNavigationOnClickListener {
            appCompatActivity.onBackPressed()
        }
        if (searchMenu) {
            it.initSearchMenu(activity, this as? IFilterable)
        }
        if (this is IMenuProvider) {
            this.createOptionsMenu(it.menu)
            it.setOnMenuItemClickListener {
                menuItem -> this.optionsItemSelected(menuItem)
            }
        }
    }
}

fun BaseFragment.getLayoutId(): Int =
    when (this.extras.getBoolean(Shared.Extra.WITH_TOOLBAR)) {
        true -> R.layout.recycler_view_with_empty_state_and_toolbar_and_fab
        else -> R.layout.recycler_view_with_empty_state
    }

inline fun <reified T: BaseFragment> T.addElevation(fm: FragmentManager): T {
    this.extras.withElevation(fm)
    return this
}

fun BaseFragment.setupDefaultRecyclerView(
        recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>)
{
    this.appCompatActivity.setupDefaultRecyclerView(recyclerView, adapter)
}

fun BaseFragment.getTitleOverride(defaultId: Int): String =
        this.getTitleOverride(getString(defaultId))

fun BaseFragment.getTitleOverride(defaultTitle: String): String {
    val title = this.extras.getString(Shared.Extra.TITLE_OVERRIDE) ?: ""
    return if (Strings.notEmpty(title)) title else defaultTitle
}

/*
 *
 * Snackbar
 *
 */

fun showSnackbar(view: View, text: String, bgColor: Int, fgColor: Int, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) {
    val sb = Snackbar.make(view, text, Snackbar.LENGTH_LONG)

    if (buttonText != null && buttonCb != null) {
        sb.setAction(buttonText, buttonCb)
    }

    val sbView = sb.view
    val context = view.context
    sbView.setBackgroundColor(ContextCompat.getColor(context, bgColor))
    val tv = sbView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
    tv.setTextColor(ContextCompat.getColor(context, fgColor))
    sb.show()
}

fun showSnackbar(view: View, stringId: Int, bgColor: Int, fgColor: Int, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) =
    showSnackbar(view, Application.instance.getString(stringId), bgColor, fgColor, buttonText, buttonCb)

fun showSnackbar(view: View, text: String, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) =
    showSnackbar(view, text, R.color.color_primary, R.color.theme_foreground, buttonText, buttonCb)

fun showSnackbar(view: View, stringId: Int, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) =
    showSnackbar(view, Application.instance.getString(stringId), buttonText, buttonCb)

fun showErrorSnackbar(view: View, text: String, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) =
    showSnackbar(view, text, R.color.theme_red, R.color.theme_foreground, buttonText, buttonCb)

fun showErrorSnackbar(view: View, stringId: Int, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) =
    showErrorSnackbar(view, Application.instance.getString(stringId), buttonText, buttonCb)

fun AppCompatActivity.showErrorSnackbar(stringId: Int, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) =
    showErrorSnackbar(this.findViewById<View>(android.R.id.content), stringId, buttonText, buttonCb)

fun AppCompatActivity.showSnackbar(stringId: Int, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) =
    showSnackbar(this.findViewById<View>(android.R.id.content), stringId, buttonText, buttonCb)

fun AppCompatActivity.showSnackbar(stringId: String, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) =
    showSnackbar(this.findViewById<View>(android.R.id.content), stringId, buttonText, buttonCb)

fun AppCompatActivity.showSnackbar(viewId: Int, stringId: Int, buttonText: String? = null, buttonCb: ((View) -> Unit)? = null) =
    showSnackbar(this.findViewById<View>(viewId), stringId, buttonText, buttonCb)

/*
 *
 * View 1-offs
 *
 */

fun CheckBox.setCheckWithoutEvent(checked: Boolean,
                                  listener: (CompoundButton, Boolean) -> Unit) {
    this.setOnCheckedChangeListener(null)
    this.isChecked = checked
    this.setOnCheckedChangeListener(listener)
}

fun EditText.setTextAndMoveCursorToEnd(text: String) {
    this.setText(text)
    this.setSelection(this.text.length)
}

fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

inline fun <reified T> FragmentManager.find(tag: String): T {
    return findFragmentByTag(tag) as T
}

/*
 *
 * Colors
 *
 */

fun RecyclerView.ViewHolder.getColorCompat(resourceId: Int): Int =
    ContextCompat.getColor(itemView.context, resourceId)

fun View.getColorCompat(resourceId: Int): Int =
    ContextCompat.getColor(context, resourceId)

fun Fragment.getColorCompat(resourceId: Int): Int =
    ContextCompat.getColor(activity!!, resourceId)

fun AppCompatActivity.getColorCompat(resourceId: Int): Int =
    ContextCompat.getColor(this, resourceId)

/*
 *
 * Keyboard
 *
 */

fun showKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun hideKeyboard(context: Context, view: View) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun AppCompatActivity.showKeyboard() = showKeyboard(this)

fun AppCompatActivity.hideKeyboard(view: View? = null) {
    val v = view ?: this.findViewById(android.R.id.content)
    hideKeyboard(this, v)
}

fun DialogFragment.showKeyboard() =
    showKeyboard(activity!!)

fun DialogFragment.hideKeyboard() =
    hideKeyboard(activity!!, activity!!.findViewById(android.R.id.content))

/*
 *
 * misc
 *
 */

fun <T1: Any, T2: Any, R: Any> letMany(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun <T1: Any, T2: Any, T3: Any, R: Any> letMany(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

fun <T1: Any, T2: Any, T3: Any, T4: Any, R: Any> letMany(p1: T1?, p2: T2?, p3: T3?, p4: T4?, block: (T1, T2, T3, T4) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
}
fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, R: Any> letMany(p1: T1?, p2: T2?, p3: T3?, p4: T4?, p5: T5?, block: (T1, T2, T3, T4, T5) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(p1, p2, p3, p4, p5) else null
}

fun titleEllipsizeMode(prefs: SharedPreferences): TextUtils.TruncateAt {
    val modeIndex = prefs.getInt(
        Prefs.Key.TITLE_ELLIPSIS_MODE_INDEX,
        Prefs.Default.TITLE_ELLIPSIS_SIZE_INDEX)

    return when(modeIndex) {
        0 -> TextUtils.TruncateAt.START
        1 -> TextUtils.TruncateAt.MIDDLE
        else -> TextUtils.TruncateAt.END
    }
}

fun fallback(input: String?, fallback: String): String =
    if (input.isNullOrEmpty()) fallback else input

fun fallback(input: String?, fallback: Int): String =
    if (input.isNullOrEmpty()) Application.instance.getString(fallback) else input
