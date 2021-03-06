package com.mwdev.sxsmcardpay.iso8583;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.basewin.define.BwPinpadSource;
import com.basewin.interfaces.OnApduCmdListener;
import com.basewin.interfaces.OnDetectListener;
import com.basewin.packet8583.exception.Packet8583Exception;
import com.basewin.services.ServiceManager;
import com.mwdev.sxsmcardpay.PosApplication;
import com.mwdev.sxsmcardpay.util.PosUtil;
import com.pos.sdk.utils.PosByteArray;

/**
 * 明华信息科技有限公司 版权所有(c)
 * 
 * @author qiuyi
 * @date
 * @version
 * @function
 * @lastmodify
 */
public class Iso8583Mgr {
	private static final String TAG = "Iso8583Mgr";
	private Iso8583Manager manager_packData;
	private Iso8583Manager manager_unpackData;
	private String psamId;
	//successful
	public final static int MAC_SUCCESS=0;
	public final static int UNPACKDATA_ERROR=4;
	public final static int MAC_ERROR=1;
	public final static int LRC_ERROR=2;
	public final static int UNPACKDATA_SUCCESS=6;
	public final static int LENGTH_ERROR=3;
	public final static int EMPTY_MAC=5;
	private CardNumInterface mycardnumif;
	private PosApplication mPosApp;
	public static final String IS_CHECKOUT="is_checkout";
	public static final String PIK_LOCAL="pik_local";
	public static final String MAK_LOCAL="mak_local";
	public static final String INTENT_TAG="intent_tag";
	public void addResigter(CardNumInterface cardnuminterface) {
		mycardnumif = cardnuminterface;
	}

	public Iso8583Mgr(Context context) {
		manager_packData = new Iso8583Manager(context);
		manager_unpackData = new Iso8583Manager(context);
		mPosApp = (PosApplication) context;
	}

	public String getPsamId() {
		return psamId;
	}

	public void setPsamId(String psamId) {
		this.psamId = psamId;
	}

	public Iso8583Manager getManager_packData() {
		return manager_packData;
	}

	public void setManager_packData(Iso8583Manager manager_packData) {
		this.manager_packData = manager_packData;
	}

	public Iso8583Manager getManager_unpackData() {
		return manager_unpackData;
	}

	public void setManager_unpackData(Iso8583Manager manager_unpackData) {
		this.manager_unpackData = manager_unpackData;
	}

