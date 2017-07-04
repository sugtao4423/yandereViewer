package sugtao4423.icondialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import sugtao4423.yandereviewer.R;

public class IconDialog{

	private Builder builder;
	private Context context;

	public IconDialog(Context context){
		builder = new Builder(context);
		this.context = context;
	}

	public Builder setTitle(String title){
		return builder.setTitle(title);
	}

	public Builder setItems(IconItem[] items, OnClickListener listener){
		IconDialogAdapter adapter = new IconDialogAdapter(context, items);
		return builder.setAdapter(adapter, listener);
	}

	public AlertDialog show(){
		return builder.show();
	}
}

class IconDialogAdapter extends ArrayAdapter<IconItem>{

	private LayoutInflater mInflater;

	public IconDialogAdapter(Context context, IconItem[] items){
		super(context, android.R.layout.select_dialog_item, android.R.id.text1, items);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	class ViewHolder{
		ImageView image;
		TextView text;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder;
		IconItem item = getItem(position);
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.item_dialog_icontext, null);
			holder = new ViewHolder();
			holder.image = (ImageView)convertView.findViewById(R.id.dialog_image);
			holder.text = (TextView)convertView.findViewById(R.id.dialog_text);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.image.setImageResource(item.getResource());
		holder.text.setText(item.getTitle());
		return convertView;
	}
}