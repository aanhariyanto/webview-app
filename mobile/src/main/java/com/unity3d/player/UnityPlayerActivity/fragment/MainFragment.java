package com.unity3d.player.UnityPlayerActivity.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.unity3d.player.UnityPlayerActivity.WebViewAppConfig;
import com.unity3d.player.UnityPlayerActivity.listener.WebViewOnKeyListener;
import com.unity3d.player.UnityPlayerActivity.listener.WebViewOnTouchListener;
import com.unity3d.player.UnityPlayerActivity.utility.ContentUtility;
import com.unity3d.player.UnityPlayerActivity.utility.IntentUtility;
import com.unity3d.player.UnityPlayerActivity.utility.NetworkUtility;
import com.unity3d.player.UnityPlayerActivity.utility.PermissionUtility;
import com.unity3d.player.UnityPlayerActivity.view.StatefulLayout;
import com.unity3d.player.UnityPlayerActivity.listener.DrawerStateListener;
import com.unity3d.player.UnityPlayerActivity.listener.LoadUrlListener;
import com.unity3d.player.UnityPlayerActivity.utility.DownloadUtility;

import java.io.File;

import com.unity3d.player.UnityPlayerActivity.plugins.AdvancedWebView;
import com.unity3d.player.UnityPlayerActivity.plugins.VideoEnabledWebChromeClient;
import com.unity3d.player.UnityPlayerActivity.plugins.VideoEnabledWebView;

import com.unity3d.player.UnityPlayerActivity.view.ScrollWebView;
import com.unity3d.player.UnityPlayerActivity.listener.WebViewOnScrollListener;
import com.unity3d.player.UnityPlayerActivity.activity.SplashActivity;

public class MainFragment extends TaskFragment implements SwipeRefreshLayout.OnRefreshListener, AdvancedWebView.Listener {
	private static final String ARGUMENT_URL = "url";
	private static final String ARGUMENT_SHARE = "share";
	private static final int REQUEST_FILE_PICKER = 1;

	private boolean mProgress = false;
	private View mRootView;
	private StatefulLayout mStatefulLayout;
	private AdvancedWebView mWebView;
	private String mUrl = "about:blank";
	private String mShare;
	private boolean mLocal = false;
	private ValueCallback<Uri> mFilePathCallback4;
	private ValueCallback<Uri[]> mFilePathCallback5;
	private int mStoredActivityRequestCode;
	private int mStoredActivityResultCode;
	private Intent mStoredActivityResultIntent;


	public static MainFragment newInstance(String url, String share) {
		MainFragment fragment = new MainFragment();

		// arguments
		Bundle arguments = new Bundle();
		arguments.putString(ARGUMENT_URL, url);
		arguments.putString(ARGUMENT_SHARE, share);
		fragment.setArguments(arguments);

		return fragment;
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);

		// handle fragment arguments
		Bundle arguments = getArguments();
		if (arguments != null) {
			handleArguments(arguments);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(com.unity3d.player.UnityPlayerActivity.R.layout.fragment_main, container, false);
		mWebView = (AdvancedWebView) mRootView.findViewById(com.unity3d.player.UnityPlayerActivity.R.id.fragment_main_webview);
		return mRootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// restore webview state
		if (savedInstanceState != null) {
			mWebView.restoreState(savedInstanceState);
		}

		// setup webview
		bindData();

		// pull to refresh
		setupSwipeRefreshLayout();

		// setup stateful layout
		setupStatefulLayout(savedInstanceState);

		// load data
		if (mStatefulLayout.getState() == null) loadData();

		// progress popup
		showProgress(mProgress);

		// check permissions
		if (WebViewAppConfig.GEOLOCATION) {
			PermissionUtility.checkPermissionAccessLocation(this);
		}
	}


	@Override
	public void onStart() {
		super.onStart();
	}


	@Override
	public void onResume() {
		super.onResume();
		mWebView.onResume();

	}


