package com.samaritan.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.samaritan.TextActivity;

public class DynamicTextView extends TextView {
	
	public DynamicTextView(Context context) {
		super(context);
	}

	public DynamicTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DynamicTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		TextActivity.storage.mWidth = w;
	}
}
