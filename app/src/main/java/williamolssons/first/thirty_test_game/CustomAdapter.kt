package williamolssons.first.thirty_test_game

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomAdapter(context: Context,  resource: Int,  objects: List<String>, private val usedItems: Set<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        setItemState(view, position)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        setItemState(view, position)
        return view
    }

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