	@Override
	public void onPause() {
		super.onPause();
		mWebView.onPause();
	}


	@Override
	public void onStop() {
		super.onStop();
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mRootView = null;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		mWebView.onDestroy();
	}


	@Override
	public void onDetach() {
		super.onDetach();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (PermissionUtility.checkPermissionReadExternalStorage(this)) {
			// permitted
			mWebView.onActivityResult(requestCode, resultCode, intent);
		} else {
			// not permitted
			mStoredActivityRequestCode = requestCode;
			mStoredActivityResultCode = resultCode;
			mStoredActivityResultIntent = intent;
		}
		//handleFilePickerActivityResult(requestCode, resultCode, intent); // not used, used advanced webview instead
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		// save current instance state
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);

		// stateful layout state
		if (mStatefulLayout != null) mStatefulLayout.saveInstanceState(outState);

		// save webview state
		mWebView.saveState(outState);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// action bar menu
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(com.unity3d.player.UnityPlayerActivity.R.menu.fragment_main, menu);

		// show or hide share button
		MenuItem share = menu.findItem(com.unity3d.player.UnityPlayerActivity.R.id.menu_fragment_main_share);
		share.setVisible(mShare != null && !mShare.trim().equals(""));
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// action bar menu behavior
		switch (item.getItemId()) {
			case com.unity3d.player.UnityPlayerActivity.R.id.menu_fragment_main_share:
				IntentUtility.startShareActivity(getContext(), getString(com.unity3d.player.UnityPlayerActivity.R.string.app_name), getShareText(mShare));
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case PermissionUtility.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
			case PermissionUtility.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:
			case PermissionUtility.REQUEST_PERMISSION_ACCESS_LOCATION:
                {
                    // if request is cancelled, the result arrays are empty
                    if (grantResults.length > 0) {
                        for (int i = 0; i < grantResults.length; i++) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                // permission granted
                                if (requestCode == PermissionUtility.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
                                    // continue with activity result handling
                                    if (mStoredActivityResultIntent != null) {
                                        mWebView.onActivityResult(mStoredActivityRequestCode, mStoredActivityResultCode, mStoredActivityResultIntent);
                                        mStoredActivityRequestCode = 0;
                                        mStoredActivityResultCode = 0;
                                        mStoredActivityResultIntent = null;
                                    }
                                }
                            } else {
                                // permission denied
                            }
                        }
                    } else {
                        // all permissions denied
                    }
                    break;
                }
		}
	}


	@Override
	public void onRefresh() {
		runTaskCallback(new Runnable()
            {
                @Override
                public void run() {
                    refreshData();
                }
            });
	}


	@Override
	public void onPageStarted(String url, Bitmap favicon) {

	}


	@Override
	public void onPageFinished(String url) {

	}


	@Override
	public void onPageError(int errorCode, String description, String failingUrl) {

	}


	@Override
	public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

	}


	@Override
	public void onExternalPageRequest(String url) {

	}


	public void refreshData() {
		if (NetworkUtility.isOnline(getActivity()) || mLocal) {
			// show progress popup
			showProgress(true);

			// load web url
			String url = mWebView.getUrl();
			if (url == null || url.equals("")) url = mUrl;
			mWebView.loadUrl(url);
		} else {
			showProgress(false);
			Toast.makeText(getActivity(), com.unity3d.player.UnityPlayerActivity.R.string.global_network_offline, Toast.LENGTH_LONG).show();
		}
	}


	private void handleArguments(Bundle arguments) {
		if (arguments.containsKey(ARGUMENT_URL)) {
			mUrl = arguments.getString(ARGUMENT_URL);
			mLocal = mUrl.contains("file://");
		}
		if (arguments.containsKey(ARGUMENT_SHARE)) {
			mShare = arguments.getString(ARGUMENT_SHARE);
		}
	}


	// not used, used advanced webview instead
	private void handleFilePickerActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_FILE_PICKER) {
			if (mFilePathCallback4 != null) {
				Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
				if (result != null) {
					String path = ContentUtility.getPath(getActivity(), result);
					Uri uri = Uri.fromFile(new File(path));
					mFilePathCallback4.onReceiveValue(uri);
				} else {
					mFilePathCallback4.onReceiveValue(null);
				}
			}

			if (mFilePathCallback5 != null) {
				Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
				if (result != null) {
					String path = ContentUtility.getPath(getActivity(), result);
					Uri uri = Uri.fromFile(new File(path));
					mFilePathCallback5.onReceiveValue(new Uri[]{uri});
				} else {
					mFilePathCallback5.onReceiveValue(null);
				}
			}

			mFilePathCallback4 = null;
			mFilePathCallback5 = null;
		}
	}


	private void loadData() {
		if (NetworkUtility.isOnline(getActivity()) || mLocal) {
			// show progress
			mStatefulLayout.showProgress();

			// load web url
			mWebView.loadUrl(mUrl);
		} else {
			mStatefulLayout.showOffline();
		}
	}


	private void showProgress(boolean visible) {
		// show pull to refresh progress bar
		SwipeRefreshLayout contentSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(com.unity3d.player.UnityPlayerActivity.R.id.container_content_swipeable);
		SwipeRefreshLayout offlineSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(com.unity3d.player.UnityPlayerActivity.R.id.container_offline_swipeable);
		SwipeRefreshLayout emptySwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(com.unity3d.player.UnityPlayerActivity.R.id.container_empty_swipeable);

		contentSwipeRefreshLayout.setRefreshing(visible);
		offlineSwipeRefreshLayout.setRefreshing(visible);
		emptySwipeRefreshLayout.setRefreshing(visible);

		boolean enabled;
		if (WebViewAppConfig.PULL_TO_REFRESH) enabled = !visible;
		else enabled = false;

		contentSwipeRefreshLayout.setEnabled(enabled);
		offlineSwipeRefreshLayout.setEnabled(enabled);
		emptySwipeRefreshLayout.setEnabled(enabled);

		mProgress = visible;
	}


	private void showContent(final long delay) {
		final Handler timerHandler = new Handler();
		final Runnable timerRunnable = new Runnable()
		{
			@Override
			public void run() {
				runTaskCallback(new Runnable()
                    {
                        public void run() {
                            if (getActivity() != null && mRootView != null) {
                                mStatefulLayout.showContent();
                            }
                        }
                    });
			}
		};
		timerHandler.postDelayed(timerRunnable, delay);
	}


	private void bindData() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }
		// webview settings
		mWebView.getSettings().setJavaScriptEnabled(true);
        if (WebViewAppConfig.CACHE_DISABLE) {
            mWebView.getSettings().setAppCacheEnabled(false);
            mWebView.getSettings().setAppCachePath(null);
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            mWebView.getSettings().setAppCacheEnabled(true);
            mWebView.getSettings().setAppCachePath(getActivity().getCacheDir().getAbsolutePath());
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }

		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setGeolocationEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(false);

        String customUserAgent = WebViewAppConfig.USER_AGENT;
        mWebView.getSettings().setUserAgentString(customUserAgent);
        if (!WebViewAppConfig.USER_AGENT.isEmpty()) {
            mWebView.getSettings().setUserAgentString(mWebView.getSettings().getUserAgentString());
        }

		// advanced webview settings
		mWebView.setListener(getActivity(), this);

		mWebView.setGeolocationEnabled(true);

        // disable third-party cookies only
        mWebView.setThirdPartyCookiesEnabled(true);

        // or disable cookies in general
        mWebView.setCookiesEnabled(true);

        //Allow or disallow (both passive and active) mixed content (HTTP content being loaded inside HTTPS sites)
        mWebView.setMixedContentAllowed(true);

		// webview style
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); // fixes scrollbar on Froyo

		// webview hardware acceleration
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		// webview chrome client
		View nonVideoLayout = getActivity().findViewById(com.unity3d.player.UnityPlayerActivity.R.id.activity_main_non_video_layout); //non video
		ViewGroup videoLayout = (ViewGroup) getActivity().findViewById(com.unity3d.player.UnityPlayerActivity.R.id.activity_main_video_layout); //video
		View progressView = getActivity().getLayoutInflater().inflate(com.unity3d.player.UnityPlayerActivity.R.layout.placeholder_progress, null); // progress
		VideoEnabledWebChromeClient webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, progressView, (VideoEnabledWebView) mWebView);
		webChromeClient.setOnToggledFullscreen(new MyToggledFullscreenCallback());
		mWebView.setWebChromeClient(webChromeClient);
		//mWebView.setWebChromeClient(new MyWebChromeClient()); // not used, used advanced webview instead

		// webview client
		mWebView.setWebViewClient(new MyWebViewClient());

		// webview key listener
		mWebView.setOnKeyListener(new WebViewOnKeyListener((DrawerStateListener) getActivity()));

		// webview touch listener
		mWebView.requestFocus(View.FOCUS_DOWN); // http://android24hours.blogspot.cz/2011/12/android-soft-keyboard-not-showing-on.html
		mWebView.setOnTouchListener(new WebViewOnTouchListener());

        // webview scroll listener
		((ScrollWebView) mWebView).setOnScrollListener(new WebViewOnScrollListener()); // not used
	}


	private void controlBack() {
		if (mWebView.canGoBack()) mWebView.goBack();
	}


	private void controlForward() {
		if (mWebView.canGoForward()) mWebView.goForward();
	}


	private void controlStop() {
		mWebView.stopLoading();
	}


	private void controlReload() {
		mWebView.reload();
	}


	private void setupStatefulLayout(Bundle savedInstanceState) {
		// reference
		mStatefulLayout = (StatefulLayout) mRootView;

		// state change listener
		mStatefulLayout.setOnStateChangeListener(new StatefulLayout.OnStateChangeListener()
            {
                @Override
                public void onStateChange(View v, StatefulLayout.State state) {

                }
            });

		// restore state
		mStatefulLayout.restoreInstanceState(savedInstanceState);
	}


	private void setupSwipeRefreshLayout() {
		SwipeRefreshLayout contentSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(com.unity3d.player.UnityPlayerActivity.R.id.container_content_swipeable);
		SwipeRefreshLayout offlineSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(com.unity3d.player.UnityPlayerActivity.R.id.container_offline_swipeable);
		SwipeRefreshLayout emptySwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(com.unity3d.player.UnityPlayerActivity.R.id.container_empty_swipeable);

		if (WebViewAppConfig.PULL_TO_REFRESH) {
			contentSwipeRefreshLayout.setOnRefreshListener(this);
			offlineSwipeRefreshLayout.setOnRefreshListener(this);
			emptySwipeRefreshLayout.setOnRefreshListener(this);
		} else {
			contentSwipeRefreshLayout.setEnabled(false);
			offlineSwipeRefreshLayout.setEnabled(false);
			emptySwipeRefreshLayout.setEnabled(false);
		}
	}


	private String getShareText(String text) {
		if (mWebView != null) {
			if (mWebView.getTitle() != null) {
				text = text.replaceAll("\\{TITLE\\}", mWebView.getTitle());
			}
			if (mWebView.getUrl() != null) {
				text = text.replaceAll("\\{URL\\}", mWebView.getUrl());
			}
		}
		return text;
	}


	private boolean isLinkExternal(String url) {
		for (String rule : WebViewAppConfig.LINKS_OPENED_IN_EXTERNAL_BROWSER) {
			if (url.contains(rule)) return true;
		}
		return false;
	}


	private boolean isLinkInternal(String url) {
		for (String rule : WebViewAppConfig.LINKS_OPENED_IN_INTERNAL_WEBVIEW) {
			if (url.contains(rule)) return true;
		}
		return false;
	}


	// not used, used advanced webview instead
	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
			if (PermissionUtility.checkPermissionReadExternalStorage(MainFragment.this)) {
				mFilePathCallback5 = filePathCallback;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
				return true;
			}
			return false;
		}


		@Override
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			callback.invoke(origin, true, false);
		}


		public void openFileChooser(ValueCallback<Uri> filePathCallback) {
			if (PermissionUtility.checkPermissionReadExternalStorage(MainFragment.this)) {
				mFilePathCallback4 = filePathCallback;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
			}
		}


		public void openFileChooser(ValueCallback filePathCallback, String acceptType) {
			if (PermissionUtility.checkPermissionReadExternalStorage(MainFragment.this)) {
				mFilePathCallback4 = filePathCallback;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
			}
		}


		public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
			if (PermissionUtility.checkPermissionReadExternalStorage(MainFragment.this)) {
				mFilePathCallback4 = filePathCallback;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
			}
		}
	}


	private class MyToggledFullscreenCallback implements VideoEnabledWebChromeClient.ToggledFullscreenCallback {
        private boolean fullscreen;

		@Override
		public void toggledFullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
            if (fullscreen) {
				WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
				attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                //
				getActivity().getWindow().setAttributes(attrs);
				if (android.os.Build.VERSION.SDK_INT >= 14) {
					getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
				}
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else {
				WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
				attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
				attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                //
				getActivity().getWindow().setAttributes(attrs);
				if (android.os.Build.VERSION.SDK_INT >= 14) {
					getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
				}
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
        public boolean isFullscreen() {
            return fullscreen;
        }
	}


	private class MyWebViewClient extends WebViewClient {
		private boolean mSuccess = true;


		@Override
		public void onPageFinished(final WebView view, final String url) {
			runTaskCallback(new Runnable()
                {
                    public void run() {
                        if (getActivity() != null && mSuccess) {
                            showContent(500); // hide progress bar with delay to show webview content smoothly
                            showProgress(false);

                            if (WebViewAppConfig.ACTION_BAR_HTML_TITLE) {
                                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(view.getTitle());
                            }
                        }
                    }
                });
		}


		@SuppressWarnings("deprecation")
		@Override
		public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl) {
			runTaskCallback(new Runnable()
                {
                    public void run() {
                        if (getActivity() != null) {
                            mSuccess = false;
                            mStatefulLayout.showEmpty();
                            showProgress(false);
                        }
                    }
                });
		}


		@TargetApi(Build.VERSION_CODES.M)
		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
			// forward to deprecated method
			onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
		}


		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (DownloadUtility.isDownloadableFile(url)) {
				if (PermissionUtility.checkPermissionWriteExternalStorage(MainFragment.this)) {
					Toast.makeText(getActivity(), com.unity3d.player.UnityPlayerActivity.R.string.fragment_main_downloading, Toast.LENGTH_LONG).show();
					DownloadUtility.downloadFile(getActivity(), url, DownloadUtility.getFileName(url));
					return true;
				}
				return true;
			} else if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
				// load url listener
				((LoadUrlListener) getActivity()).onLoadUrl(url);

				// determine for opening the link externally or internally
				boolean external = isLinkExternal(url);
				boolean internal = isLinkInternal(url);
				if (!external && !internal) {
					external = WebViewAppConfig.OPEN_LINKS_IN_EXTERNAL_BROWSER;
				}

				// open the link
				if (external) {
					IntentUtility.startWebActivity(getContext(), url);
					return true;
				} else {
					showProgress(true);
					return false;
				}
			} else {
				return IntentUtility.startIntentActivity(getContext(), url);
			}
		}
	}
}
