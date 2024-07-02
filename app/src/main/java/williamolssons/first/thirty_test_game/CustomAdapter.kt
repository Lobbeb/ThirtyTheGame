package williamolssons.first.thirty_test_game

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * Custom adapter for displaying a list of items with certain items marked as used.
 * @param context The current context.
 * @param resource The resource ID for a layout file containing a TextView to use when instantiating views.
 * @param objects The objects to represent in the ListView.
 * @param usedItems A set of items that are marked as used and should be displayed in a different color.
 */
class CustomAdapter(context: Context, resource: Int, objects: List<String>, private val usedItems: Set<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    /**
     * Returns a view for the specified position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        setItemState(view, position)
        return view
    }

    /**
     * Returns a view for the specified position in the drop-down menu.
     * @param position The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position in the drop-down menu.
     */
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        setItemState(view, position)
        return view
    }

    /**
     * Sets the state of the item view based on whether it is marked as used.
     * @param view The view to update.
     * @param position The position of the item within the adapter's data set.
     */
    private fun setItemState(view: View, position: Int) {
        val textView = view as TextView
        if (usedItems.contains(getItem(position))) {
            textView.setTextColor(context.resources.getColor(android.R.color.darker_gray, null))
            textView.isEnabled = false
        } else {
            textView.setTextColor(context.resources.getColor(android.R.color.black, null))
            textView.isEnabled = true
        }
    }
}
