package myamamic.tp.devsupport.apps;

import myamamic.tp.devsupport.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppListAdapter extends BaseAdapter {
    private static final String TAG = "AppListAdapter";

    private final String KEY_PACKAGENAME = "key_packagename";
    private final String KEY_DESCRIPTION = "key_description";

    private Bitmap mFolderIcon;
    private LayoutInflater mInflater;
    private ArrayList<Map> mAppList = new ArrayList<Map>();

    public AppListAdapter(Context context) {
        mFolderIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
    }

    public void addItem(String packageName, String description) {
        Map map = new HashMap<String, String>();
        map.put(KEY_PACKAGENAME, packageName);
        map.put(KEY_DESCRIPTION, description);
        mAppList.add(map);
    }

    /**
     * The number of items in the list is determined by the number of speeches
     * in our array.
     *
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
        return mAppList.size();
    }

    /**
     * Since the data comes from an array, just returning the index is
     * sufficient to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
     *
     * @see android.widget.ListAdapter#getItem(int)
     */
    public Object getItem(int position) {
        return mAppList.get(position);
    }

    public String getItemAppName(int position) {
        Map map = mAppList.get(position);
        if (map == null) {
            return null;
        }
        return (String)map.get(KEY_PACKAGENAME);
    }

    public String getItemDescription(int position) {
        Map map = mAppList.get(position);
        if (map == null) {
            return null;
        }
        return (String)map.get(KEY_DESCRIPTION);
    }

    /**
     * Use the array index as a unique id.
     *
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Make a view to hold each row.
     *
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to re-inflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.app_list_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.icon = (ImageView)convertView.findViewById(R.id.app_image_icon);
            holder.name = (TextView)convertView.findViewById(R.id.app_name_text);
            holder.description = (TextView)convertView.findViewById(R.id.app_description_text);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        holder.icon.setImageBitmap(mFolderIcon);
        holder.name.setText(getItemAppName(position));
        holder.description.setText(getItemDescription(position));
        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView description;
    }
}