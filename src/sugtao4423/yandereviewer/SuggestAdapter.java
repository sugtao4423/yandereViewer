package sugtao4423.yandereviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SuggestAdapter extends ArrayAdapter<SearchItem>{

	private LayoutInflater mInflater;

	public SuggestAdapter(Context context){
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	class ViewHolder{
		ImageView icon;
		TextView text;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder;
		SearchItem item = getItem(position);
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.item_dialog_icontext, null);
			ImageView icon = (ImageView)convertView.findViewById(R.id.dialog_image);
			TextView text = (TextView)convertView.findViewById(R.id.dialog_text);

			holder = new ViewHolder();
			holder.icon = icon;
			holder.text = text;

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		if(item.getKind() == SearchItem.TAG)
			holder.icon.setImageResource(R.drawable.ic_menu_attachment);
		else if(item.getKind() == SearchItem.HISTORY)
			holder.icon.setImageResource(android.R.drawable.ic_menu_recent_history);
		holder.text.setText(item.getName());
		return convertView;
	}

}