	/**
	 * 封包
	 */
	public byte[] packData(Map<String, String> sourceMap, boolean needMac)
			throws Packet8583Exception, UnsupportedEncodingException {
		for (Map.Entry<String, String> entry : sourceMap.entrySet()) {

			Log.i("qiuyi", "Key = " + entry.getKey() + "       Value = "
					+ entry.getValue());
			manager_packData.setBit(entry.getKey(), entry.getValue());

		}
		// // 打包调用
		byte[] needunpack = null;
		try {
			needunpack = manager_packData.pack();
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("qiuyi",
				"bitmap  -------------》" + manager_packData.getBit("bitmap"));
		Log.i("qiuyi",
				"包装的数据包1：-------------》" + PosUtil.byteArray2Hex(needunpack));
        String needunpack_string=PosUtil.byteArray2Hex(needunpack);
		String out_macString=needunpack_string.substring(0,needunpack_string.length()-16);
		// 这里增加mac码
		// sourceMacData为要mac的数据
		if (needMac) {
			String sourceMacData = PosUtil.byteArray2Hex(needunpack).substring(34,
					PosUtil.byteArray2Hex(needunpack).length() - 16);
			Log.i("qiuyi", "sourceMacData  -------------》" + sourceMacData);
			// 计算mac码
			String Mac = "";
			try {
				Mac = ServiceManager.getInstence().getPinpad()
						.calcMAC(sourceMacData, BwPinpadSource.MAC_MOD2);
				Log.i("qiuyi", "Mac码==============》" + Mac);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			byte[] MacByte = PosUtil.HexStringToByteArray(Mac);
			// 添加64域计算好的mac码
			manager_packData.setBinaryBit("64", MacByte);
			// 重新打包数据（将计算号的mac码添加进报文里）

			needunpack_string=out_macString+Mac;

			needunpack=PosUtil.HexStringToByteArray(needunpack_string);
//			try {
//				needunpack = manager_packData.pack();
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}

		return addSTX_ETX(needunpack);

	}

	/**
	 * 封包添加报文长度、STX、ETX和校验码
	 */
	public byte[] addSTX_ETX(byte[] needunpack) {
		// 获取长度
		int len = needunpack.length;
		String f = "";
		if (len < 16 && len > 0) {
			f = "0" + Integer.toHexString(len) + "00";
		} else if (len >= 16 && len < 256) {
			f = Integer.toHexString(len) + "00";
		} else if (len >= 256 && len < 4096) {
			f = "0" + Integer.toHexString(len);
			f = f.substring(2, 4) + f.substring(0, 2);
		} else if (len >= 4096) {
			f = Integer.toHexString(len);
			f = f.substring(2, 4) + f.substring(0, 2);
		}
		byte[] bytelen = PosUtil.HexStringToByteArray(f);
		Log.i("qiuyi", "bytelen：-------------》" + PosUtil.byteArray2Hex(bytelen));
		// etx
		byte[] ETX = PosUtil.HexStringToByteArray("03");
		byte[] LEN_ETX = new byte[len + 3];
		System.arraycopy(bytelen, 0, LEN_ETX, 0, bytelen.length);
		System.arraycopy(needunpack, 0, LEN_ETX, bytelen.length,
				needunpack.length);
		System.arraycopy(ETX, 0, LEN_ETX, bytelen.length + needunpack.length, 1);
		Log.i("qiuyi", "LEN_ETX：-------------》" + PosUtil.byteArray2Hex(LEN_ETX));
		// 得到校验码
		int tep = 0;

		for (int j = 0; j < LEN_ETX.length; j++) {
			tep = tep ^ LEN_ETX[j];
		}
		Log.i("qiuyi", "tep：-------------》" + tep);
		byte[] LRC = PosUtil.IntToHex(tep);
		// Integer.
		Log.i("qiuyi", "LRC：-------------》" + PosUtil.byteArray2Hex(LRC));
		Log.i("qiuyi", "LRC.length：-------------》" + LRC.length);
		// 拼成完整报文
		byte[] STX = PosUtil.HexStringToByteArray("02");
		byte[] STX_len_ETX_LRC = new byte[LEN_ETX.length + 2];
		System.arraycopy(STX, 0, STX_len_ETX_LRC, 0, 1);
		System.arraycopy(LEN_ETX, 0, STX_len_ETX_LRC, 1, LEN_ETX.length);
		System.arraycopy(LRC, LRC.length - 1, STX_len_ETX_LRC,
				LEN_ETX.length + 1, 1);
		Log.i("qiuyi",
				"包装的数据包2：-------------》" + PosUtil.byteArray2Hex(STX_len_ETX_LRC));
		return STX_len_ETX_LRC;

	}

	/**
	 * 解包前处理
	 */
	public byte[] before_unpackData(byte[] needunpack) {
		if (needunpack[0] == 02) {
			byte[] ss = new byte[needunpack.length - 2];
			System.arraycopy(needunpack, 1, ss, 0, needunpack.length - 2);
			// 取校验码
			int tep = 0;

			for (int j = 0; j < ss.length; j++) {
				tep = tep ^ ss[j];
			}
			Log.i("qiuyi", "tep：-------------》" + tep);
			Log.i("qiuyi", "needunpack[needunpack.length - 1]：-------------》"
					+ needunpack[needunpack.length - 1]);
			// 判断校验吗正确否
			if (tep == needunpack[needunpack.length - 1]) {
				Log.i("qiuyi", "校验通过");
				byte[] sss = new byte[needunpack.length - 3];
				System.arraycopy(needunpack, 1, sss, 0, needunpack.length - 3);
				return sss;
			} else {
				Log.i("qiuyi", "校验不通过");
//				byte[] sss = new byte[needunpack.length - 3];
//				System.arraycopy(needunpack, 1, sss, 0, needunpack.length - 3);
				return null;

			}
		} else
			return null;

	}

	/**
	 * 解包
	 */
	public int unpackData(byte[] needunpack) {
		byte[] data = before_unpackData(needunpack);
		Log.i("qiuyi", "Iso8583Mgr.unpackData开始");
		if(data != null){
				int len = Integer.valueOf(data[1]) * 256
						+ (Integer.valueOf(data[0]) & 0xff);
			if(len==data.length-2){
				Log.i("qiuyi","chang du jiao yan cheng gong!");
				byte[] needunpack2 = new byte[data.length - 2];
				Log.i("qiuyi",
						"Integer.valueOf(data[1])* 256======>"
								+ Integer.valueOf(data[1] * 256));
				Log.i("qiuyi", "len======>" + len);

				// 解包需要去掉前面两个字节的长度数据
				System.arraycopy(data, 2, needunpack2, 0, len);
				try {
					manager_unpackData.unpack(needunpack2);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.i("qiuyi", "manager2.unpack出错" + e.toString());
					e.printStackTrace();
					return UNPACKDATA_ERROR;

				}
				// Log.d("8583", "解包数据为 = " +
				// BCDHelper.hex2DebugHexString(manager2.pack(),
				// manager2.pack().length));
				// 第3位、第4位、第11位、第22位、第25位、第26位、第35位、第41位、第42位、第49位、第52位、第53位、第60位、第64位。
				// 解包后获取各个域的数据
				Log.i("qiuyi", "tpdu====>" + manager_unpackData.getBit("tpdu")
						+ "\n header====>" + manager_unpackData.getBit("header")
						+ "\n msgid====>" + manager_unpackData.getBit("msgid")
						+ "\n bitmap====>" + manager_unpackData.getBit("bitmap")
						+ "\n 域2-主账号====>" + manager_unpackData.getBit("2")
						+ "\n 域3-交易处理码====>" + manager_unpackData.getBit("3")
						+ "\n 域4====>" + manager_unpackData.getBit("4")
						+ "\n 域8====>" + manager_unpackData.getBit("8")
						+ "\n 域11====>" + manager_unpackData.getBit("11")
						+ "\n 域12====>" + manager_unpackData.getBit("12")
						+ "\n 域13====>" + manager_unpackData.getBit("13")
						+ "\n 域14====>" + manager_unpackData.getBit("14")
						+ "\n 域15====>" + manager_unpackData.getBit("15")
						+ "\n 域22====>" + manager_unpackData.getBit("22")
						+ "\n 域23====>" + manager_unpackData.getBit("23")
						+ "\n 域24====>" + manager_unpackData.getBit("24")
						+ "\n 域25====>" + manager_unpackData.getBit("25")
						+ "\n 域26====>" + manager_unpackData.getBit("26")
						+ "\n 域32====>" + manager_unpackData.getBit("32")
						+ "\n 域35====>" + manager_unpackData.getBit("35")
						+ "\n 域36====>" + manager_unpackData.getBit("36")
						+ "\n 域37====>" + manager_unpackData.getBit("37")
						+ "\n 域38====>" + manager_unpackData.getBit("38")
						+ "\n 域39====>" + manager_unpackData.getBit("39")
						+ "\n 域41====>" + manager_unpackData.getBit("41")
						+ "\n 域42====>" + manager_unpackData.getBit("42")
						+ "\n 域44====>" + manager_unpackData.getBit("44")
						+ "\n 域48====>" + manager_unpackData.getBit("48")
						+ "\n 域49====>" + manager_unpackData.getBit("49")
						+ "\n 域52====>" + manager_unpackData.getBit("52")
						+ "\n 域53====>" + manager_unpackData.getBit("53")
						+ "\n 域54====>" + manager_unpackData.getBit("54")
						+ "\n 域55====>" + manager_unpackData.getBit("55")
						+ "\n 域58====>" + manager_unpackData.getBit("58")
						+ "\n 域60====>" + manager_unpackData.getBit("60")
						+ "\n 域61====>" + manager_unpackData.getBit("61")
						+ "\n 域62====>" + manager_unpackData.getBit("62")
						+ "\n 域63====>" + manager_unpackData.getBit("63")
						+ "\n 域64-mac码====>" + manager_unpackData.getBit("64"));
				return UNPACKDATA_SUCCESS;
			}else {
				return LENGTH_ERROR;
			}

		}else {
				return LRC_ERROR;
		}





	}

	public int makeMac(byte[] data){


		byte[] sourceMacdata=new byte[data.length - 30];
		System.arraycopy(data, 20, sourceMacdata, 0, sourceMacdata.length);
		byte[] Macdata=new byte[8];
		System.arraycopy(data, data.length-10, Macdata, 0,8);

		Log.i("xxx", "Macdata=====>"+ PosUtil.byteArray2Hex(Macdata));
		Log.i("xxx", "byteArray2Hex(sourceMacdata)=====>"+PosUtil.byteArray2Hex(data));
		String Mac = "";
		try {
			Mac = ServiceManager.getInstence().getPinpad()
					.calcMAC(sourceMacdata, BwPinpadSource.MAC_MOD2);
			Log.i("xxx", "Mac码==============》" + Mac);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(Mac.equalsIgnoreCase(PosUtil.byteArray2Hex(Macdata))){
			return MAC_SUCCESS;
		}else return MAC_ERROR;
}



	public String getBitData(int id) {
		return manager_unpackData.getBit(id);
	}

	public String getBitData(String id) {
		return manager_unpackData.getBit(id);
	}

	public void getData() {
		// TODO Auto-generated method stub
		String tpdu = manager_packData.getBit("tpdu");
		String data_49 = manager_packData.getBit("49");
		String data_2 = manager_packData.getBit("2");
		String data_3 = manager_packData.getBit("3");
		String head = manager_packData.getBit("header");
		String msgid = manager_packData.getBit("msgid");
		String data_4 = manager_packData.getBit("4");
		String data_11 = manager_packData.getBit("11");
		String data_22 = manager_packData.getBit("22");
		String data_25 = manager_packData.getBit("25");
		// String data_26 = manager2.getBit("26");
		String data_39 = manager_packData.getBit("39");
		String data_41 = manager_packData.getBit("41");
		String data_42 = manager_packData.getBit("42");
		String data_53 = manager_packData.getBit("53");
		String data_55 = manager_packData.getBit("55");
		String data_60 = manager_packData.getBit("60");
		String data_64 = manager_packData.getBit("64");
		String bitmap = manager_packData.getBit("bitmap");

		Log.d(TAG, "tpdu====>" + tpdu + "        head====>" + head
				+ "        msgid====>" + msgid + " bitmap = " + bitmap
				+ "  data_2 = " + data_2 + "  data_3 = " + data_3
				+ "  data_4 = " + data_4 + "  data_11 = " + data_11
				+ "  data_22 = " + data_22 + "  data_25 = " + data_25
				+ "  data_39 = " + data_39 + "  data_41 = " + data_41
				+ "  data_42 = " + data_42 + "  data_53 = " + data_53
				+ "  data_55 = " + data_55 + "  data_49 = " + data_49
				+ "  data_60 = " + data_60 + "  data_64 = " + data_64);
	}

	// tpdu====>6000000000
	// header====>060101000000000000000000
	// msgid====>00080000
	// bitmap====>2018000002C000010000000000000000
	//
	// 域3-交易处理码====>000000000000
	//
	// 域12====>010102070207
	// 域13====>0200010600080105
	// 域39====>3030
	// 域41====>393330363030383132373833
	// 域42====>393131313131303030303030303031
	// 域64-mac码====>0000000000000000

	/**
	 * 签到报文 checkIn(String psamid, String merchantNum) String psamid 受卡机终端标识码
	 * SAM卡ID为终端号 12位 S tring merchantNum 受卡方标识码 商户号 15位
	 */
	public byte[] checkIn(String psamid, String merchantNum) {
		// getPsamId();
		byte[] packdata = null;
		Map<String, String> sourcedata = new HashMap<String, String>();
		sourcedata.put("tpdu", "6000000000");
		sourcedata.put("header", "060101000000000000000000");
		sourcedata.put("msgid", "00080000");
		sourcedata.put("3", "000000000000");
		// 域11-----交易流水号
		// 签到是否需要流水号 待定
		// 域12-受卡方所在地时间
		sourcedata.put("12", getBCD(getTime()));
		sourcedata.put("13", getBCD(getDate()));
		// 41域 an12 受卡机终端标识码 SAM卡ID为终端号
		sourcedata.put("41", psamid);
		// 42域 an15 受卡方标识码 商户号
		sourcedata.put("42", merchantNum);
		sourcedata.put("60", getBCD("00" + getBatch() + "003"));
		sourcedata.put("64", "");
		try {
			packdata = packData(sourcedata, false);
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packdata;
	}

	@SuppressLint("SimpleDateFormat")
	public String getDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String DataAndTime = df.format(new Date());
		return DataAndTime;

	}

	/**
	 * 签退报文
	 */
	public byte[] checkOut(String psamid, String merchantNum) {
		byte[] packdata = null;
		Map<String, String> sourcedata = new HashMap<String, String>();
		sourcedata.put("tpdu", "6000000000");
		sourcedata.put("header", "060101000000000000000000");
		sourcedata.put("msgid", "00080200");
		sourcedata.put("3", "000000000000");
		sourcedata.put("12", getBCD(getTime()));
		sourcedata.put("13", getBCD(getDate()));

		// 41域 an12 受卡机终端标识码 SAM卡ID为终端号
		sourcedata.put("41", psamid);
		// 42域 an15 受卡方标识码 商户号
		sourcedata.put("42", merchantNum);
		sourcedata.put("60", getBCD("00" + getBatch() + "002"));
		sourcedata.put("64", "");
		try {
			packdata = packData(sourcedata, true);
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packdata;
	}

	/**
	 * 余额查询报文
	 */
	public byte[] balance_query(String psamId, String merchantNum,
			String cardNum, String passWord, String tradeNum) {
		byte[] packdata = null;
		Map<String, String> sourcedata = new HashMap<String, String>();
		sourcedata.put("tpdu", "6000000000");
		sourcedata.put("header", "060101000000000000000000");
		sourcedata.put("msgid", "00020000");
		sourcedata.put("55", cardNum);
		sourcedata.put("3", "030101000000");
		// 查询时 消费额值0 可选
		// sourcedata.put("4", "000000000000000000000000");

		sourcedata.put("11", getBCD(tradeNum));
		sourcedata.put("12", getBCD(getTime()));
		sourcedata.put("13", getBCD(getDate()));
		sourcedata.put("22", getBCD("051"));
		sourcedata.put("25", getBCD("00"));
		sourcedata.put("26", getBCD("06"));
		// 32 受理方标识码 n..11 LLVAR BCD C M 上送设备自定义终端编号，下发市民卡中心编号(待定)
		sourcedata.put("49", "156");
		sourcedata.put("41", psamId);
		sourcedata.put("42", merchantNum);
//		sourcedata.put("52", passWord);
		sourcedata.put("53", getBCD("2600000000000000"));
		// 54 附加金额 an…020 LLLVAR ASCII M
		sourcedata.put("60", getBCD("01" + getBatch() + "000" + "5"));
		sourcedata.put("64", "");
		try {
			packdata = packData(sourcedata, true);
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packdata;
	}

	/**
	 * 消费报文
	 */
	public byte[] trade(String psamId, String merchantNum, String cardNum,
			String passWord, String tradeNum, String amount) {
		byte[] packdata = null;
		Map<String, String> sourcedata = new HashMap<String, String>();
		sourcedata.put("tpdu", "6000000000");
		sourcedata.put("header", "060101000000000000000000");
		sourcedata.put("msgid", "00020000");
		sourcedata.put("55", cardNum);
		sourcedata.put("3", "000001000000");
		sourcedata.put("4", getBCD(amount));
		sourcedata.put("11", getBCD(tradeNum));
		sourcedata.put("12", getBCD(getTime()));
		sourcedata.put("13", getBCD(getDate()));
		sourcedata.put("22", getBCD("051"));
		sourcedata.put("25", getBCD("00"));
		sourcedata.put("26", getBCD("06"));
		sourcedata.put("41", psamId);
		sourcedata.put("42", merchantNum);
		sourcedata.put("49", "156");
		sourcedata.put("52", passWord);
		sourcedata.put("53", getBCD("2600000000000000"));
		sourcedata.put("60", getBCD("22" + getBatch() + "000" + "5"));
		sourcedata.put("64", "");
		try {
			packdata = packData(sourcedata, true);
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packdata;
	}

	// impact_reason 冲正原因码
	// 98 POS终端在时限内未能收到POS中心的应答
	// 96 POS终端收到POS中心的批准应答消息，但由于POS机故障无法完成交易
	// A0 POS终端对收到POS中心的批准应答消息，验证MAC出错
	// 06 其他情况引发的冲正
	// original_tradeNum 原始交易流水号
	// original_batchNum 原始批次号
	/**
	 * 消费冲正
	 */
	public byte[] trade_impact(String impact_reason, String psamId,
			String merchantNum, String cardNum, String passWord,
			String tradeNum, String amount, String original_batchNum,
			String original_tradeNum) {
		byte[] packdata = null;
		Map<String, String> sourcedata = new HashMap<String, String>();
		sourcedata.put("tpdu", "6000000000");
		sourcedata.put("header", "060101000000000000000000");
		sourcedata.put("msgid", "00040000");
		sourcedata.put("55", cardNum);
		sourcedata.put("3", "000001000000");
		sourcedata.put("4", getBCD(amount));
		sourcedata.put("11", getBCD(tradeNum));
		sourcedata.put("12", getBCD(getTime()));
		sourcedata.put("22", getBCD("051"));
		sourcedata.put("25", getBCD("00"));

		sourcedata.put("39", impact_reason);
		sourcedata.put("41", psamId);
		sourcedata.put("42", merchantNum);
		sourcedata.put("49", "156");
		sourcedata.put("60", getBCD("22" + getBatch() + "000" + "5"));
		sourcedata.put("61", getBCD(original_batchNum + original_tradeNum));
		sourcedata.put("64", "");

		try {
			packdata = packData(sourcedata, true);
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return packdata;

	}

	// psamId psam卡终端号
	// merchantNum 商户号
	// cardNum 主帐号
	// passWord 个人密码
	// tradeNum 交易流水号
	// amount 金额
	// retrieve_referenceNum 检索参考号
	// original_tradeNum 原始交易流水号
	// original_batchNum 原始批次号
	//

	/**
	 * 消费撤销报文
	 */
	public byte[] trade_cancel(String psamId, String merchantNum,
			String cardNum, String passWord, String tradeNum, /*String amount,*/
			String retrieve_referenceNum, String original_tradeNum,
			String original_batchNum,String original_amount) {
		byte[] packdata = null;
		Map<String, String> sourcedata = new HashMap<String, String>();
		sourcedata.put("tpdu", "6000000000");
		sourcedata.put("header", "060101000000000000000000");
		sourcedata.put("msgid", "00020000");
		sourcedata.put("4", getBCD(original_amount));
		sourcedata.put("55", cardNum);
		// 20 10 00
		sourcedata.put("3", getBCD("201000"));
		// 撤销时要不要传入金额，待定
//		sourcedata.put("4", getBCD(amount));
		sourcedata.put("11", getBCD(tradeNum));
		sourcedata.put("12", getBCD(getTime()));
		sourcedata.put("13", getBCD(getDate()));
		sourcedata.put("22", getBCD("051"));
		sourcedata.put("25", getBCD("00"));
		sourcedata.put("26", getBCD("06"));
		sourcedata.put("37", retrieve_referenceNum);
		// 38 授权标识应答码 an6 ASCII 如果原始交易的应答中有授权码，则须填入原交易授权码
		sourcedata.put("41", psamId);
		sourcedata.put("42", merchantNum);
		sourcedata.put("49", "156");
//		sourcedata.put("52", passWord);
		sourcedata.put("53", getBCD("2600000000000000"));
		sourcedata.put("60", getBCD("23" + getBatch() + "000" + "5"));

		sourcedata.put("61", getBCD(original_batchNum + original_tradeNum));

		sourcedata.put("64", "");

		try {
			packdata = packData(sourcedata, true);
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packdata;

	}

	/**
	 * 消费撤销冲正
	 */

	public byte[] trade_cancel__impact(String psamId, String merchantNum,
			String cardNum, String passWord, String tradeNum, String amount,
			String retrieve_referenceNum, String original_tradeNum,
			String original_batchNum, String impact_reason) {
		byte[] packdata = null;
		Map<String, String> sourcedata = new HashMap<String, String>();
		sourcedata.put("tpdu", "6000000000");
		sourcedata.put("header", "060101000000000000000000");
		sourcedata.put("msgid", "00040000");
		sourcedata.put("55", cardNum);
		sourcedata.put("3", getBCD("201000"));
		sourcedata.put("4", getBCD(amount));
		sourcedata.put("11", getBCD(tradeNum));
		sourcedata.put("12", getBCD(getTime()));
		sourcedata.put("13", getBCD(getDate()));
		sourcedata.put("22", getBCD("051"));
		sourcedata.put("25", getBCD("00"));
		sourcedata.put("39", impact_reason);
		sourcedata.put("41", psamId);
		sourcedata.put("42", merchantNum);
		sourcedata.put("49", "156");
		sourcedata.put("60", getBCD("23" + getBatch() + "000" + "5"));
		sourcedata.put("61", getBCD(original_batchNum + original_tradeNum));
		sourcedata.put("64", "");
		try {
			packdata = packData(sourcedata, true);
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packdata;

	}

	// operatorNum pos机操作员号
	// operatorPassWord pos机操作员密码

	/**
	 * 退货报文
	 */
	public byte[] Return_goods(String psamId, String merchantNum,
			String cardNum, String passWord, String tradeNum, String amount,
			String retrieve_referenceNum, String original_tradeNum,
			String original_batchNum) {
		byte[] packdata = null;
		Map<String, String> sourcedata = new HashMap<String, String>();
		sourcedata.put("tpdu", "6000000000");
		sourcedata.put("header", "060101000000000000000000");
		sourcedata.put("msgid", "00020200");
		sourcedata.put("55", cardNum);
		sourcedata.put("3", getBCD("401000"));
		sourcedata.put("4", getBCD(amount));
		sourcedata.put("11", getBCD(tradeNum));
		sourcedata.put("12", getBCD(getTime()));
		sourcedata.put("13", getBCD(getDate()));
		sourcedata.put("22", getBCD("051"));
		sourcedata.put("25", getBCD("00"));
		sourcedata.put("26", getBCD("06"));
		sourcedata.put("37", retrieve_referenceNum);
		sourcedata.put("41", psamId);
		sourcedata.put("42", merchantNum);
		sourcedata.put("49", "156");
//		sourcedata.put("52", passWord);
		sourcedata.put("53", getBCD("2600000000000000"));
		sourcedata.put("60", getBCD("25" + getBatch() + "000" + "5"));
		sourcedata.put("61", getBCD(original_batchNum + original_tradeNum));
		// 63域 暂定
//		sourcedata.put("63", operatorNum + operatorPassWord);
		sourcedata.put("64", "");
		try {
			packdata = packData(sourcedata, true);
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packdata;

	}

	// total_consumption 消费总额
	// total_penNum 消费总笔数
	// tradeNum 消费流水号
	// merchantNum 商户号
	// operatorNum pos机操作员号

	/**
	 * 批结算报文
	 */
	public byte[] batch_settlement(String total_consumption,
			String total_penNum,String Dtotal_consumption,String Dtotal_penNum, String tradeNum, String psamId,
			String merchantNum, String operatorNum) {
		byte[] packdata = null;
		Map<String, String> sourcedata = new HashMap<String, String>();
		sourcedata.put("tpdu", "6000000000");
		sourcedata.put("header", "060101000000000000000000");
		sourcedata.put("msgid", "00050000");
		sourcedata.put("3", getBCD("000000"));
		sourcedata.put("11", getBCD(tradeNum));
		sourcedata.put("12", getBCD(getTime()));
		sourcedata.put("13", getBCD(getDate()));
		sourcedata.put("41", psamId);
		sourcedata.put("42", merchantNum);
		sourcedata.put("48", getBCD(total_consumption + total_penNum +Dtotal_consumption+Dtotal_penNum+"0"));
		sourcedata.put("49", "156");
		sourcedata.put("60", getBCD("00" + getBatch() + "201"));
		sourcedata.put("63", operatorNum);
		sourcedata.put("64", "");
		try {
			packdata = packData(sourcedata, true);
		} catch (Packet8583Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packdata;

	}

	@SuppressLint("SimpleDateFormat")
	public String getTime() {
		SimpleDateFormat df = new SimpleDateFormat("HHmmss");
		String DataAndTime = df.format(new Date());
		return DataAndTime;

	}

	@SuppressLint("SimpleDateFormat")
	public String getBatch() {
		String DataAndTime = mPosApp.getBattchNum();
		return DataAndTime;

	}

	/**
	 * 十进制数字符串转成非压缩BCD
	 */

	public String getBCD(String data) {

		String ss = "";
		if (data != null) {
			for (int i = 0; i < data.length(); i++) {
				ss += "0" + data.charAt(i);
			}
		}else return null;

		return ss;

	}

	/**
	 * 
	 * 读取psam卡终端号
	 */
	public void readPsamId() {
		// TODO Auto-generated method stub
		final ServiceManager msm = ServiceManager.getInstence();
		try {// 判断有无卡
			msm.getCard().openPsamAndDetectBy38400(100, new OnDetectListener() {

				@Override
				public void onSuccess(int cardtype) {
					// TODO Auto-generated method stub
					Log.d("qiuyi", "psam卡寻卡成功");

					Log.i("qiuyi", "arg0=====>" + cardtype);
					// sb = new StringBuffer("");
					// sb.append("psam卡寻卡成功" + "\n");
					try {
						msm.getCard().resetCard();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						msm.getCard().transmitApduToCard(
								PosUtil.HexStringToByteArray("00B0960006"),
								new OnApduCmdListener() {

									@Override
									public void onSuccess(PosByteArray arg0,
											byte[] arg1) {
										// TODO
										// Auto-generated
										// method stub
										psamId = PosUtil
												.byteArray2Hex(arg0.buffer);
										Log.i("qiuyi", "PSAM终端号===="
												+ psamId);
									}

									@Override
									public void onError() {
										// TODO
										// Auto-generated
										// method stub
										Log.i("qiuyi", "APDU失败");
									}
								});
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				@Override
				public void onError(int errorcode, String msg) {
					// TODO Auto-generated method stub
					Log.d("qiuyi", "psam卡寻卡失败" + errorcode + "   " + msg);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 读取ic卡主帐号
	 */
	public void readCardNum() {
		final ServiceManager msm = ServiceManager.getInstence();
		// TODO Auto-generated method stub
		try {
			msm.getCard().openCPUAndDetect(30000, new OnDetectListener() {

				@Override
				public void onSuccess(int arg0) {
					// // TODO Auto-generated method stub
					Log.i("qiuyi", "寻卡成功");
					try {
						msm.getCard().transmitApduToCard(
								PosUtil.HexStringToByteArray("00A40000023F0100"),
								new OnApduCmdListener() {

									@Override
									public void onSuccess(PosByteArray arg0,
														  byte[] arg1) {
										// TODO
										// Auto-generated
										// method stub
										Log.i("qiuyi",
												"选择3F01 success argo===="
														+ arg0);
										Log.i("qiuyi",
												"选择3F01 success arg1===="
														+ PosUtil.byteArray2Hex(arg1));

										try {
											Thread.sleep(100);
										} catch (InterruptedException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}

										Log.i("qiuyi", "ic卡读二进制文件0015："
												+ "00B095001E");
										try {
											msm.getCard()
													.transmitApduToCard(
															PosUtil.HexStringToByteArray("00B095001E"),
															new OnApduCmdListener() {

																@Override
																public void onSuccess(
																		PosByteArray arg0,
																		byte[] arg1) {
																	// TODO
																	// Auto-generated
																	// method
																	// stub
																	Log.i("qiuyi",
																			"ic卡读卡===="
																					+ PosUtil.byteArray2Hex(arg1));

																	String cardnum = PosUtil
																			.byteArray2Hex(arg0.buffer);
																	Log.i("qiuyi",
																			"ox15文件数据===="
																					+ cardnum);
																	if(cardnum!=null&&cardnum.length()>0){
																		mycardnumif
																				.onScuess(cardnum);
																	}else mycardnumif.onerror(6);


																}

																@Override
																public void onError() {
																	// TODO
																	// Auto-generated
																	// method
																	// stub
																	Log.i("qiuyi",
																			"读二进制文件0015失败");
																	mycardnumif.onerror(5);
																}
															});
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
											mycardnumif.onerror(4);
										}

									}

									@Override
									public void onError() {
										// TODO
										// Auto-generated
										// method stub
										Log.i("qiuyi", "选择3F01文件失败");
										mycardnumif.onerror(3);
									}
								});
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mycardnumif.onerror(2);
					}

				}

				@Override
				public void onError(int arg0, String arg1) {
					// TODO Auto-generated method stub
					Log.i("qiuyi", "IC卡寻卡失败      arg0===>" + arg0
							+ "     arg1=======>" + arg1);
					mycardnumif.onerror(1);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mycardnumif.onerror(0);
		}

	}



	/**
	 * 读取0x05文件的数据
	 */
	public void readdata_05() {
		final ServiceManager msm = ServiceManager.getInstence();
		// TODO Auto-generated method stub
		try {
			msm.getCard().openCPUAndDetect(30000, new OnDetectListener() {

				@Override
				public void onSuccess(int arg0) {
					// // TODO Auto-generated method stub
					Log.i("qiuyi", "寻卡成功");
					try {
						msm.getCard().transmitApduToCard(
								PosUtil.HexStringToByteArray("00A40000023F0000"),
								new OnApduCmdListener() {

									@Override
									public void onSuccess(PosByteArray arg0,
														  byte[] arg1) {
										// TODO
										// Auto-generated
										// method stub
										Log.i("qiuyi",
												"选择3F00 success argo===="
														+ arg0);
										Log.i("qiuyi",
												"选择3F00 success arg1===="
														+ PosUtil.byteArray2Hex(arg1));

										try {
											Thread.sleep(100);
										} catch (InterruptedException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}

										Log.i("qiuyi", "ic卡读二进制文件0005："
												+ "00B0850016");
										try {
											msm.getCard()
													.transmitApduToCard(
															PosUtil.HexStringToByteArray("00B0850016"),
															new OnApduCmdListener() {

																@Override
																public void onSuccess(
																		PosByteArray arg0,
																		byte[] arg1) {
																	// TODO
																	// Auto-generated
																	// method
																	// stub
																	Log.i("qiuyi",
																			"ic卡读卡===="
																					+ PosUtil.byteArray2Hex(arg1));

																	String data_0x05 = PosUtil
																			.byteArray2Hex(arg0.buffer);
																	Log.i("qiuyi",
																			"0x05文件里数据===="
																					+ data_0x05);
																	mycardnumif.ondata_05success(data_0x05);
																	try {
																		Thread.sleep(50);
																	} catch (InterruptedException e) {
																		e.printStackTrace();
																	}
																	readCardNum();

																}

																@Override
																public void onError() {
																	// TODO
																	// Auto-generated
																	// method
																	// stub
																	Log.i("qiuyi",
																			"读二进制文件05失败");
																	mycardnumif.onerror(5);
																}
															});
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
											mycardnumif.onerror(4);
										}

									}

									@Override
									public void onError() {
										// TODO
										// Auto-generated
										// method stub
										Log.i("qiuyi", "选择3F00文件失败");
										mycardnumif.onerror(3);
									}
								});
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mycardnumif.onerror(2);
					}

				}

				@Override
				public void onError(int arg0, String arg1) {
					// TODO Auto-generated method stub
					Log.i("qiuyi", "IC卡寻卡失败      arg0===>" + arg0
							+ "     arg1=======>" + arg1);
					mycardnumif.onerror(1);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mycardnumif.onerror(0);
		}

	}

	/**
	 * 二进制字符串转成十六进制的字符串
	 */
	public static String binaryString2hexString(String bString) {
		if (bString == null || bString.equals("") || bString.length() % 8 != 0)
			return null;
		StringBuffer tmp = new StringBuffer();
		int iTmp = 0;
		for (int i = 0; i < bString.length(); i += 4) {
			iTmp = 0;
			for (int j = 0; j < 4; j++) {
				iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
			}
			tmp.append(Integer.toHexString(iTmp));
		}
		return tmp.toString();
	}

	/**
	 * 十六进制的字符串转成二进制字符串
	 */

	public static String hexString2binaryString(String hexString)
	{
		if (hexString == null || hexString.length() % 2 != 0)
			return null;
		String bString = "", tmp;
		for (int i = 0; i < hexString.length(); i++)
		{
			tmp = "0000"
					+ Integer.toBinaryString(Integer.parseInt(hexString
					.substring(i, i + 1), 16));
			bString += tmp.substring(tmp.length() - 4);
		}
		return bString;
	}

	public static String inputamount2amount(String inputamount) {
		String result = "";
		if (inputamount != null && !inputamount.equalsIgnoreCase("")) {
			if (inputamount.contains(".")) {
				int i = inputamount.indexOf(".");
				if(inputamount.length()-i==3){
					result = inputamount.substring(0, i) + inputamount.substring(i + 1, inputamount.length());
				}else if(inputamount.length()-i==2){
					result = inputamount.substring(0, i) + inputamount.substring(i + 1, inputamount.length())+"0";
				}else result = inputamount.substring(0, i) + "00";

			}else result=inputamount+"00";


			int length = result.length();
			Log.i("qiuyi1","result=====>"+result);
			String add0 = "";
			for (int j = 0; j < 12 - length; j++) {
				add0 += "0";
			}
			result = add0 + result;

	}


		return result;
	}

	/**
	 * 55域数据组织
	 */
public String make55data(String sourcedata){
String data = null;
	if(sourcedata.length()>=100){
		//BCD
		String data1=sourcedata.substring(0,8);
		//BCD
		String data2=sourcedata.substring(8,28);
		//BCD
		String data3=sourcedata.substring(28,30);
		//BCD
		String data4=sourcedata.substring(30,32);
		//HEX
		String data5=sourcedata.substring(32,34);
		//HEX
		String data6=sourcedata.substring(34,36);
		//BCD
		String data7=sourcedata.substring(36,44);
		//BCD
		String data8=sourcedata.substring(44,48);
		//BCD
		String data9=sourcedata.substring(48,52);
		//BCD
		String data10=sourcedata.substring(52,56);
		//HEX
		String data11=sourcedata.substring(56,60);
		//BCD
		String data12=sourcedata.substring(60,62);
        //BCD
		String data13=sourcedata.substring(62,64);
		//HEX
		String data14=sourcedata.substring(64,68);
		//HEX
		String data15=sourcedata.substring(68,84);
		//BCD
		String data16=sourcedata.substring(84,92);
		//BCD
		String data17=sourcedata.substring(92,100);
		//HEX
		String data18=sourcedata.substring(100,102);
		//HEX
		String data19=sourcedata.substring(102,104);
		Log.i("qiuyi","data1=======>"+data1
				     +"\ndata2======>"+data2
				+"\ndata3======>"+data3
				+"\ndata4======>"+data4
				+"\ndata5======>"+data5
				+"\ndata6======>"+data6
				+"\ndata7======>"+data7
				+"\ndata8======>"+data8
				+"\ndata9======>"+data9
				+"\ndata10======>"+data10
				+"\ndata11======>"+data11
				+"\ndata12======>"+data12
				+"\ndata13======>"+data13
				+"\ndata14======>"+data14
				+"\ndata15======>"+data15
				+"\ndata16======>"+data16
				+"\ndata17======>"+data17
				+"\ndata18======>"+data18
				+"\ndata19======>"+data19);
		//1 2  3  4  7 8  9  10 12 13 16 17
//		data1=getBCD(data1);
//		data2=getBCD(data2);
//		data3=getBCD(data3);
//		data4=getBCD(data4);
//		data7=getBCD(data7);
//		data8=getBCD(data8);
//		data9=getBCD(data9);
//		data10=getBCD(data10);
//		data12=getBCD(data12);
//		data13=getBCD(data13);
//		data16=getBCD(data16);
//		data17=getBCD(data17);
//		Log.i("qiuyi","data1=======>"+data1
//				+"\ndata2======>"+data2
//				+"\ndata3======>"+data3
//				+"\ndata4======>"+data4
//				+"\ndata5======>"+data5
//				+"\ndata6======>"+data6
//				+"\ndata7======>"+data7
//				+"\ndata8======>"+data8
//				+"\ndata9======>"+data9
//				+"\ndata10======>"+data10
//				+"\ndata11======>"+data11
//				+"\ndata12======>"+data12
//				+"\ndata13======>"+data13
//				+"\ndata14======>"+data14
//				+"\ndata15======>"+data15
//				+"\ndata16======>"+data16
//				+"\ndata17======>"+data17
//				+"\ndata18======>"+data18
//				+"\ndata19======>"+data19);
		data=data1+data2+data3+data4+data5+data6+data7+data8+data9+data10+data11+data12+data13+data14+data15+data16+data17+data18+data19;
	}

	return data;
}

	/**
	 *  计算字符串有几个特定字符或字符串
	 * @param data
	 * @param which
     * @return
     */
	public int getnum(String data,String which){

		int cnt = 0;
		int offset = 0;
//		while((offset = data.indexOf(which, offset)) != -1){
//			offset = offset + which.length();
//			cnt++;
//		}

		for(int i=0;i<data.length();i++){
			if((data.charAt(i)+"").equalsIgnoreCase(which)){
				cnt++;
			}
		}
		return cnt;
	}

	/**
	 *  判断金额字符串格式是否正确
	 * @param data
     * @return
     */
	public boolean isnumtrue(String data){
		if(getnum(data,".")>=2){
			return false;
		}else if(getnum(data,".")==1){
			int i=data.indexOf(".");
			if(data.length()-i>=4){
				return false;
			}
		}
		return true;
	}

	/**
	 * 让输入的不太正规的金额，正确显示
	 * @param data
	 * @return
     */
	public String textshow_amount(String data){
		String result="";
		if(getnum(data, ".")==0){
			result=data+".00";
		}else {
			int i = data.indexOf(".");
			if (i == 0) {
               if(data.length() - i == 2){
				   result = "0"+data + "0";
			   }else {
				   result = "0"+data;
			   }
			}else{
			if (data.length() - i == 1) {
				result = data + "00";
			} else if (data.length() - i == 2) {
				result = data + "0";
			} else {
				result = data;
			}
		}
		}
		return result;
	}
}
