package com.unity3d.player.UnityPlayerActivity;


public class WebViewAppConfig {

    //Set to "true" to prevent the device from going into sleep while the app is active
    public static final boolean PREVENT_SLEEP = true;

    // splashscreen time out
    public static final int SPLASH_SCREEN_TIME_OUT = 1000;

    // Set to "true" to clear the WebView cache on each app startup and do not use cached versions of your web app/website
    public static final boolean CACHE_DISABLE = true;

    // Set a customized UserAgent for WebView URL requests (or leave it empty to use the default Android UserAgent)
    public static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 15_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/100.0.4896.77 Mobile/15E148 Safari/604.1";

	// true for showing action bar
	public static final boolean ACTION_BAR = false;

	// true for showing html title rather than navigation title in the action bar
	public static final boolean ACTION_BAR_HTML_TITLE = false;

	// true for enabling navigation drawer menu
	public static final boolean NAVIGATION_DRAWER = false;

	// true for enabling background image in the header of the navigation drawer menu,
	// otherwise accent color will be used,
	// background image is stored in navigation_header_bg.png
	public static final boolean NAVIGATION_DRAWER_HEADER_IMAGE = false;

	// true for enabling icon tint in the navigation drawer menu,
	// note that only transparent PNG icons can be tinted,
	// tint color is defined in @color/navigation_icon_tint
	public static final boolean NAVIGATION_DRAWER_ICON_TINT = false;

	// true for enabling pull-to-refresh gesture
	public static final boolean PULL_TO_REFRESH = true;

	// true for enabling exit confirmation when back button is pressed
	public static final boolean EXIT_CONFIRMATION = true;

	// true for enabling geolocation
	public static final boolean GEOLOCATION = true;

	// frequency of showing rate my app prompt,
	// prompt will be shown after each x launches of the app,
	// set 0 if you do not want to show rate my app prompt
	public static final int RATE_APP_PROMPT_FREQUENCY = 0;

	// duration of showing rate my app prompt in milliseconds
	public static final int RATE_APP_PROMPT_DURATION = 10000;

	// true for opening webview links in external web browser rather than directly in the webview
	public static final boolean OPEN_LINKS_IN_EXTERNAL_BROWSER = false;

	// rules for opening links in external browser,
	// if URL link contains the string, it will be opened in external browser,
	// these rules have higher priority than OPEN_LINKS_IN_EXTERNAL_BROWSER option
	public static final String[] LINKS_OPENED_IN_EXTERNAL_BROWSER = {
        "target=blank",
        "target=external",
        "play.google.com/store",
        "youtube.com/watch"
	};

	// rules for opening links in internal webview,
	// if URL link contains the string, it will be loaded in internal webview,
	// these rules have higher priority than OPEN_LINKS_IN_EXTERNAL_BROWSER option
	public static final String[] LINKS_OPENED_IN_INTERNAL_WEBVIEW = {
        "target=webview",
        "target=internal"
	};

	// list of file extensions or expressions for download,
	// if webview URL matches with this regular expression, the file will be downloaded via download manager,
	// leave this array empty if you do not want to use download manager
	public static final String[] DOWNLOAD_FILE_TYPES = {
        ".*zip$", ".*rar$", ".*pdf$", ".*doc$", ".*xls$",
        ".*mp3$", ".*wma$", ".*ogg$", ".*m4a$", ".*wav$",
        ".*avi$", ".*mov$", ".*mp4$", ".*mpg$", ".*3gp$",
        ".*drive.google.com.*download.*",
        ".*dropbox.com/s/.*"
	};
}
