package androidx.preference

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView

@Suppress("unused")
class ExtMultiSelectListPreference : MultiSelectListPreference {
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)

    var maxLines: Int = 10
    var ellipsize: TextUtils.TruncateAt? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val summaryView = holder.findViewById(android.R.id.summary) as TextView
        summaryView.maxLines = maxLines
        summaryView.ellipsize = ellipsize
    }
}
