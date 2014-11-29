package com.domen.tools;

import android.provider.BaseColumns;

public class TopicsContract {
	
	/* Inner class that defines the table contents */
    public static abstract class TopicsEntryContract implements BaseColumns {
    	
        public static final String TABLE_NAME = "topics";
        public static final String COLUMN_NAME_OF_ID = "ofid";
        public static final String COLUMN_NAME_TOPIC_NAME = "topicName";
        public static final String COLUMN_NAME_TOPIC_TYPE = "type";
        public static final String COLUMN_NAME_TOPIC_URL = "url";
        public static final String COLUMN_NAME_TOPIC_POSITIVE = "positive";
        public static final String COLUMN_NAME_TOPIC_NEGATIVE = "negative";
        public static final String COLUMN_NAME_TOPIC_DESC = "description";
        public static final String COLUMN_NAME_TOPIC_DATE = "date";
        
    }
}
