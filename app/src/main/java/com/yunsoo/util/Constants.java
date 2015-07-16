package com.yunsoo.util;

public final class Constants {

	
	public final static class App
	{
		public final static String APP_VERSION = "1.0.0";
		
	}
	
	public final static class Cache {
		public final static String CACHE_SDCARD_IMAGE_PATH = "/image";
		public final static String CACHE_SDCARD_DATA_PATH = "/data";
		public final static String CACHE_SDCARD_DOWNLOAD_PATH = "/download";
	}

	public final static class Preference {
		public final static String PREF_LOGIN = "login";

        public final static String PREF_FILE="file_in";
        public final static String PACK_FILE_LAST_INDEX="pack_file_index";
		// json String
		public final static String KEY_USER_INFO = "user_info";
		
		public final static String PREF_ENTERPRISE_LOGIN = "enterprise_login";
		
		public final static String KEY_ENTERPRISE_ACCOUNT_INFO = "account_info";
		
		public static final String PREF_LAST_KNOWN_LOCATION = "location";
		public static final String KEY_LAST_LOCATION = "location_info";
		
		public final static String PREF_DEVICE_INFO = "device_in";
		public static final String KEY_DEVICE_INFO = "device_info";
		
		public final static String PREF_FOLLOWING = "manufacturers";
		public final static String KEY_FOLLOWING_DATA = "manufacturer_data";
		public final static String PREF_ORG = "org";
		public final static String KEY_LAST_MESSAGE_ID = "manufacturer_data";
	}

	public final static class Request {

		public static final String LastModified = "Last-Modified";		
	}
	// we need / slash 
	public static final String SERVER_URL = "http://api.test.yunsu.co:6080";
	public static final String USER_AGREEMENT_URL = "http://t.m.yunsu.co/usercontract";

	public static final String EmptyString = "";
	public static final String QuestionMark = "?";
	public static final String NA = "N/A";
	
	public static final String CODE_BASE_URL = "http://t.m.yunsu.co/";

    public static final String SQ_DATABASE="yunsoo_pda";

    public static final String YUNSOO_FOLDERNAME = "/yunsoo";

    public static final String PACK_SYNC_TASK_FOLDER = "/pack/sync_task";

}
