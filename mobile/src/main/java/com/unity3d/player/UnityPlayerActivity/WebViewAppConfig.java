package com.unity3d.player.UnityPlayerActivity;


public class WebViewAppConfig {
    // custom HTTP headers in addition to the ones sent by the web browser implementation
//    public static final String HTTP_HEADER = "googleweblight";

    // splashscreen time out
    public static final int SPLASH_SCREEN_TIME_OUT = 1000;

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
	public static final boolean PULL_TO_REFRESH = false;

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
