package com.angelstone.android.smsblocker.store;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class EventLog {
	public static final int LOG_TYPE_CALL = 0x00000001;
	public static final int LOG_TYPE_SMS = 0x00000002;
	public static final int LOG_SCOPE_INTERCEPTED = 0x00000001;
	public static final int LOG_SCOPE_RECEIVED = 0x00000002;
	public static final int LOG_SCOPE_ALL = 0x00000003;
	public static final int CALL_LOG_BLOCK_TYPE_BL = 0x00000001;
	public static final int CALL_LOG_BLOCK_TYPE_ADV = 0x00000002;
	public static final int SMS_LOG_BLOCK_TYPE_BL = 0x00000003;
	public static final int SMS_LOG_BLOCK_TYPE_FILTER = 0x00000004;

	public int compareTo(Object cmp_obj) {
		EventLog cmp = (EventLog) cmp_obj;
		return m_Time.compareTo(cmp.m_Time);
	}

	public EventLog(String Number, int Type) {
		m_Time = new Date();
		Locale locale = Locale.getDefault();

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", locale);
		String dateStr = df.format(m_Time);

		Log.d("scfw", "EventLog:" + dateStr);
		Log.d("scfw", "EventLog:" + String.format("%d", m_Time.getTime()));

		m_Number = Number;
		m_Type = Type;
		m_CallType = -1;
		m_SmsTxt = null;
		m_Tag_or_Name = null;
		m_Scene_or_Keyword = null;
	}

	public EventLog(String Number, int Type, int CallType, long Time) {
		m_Time = new Date();
		m_Time.setTime(Time);
		m_Number = Number;
		m_Type = Type;
		m_CallType = CallType;
		m_SmsTxt = null;
		m_Tag_or_Name = null;
		m_Scene_or_Keyword = null;
		m_ReplySmsTxt = null;
	}

	public Date getTime() {
		return m_Time;
	}

	public String getNumber() {
		return m_Number;
	}

	public int getType() {
		return m_Type;
	}

	public int getCallType() {
		return m_CallType;
	}

	public String getSmsTxt() {
		if (m_Type == LOG_TYPE_SMS)
			return m_SmsTxt;
		else
			return null;
	}

	public boolean setSmsTxt(String SmsText) {
		if (m_Type == LOG_TYPE_SMS) {
			m_SmsTxt = SmsText;
			return true;
		} else {
			return false;
		}
	}

	public String getTagOrName() {
		return m_Tag_or_Name;
	}

	public void setTagOrName(String Tag_or_Name) {
		m_Tag_or_Name = Tag_or_Name;
	}

	public String getSceneOrKeyword() {
		return m_Scene_or_Keyword;
	}

	public void setSceneOrKeyword(String Scene_or_Keyword) {
		m_Scene_or_Keyword = Scene_or_Keyword;
	}

	public boolean isBlockedByBlacklist() {
		return m_Tag_or_Name != null && m_Scene_or_Keyword == null;
	}

	public boolean isBlockedByScene() {
		return m_Type == LOG_TYPE_CALL && m_Scene_or_Keyword != null;
	}

	public boolean sms_isBlockedByKeyword() {
		return m_Type == LOG_TYPE_SMS && m_Scene_or_Keyword != null;
	}

	public void setBlockType(int block_type) {
		m_BlockType = block_type;
	}

	public int getBlockType() {
		return m_BlockType;
	}

	public void setReplySmsTxt(String replySmsTxt) {
		m_ReplySmsTxt = replySmsTxt;
	}

	public String getReplySmsTxt() {
		return m_ReplySmsTxt;
	}

	private Date m_Time;
	private String m_Number;
	private int m_Type; // one of LOG_TYPE_XXX constants
	private int m_CallType; // applicable only when m_Type == LOG_TYPE_CALL;
	private String m_SmsTxt; // content of the short message. applied only when
														// m_Type == LOG_TYPE_SMS, otherwise set to 'null'

	private String m_Tag_or_Name;
	private String m_Scene_or_Keyword;
	private int m_BlockType;

	private String m_ReplySmsTxt;
}
