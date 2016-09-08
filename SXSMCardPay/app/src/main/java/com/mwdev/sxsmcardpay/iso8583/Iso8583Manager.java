package com.mwdev.sxsmcardpay.iso8583;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.basewin.packet8583.exception.Packet8583Exception;
import com.basewin.packet8583.factory.Xml8583Config;
import com.basewin.packet8583.model.IsoPackage;
import com.basewin.utils.BCDHelper;
import com.basewin.utils.EncodeUtil;

public class Iso8583Manager {
	private final String MSGID = "msgid";
	private IsoPackage iosPackage = null;
	private Map<String, String> requestMap = null;
	private Context context;

	public Iso8583Manager(Context context) {
		this.context = context;
		Xml8583Config myXmlReader = new Xml8583Config(context);
		this.iosPackage = myXmlReader.readXml8583Bean();
		this.requestMap = new HashMap<String, String>();
	}

	public void clean() {
		this.requestMap.clear();
	}

	public void setBit(int id, String value) throws Packet8583Exception,
			UnsupportedEncodingException {
		if (this.requestMap != null) {
			this.requestMap.put(String.valueOf(id), value);
			this.iosPackage.getIsoField(String.valueOf(id)).setValue(value);
		} else {
			throw new Packet8583Exception("设置bit域失败，未实例化Iso8583Manager管理类");
		}
	}

	public void setBit(String id, String value) throws Packet8583Exception,
			UnsupportedEncodingException {
		if (this.requestMap != null) {
			this.requestMap.put(id, value);
			this.iosPackage.getIsoField(id).setValue(value);
		} else {
			throw new Packet8583Exception("设置bit域失败，未实例化Iso8583Manager管理类");
		}
	}

	public void setBinaryBit(int id, byte[] value) throws Packet8583Exception,
			UnsupportedEncodingException {
		if (this.requestMap != null) {
			this.requestMap.put(String.valueOf(id), EncodeUtil.binary(value));
			this.iosPackage.getIsoField(String.valueOf(id)).setValue(
					EncodeUtil.binary(value));
		} else {
			throw new Packet8583Exception("设置bit域失败，未实例化Iso8583Manager管理类");
		}
	}

	public void setBinaryBit(String id, byte[] value)
			throws Packet8583Exception, UnsupportedEncodingException {
		if (this.requestMap != null) {
			this.requestMap.put(id, EncodeUtil.binary(value));
			this.iosPackage.getIsoField(id).setValue(EncodeUtil.binary(value));
		} else {
			throw new Packet8583Exception("设置bit域失败，未实例化Iso8583Manager管理类");
		}
	}

	public String getBit(int id) throws Packet8583Exception {
		if (this.requestMap != null)
			return ((String) this.requestMap.get(String.valueOf(id)));

		throw new Packet8583Exception("获取对应bit域失败，未实例化Iso8583Manager管理类");
	}

	public String getBit(String id) throws Packet8583Exception {
		if (this.requestMap != null)
			return ((String) this.requestMap.get(id));

		throw new Packet8583Exception("获取对应bit域失败，未实例化Iso8583Manager管理类");
	}

	public byte[] pack() throws Packet8583Exception, ClassNotFoundException,
			IOException {
		IsoMsgFactory factory = IsoMsgFactory.getInstance();
		System.out.print("组包时候的hashmap = " + this.requestMap.toString());
		return factory.pack(this.requestMap, this.iosPackage);
	}

	public void unpack(byte[] source) throws Exception {
		Log.i("qiuyi", "Iso8583Manager.unpack开始");
		IsoMsgFactory factory = IsoMsgFactory.getInstance();
		System.out.println("解包数据为:"
				+ BCDHelper.hex2DebugHexString(source, source.length));
		this.requestMap = factory.unpack(source, this.iosPackage);
	}

	public byte[] getMacData(String start, String end) throws IOException {
		IsoMsgFactory factory = IsoMsgFactory.getInstance();
		factory.addBitmap(this.requestMap, this.iosPackage);
		return factory.merge(this.iosPackage, start, end);
	}

	public String getBitString() {
		return this.iosPackage.toString();
	}

	public void Load8583XMLconfigByTag(String xml8583tag) {
		Xml8583Config myXmlReader = new Xml8583Config(this.context);
		myXmlReader.ISO8583CONFIG = xml8583tag;
		this.iosPackage = myXmlReader.readXml8583Bean();
		this.requestMap = new HashMap<String, String>();
	}

	public void restore8583XMLconfig() {
		Xml8583Config myXmlReader = new Xml8583Config(this.context);
		myXmlReader.ISO8583CONFIG = "ISO8583Config";
		this.iosPackage = myXmlReader.readXml8583Bean();
		this.requestMap = new HashMap<String, String>();
	}
}
