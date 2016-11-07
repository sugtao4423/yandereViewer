package com.tao.yandereviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MultiAutoCompleteTextView;

/**
 * sub class of {@link android.widget.AutoCompleteTextView} that includes a clear (dismiss / close) button with
 * a OnClearListener to handle the event of clicking the button
 * based on code from {@link http://www.gubed.net/clearableautocompletetextview}
 * @author Michael Derazon
 *
 */
public class ClearableMultiAutoCompleteTextView extends MultiAutoCompleteTextView{
	// was the text just cleared?
	boolean justCleared = false;

	// if not set otherwise, the default clear listener clears the text in the
	// text view
	private OnClearListener defaultClearListener = new OnClearListener(){

		@Override
		public void onClear(){
			ClearableMultiAutoCompleteTextView et = ClearableMultiAutoCompleteTextView.this;
			et.setText("");
		}
	};

	private OnClearListener onClearListener = defaultClearListener;

	// The image we defined for the clear button
	public Drawable imgClearButton = getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel);

	public interface OnClearListener{
		void onClear();
	}

	/* Required methods, not used in this implementation */
	public ClearableMultiAutoCompleteTextView(Context context){
		super(context);
		init();
	}

	/* Required methods, not used in this implementation */
	public ClearableMultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}

	/* Required methods, not used in this implementation */
	public ClearableMultiAutoCompleteTextView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	@SuppressLint("ClickableViewAccessibility")
	void init(){
		// Set the bounds of the button
		this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgClearButton, null);

		// if the clear button is pressed, fire up the handler. Otherwise do
		// nothing
		this.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event){

				ClearableMultiAutoCompleteTextView et = ClearableMultiAutoCompleteTextView.this;

				if(et.getCompoundDrawables()[2] == null)
					return false;

				if(event.getAction() != MotionEvent.ACTION_UP)
					return false;

				if(event.getX() > et.getWidth() - et.getPaddingRight() - imgClearButton.getIntrinsicWidth()){
					onClearListener.onClear();
					justCleared = true;
				}
				return false;
			}
		});
	}

	public void setImgClearButton(Drawable imgClearButton){
		this.imgClearButton = imgClearButton;
	}

	public void setOnClearListener(final OnClearListener clearListener){
		this.onClearListener = clearListener;
	}

	public void hideClearButton(){
		this.setCompoundDrawables(null, null, null, null);
	}

	public void showClearButton(){
		this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgClearButton, null);
	}
}