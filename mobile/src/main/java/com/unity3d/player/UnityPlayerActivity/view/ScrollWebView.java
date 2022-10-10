package com.unity3d.player.UnityPlayerActivity.view;

import android.content.Context;
import android.util.AttributeSet;

import com.unity3d.player.UnityPlayerActivity.plugins.VideoEnabledWebView;

public class ScrollWebView extends VideoEnabledWebView {
	private OnScrollListener mOnScrollListener = null;

	public interface OnScrollListener {
		void onScrollChanged(ScrollWebView scrollWebView, int x, int y, int oldx, int oldy);
	}

	public ScrollWebView(Context context) {
		super(context);
	}

	public ScrollWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollChanged(this, x, y, oldx, oldy);
		}
	}

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		mOnScrollListener = onScrollListener;
	}
}
