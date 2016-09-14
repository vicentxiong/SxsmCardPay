package com.mwdev.sxsmcardpay.iso8583;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.basewin.packet8583.model.BitMap;
import com.basewin.packet8583.model.IsoField;
import com.basewin.packet8583.model.IsoPackage;
import com.basewin.utils.BCDHelper;
import com.basewin.utils.EncodeUtil;
import com.basewin.utils.SimpleUtil;
import com.mwdev.sxsmcardpay.util.PosLog;

public abstract class AbstractIsoMsgFactory {
	public byte[] pack(Map<String, String> dataMap, IsoPackage pack)
			throws IOException, ClassNotFoundException {
		IsoPackage packClone = pack.deepClone();
		List<Integer> dataFieldList = new ArrayList<Integer>(dataMap.size());
		for (Iterator<String> localIterator = dataMap.keySet().iterator(); localIterator
				.hasNext();) {
			String key = (String) localIterator.next();
			IsoField field = packClone.getIsoField(key);
			if (field == null)
				continue;
			if (dataMap.get(key) == null)
				continue;

			field.setValue((String) dataMap.get(key));

			if (!(SimpleUtil.isNumeric(key)))
				continue;
			int val = Integer.valueOf(key).intValue();
			if ((packClone.isBit64()) && (val > 64)) {
				packClone.setBit64(false);

				dataFieldList.add(Integer.valueOf(1));
			}
			dataFieldList.add(Integer.valueOf(val));
		}

		BitMap bitMap = null;
		if (packClone.isBit64())
			bitMap = new BitMap(64);
		else
			bitMap = new BitMap(128);

		byte[] bitMapByte = bitMap.addBits(dataFieldList);
		byte[] bitMapByteMore = new byte[] { 00, 00, 00, 00, 00, 00, 00, 00 };

		byte[] bitMapByteNew = new byte[bitMapByte.length
				+ bitMapByteMore.length];
		System.arraycopy(bitMapByte, 0, bitMapByteNew, 0, bitMapByte.length);
		System.arraycopy(bitMapByteMore, 0, bitMapByteNew, bitMapByte.length,
				bitMapByteMore.length);
		PosLog.i("qiuyi", "bitmap========>" + util.byteArray2Hex(bitMapByteNew));
		packClone.getIsoField("bitmap").setByteValue(bitMapByteNew);

		return merge(packClone);
	}

	public Map<String, String> unpack(byte[] bts, IsoPackage pack)
			throws Exception {
		PosLog.i("qiuyi", "Map<String, String> unpack开始");
		if ((pack == null) || (pack.size() == 0))
			throw new IllegalArgumentException("配置为空，请检查IsoPackage是否为空");

		Map<String, String> returnMap = new HashMap<String, String>();

		int offset = 0;

		IsoPackage target = pack.deepClone();

		boolean hasBitMap = false;
		BitMap bitMap = null;
		for (Iterator<IsoField> localIterator = target.iterator(); localIterator
				.hasNext();) {
			IsoField field = (IsoField) localIterator.next();
//			System.out.println(field.getId() + "解包数据为:" + field.toString()
//					+ "\n");
//			Log.i("qiuyi", field.getId() + "解包数据为:" + field.toString() + "\n");
			if (field.isAppData()) {
//				PosLog.i("qiuyi", "if (field.isAppData())");
				if (!(hasBitMap))
					continue;
				int index = Integer.valueOf(field.getId()).intValue();
				PosLog.i("qiuyi", "index=====>" + index);
				if (index == 1)
					continue;

				if (bitMap.getBit(index - 1) != 1)
					continue;
				offset += subByte(bts, offset, field);
//				PosLog.i("qiuyi", "offset==============================>" + offset);
				returnMap.put(field.getId(), field.getValue());

				continue;
			}
			offset += subByte(bts, offset, field);
			PosLog.i("qiuyi", "offset=====>" + offset);
			returnMap.put(field.getId(), field.getValue());
			if (!(field.getId().equalsIgnoreCase("bitmap")))
				continue;
			System.out.println("获取到Bitmap数据/n");
			hasBitMap = true;
			bitMap = BitMap.addBits(field.getByteValue());
		}
		PosLog.i("qiuyi", "Map<String, String> unpack结束");
		return returnMap;
	}

	// BINARY, CHAR, NUMERIC, LLVAR, LLLVAR, LLVAR_NUMERIC, LLLVAR_NUMERIC,
	// LLBINARY, LLLBINARY, LLTRACK, LLLTRACK;
	private int subByte(byte[] bts, int offset, IsoField field)
			throws UnsupportedEncodingException {
		PosLog.i("qiuyi", "subByte开始");
		byte[] val = null;
		int length = field.getLength();
		PosLog.i("qiuyi", "field.getType()===>" + field.getType());
		switch (field.getIsoType()) {
			case BINARY:
				Log.i("qiuyi", "case 1");
			case CHAR:
				Log.i("qiuyi", "case 2");
			case NUMERIC:
				Log.i("qiuyi", "case 3");
				val = new byte[field.getLength()];
				System.out.println(field.getId());
				System.arraycopy(bts, offset, val, 0, length);
				break;
			case LLVAR_NUMERIC:
				PosLog.i("qiuyi", "case 6");
				byte[] lllnumerLen = new byte[2];
				lllnumerLen[0] = bts[offset];
				lllnumerLen[1] = bts[(offset + 1)];
				int first3Len = 0;
				if (field.getLengthType().trim().equals("hex")) {
					first3Len = (lllnumerLen[0] * 255 + lllnumerLen[1]) / 2
							+ (lllnumerLen[0] * 255 + lllnumerLen[1]) % 2;
				} else {
					first3Len = Integer.valueOf(lllnumerLen[0]) * 10
							+ Integer.valueOf(lllnumerLen[1]);
					// first3Len = zhuanhuan / 2 + zhuanhuan % 2;

				}
				val = new byte[first3Len];
				System.arraycopy(bts, offset + 2, val, 0, first3Len);
				length = 2 + first3Len;
				break;
			case LLVAR:
				PosLog.i("qiuyi", "case 4");
			case LLBINARY:
				byte[] llvarLen = new byte[2];

				llvarLen[0] = bts[offset];
				llvarLen[1] = bts[offset + 1];
				int firstLen = 0;
				if (field.getLengthType().trim().equals("hex"))
					firstLen = llvarLen[0];
				else
					firstLen = Integer.valueOf(llvarLen[0]) * 10
							+ Integer.valueOf(llvarLen[1]);

				val = new byte[firstLen];
				System.arraycopy(bts, offset + 2, val, 0, firstLen);
				length = 2 + firstLen;
				break;

			case LLLVAR:
				PosLog.i("qiuyi", "case 5");
			case LLLBINARY:
				PosLog.i("qiuyi", "case 9");
				byte[] lllvarLen = new byte[3];
				lllvarLen[0] = bts[offset];
				lllvarLen[1] = bts[(offset + 1)];
				lllvarLen[2] = bts[(offset + 2)];
				Log.i("qiuyi", "位置1");
				int first2Len = 0;
				if (field.getLengthType().trim().equals("hex"))
					first2Len = lllvarLen[0] * 255 + lllvarLen[1];
				else first2Len = Integer.valueOf(lllvarLen[0]) * 100
						+ Integer.valueOf(lllvarLen[1]) * 10
						+ Integer.valueOf(lllvarLen[2]);
				PosLog.i("qiuyi", "位置2    第一位 Integer.valueOf(lllvarLen[0])==>"+Integer.valueOf(lllvarLen[0]));
				PosLog.i("qiuyi", "位置2    第二位 Integer.valueOf(lllvarLen[1])==>"+Integer.valueOf(lllvarLen[1]));
				PosLog.i("qiuyi", "位置2    第三位 Integer.valueOf(lllvarLen[2])==>"+Integer.valueOf(lllvarLen[2]));
				PosLog.i("qiuyi", "位置2    first2Len=====》"+first2Len);
				val = new byte[first2Len];
				System.arraycopy(bts, offset + 3, val, 0, first2Len);
				PosLog.i("qiuyi", "位置3");
				length = 3 + first2Len;
				break;
			case LLLVAR_NUMERIC:
				PosLog.i("qiuyi", "case 7");
				byte[] lllnumerLen_1 = new byte[3];
				lllnumerLen_1[0] = bts[offset];
				lllnumerLen_1[1] = bts[(offset + 1)];
				lllnumerLen_1[2] = bts[(offset + 2)];
				int first3Len_1 = 0;
				if (field.getLengthType().trim().equals("hex")) {
					first3Len_1 = (lllnumerLen_1[0] * 255 + lllnumerLen_1[1]) / 2
							+ (lllnumerLen_1[0] * 255 + lllnumerLen_1[1]) % 2;
				} else {
					first3Len_1 = Integer.valueOf(lllnumerLen_1[0]) * 100
							+ Integer.valueOf(lllnumerLen_1[1]) * 10
							+ Integer.valueOf(lllnumerLen_1[2]);
					PosLog.i("qiuyi", "Integer.valueOf(lllnumerLen_1[0])======>"
							+ Integer.valueOf(lllnumerLen_1[0]));
					PosLog.i("qiuyi", "Integer.valueOf(lllnumerLen_1[1]))======>"
							+ Integer.valueOf(lllnumerLen_1[1]));
					PosLog.i("qiuyi", "Integer.valueOf(lllnumerLen_1[2])======>"
							+ Integer.valueOf(lllnumerLen_1[2]));
					PosLog.i("qiuyi", "first3Len_1======>" + first3Len_1);
				}
				val = new byte[first3Len_1];
				System.arraycopy(bts, offset + 3, val, 0, first3Len_1);
				length = 3 + first3Len_1;
				break;
			case LLTRACK:
				PosLog.i("qiuyi", "case 10");
				byte[] lltrackLen = new byte[1];
				lltrackLen[0] = bts[offset];

				int first4Len = 0;
				if (field.getLengthType().trim().equals("hex"))
					first4Len = lltrackLen[0];
				else
					first4Len = Integer.valueOf(EncodeUtil.hex(lltrackLen))
							.intValue();

				val = new byte[first4Len];
				System.arraycopy(bts, offset + 1, val, 0, first4Len);
				length = 1 + first4Len;
				break;
			case LLLTRACK:
				PosLog.i("qiuyi", "case 11");
				byte[] llltrackLen = new byte[2];
				llltrackLen[0] = bts[offset];
				llltrackLen[1] = bts[(offset + 1)];
				int first5Len = 0;
				if (field.getLengthType().trim().equals("hex"))
					first5Len = llltrackLen[0] * 255 + llltrackLen[1];
				else
					first5Len = Integer.valueOf(EncodeUtil.hex(llltrackLen))
							.intValue();

				val = new byte[first5Len];
				System.arraycopy(bts, offset + 2, val, 0, first5Len);
				length = 2 + first5Len;
		}

		System.out.println("subByte:"
				+ BCDHelper.hex2DebugHexString(val, val.length) + "\n"
				+ field.toString() + "\n");
		field.setByteValue(val);
//		Log.i("qiuyi",
//				"subByte:" + BCDHelper.hex2DebugHexString(val, val.length)
//						+ "\n" + field.toString() + "length====>" + length
//						+ "\n");
		return length;
	}

	// BINARY, CHAR, NUMERIC, LLVAR, LLLVAR, LLVAR_NUMERIC, LLLVAR_NUMERIC,
	// LLBINARY, LLLBINARY, LLTRACK, LLLTRACK;
	public byte[] merge(IsoPackage isoPackage) throws IOException {
		ByteArrayOutputStream byteOutPut = new ByteArrayOutputStream(300);
		System.out.println("merge(IsoPackage isoPackage)\n");
		for (Iterator<IsoField> localIterator = isoPackage.iterator(); localIterator
				.hasNext();) {
			IsoField field = (IsoField) localIterator.next();
			if (!(field.isChecked()))
				continue;
			System.out.print("组包：" + field.toString() + "\n");
			PosLog.i("qiuyi", "组包：" + field.toString() + "\n");
			switch (field.getIsoType()) {
				case LLVAR_NUMERIC:
					PosLog.i("qiuyi",
							field.getId() + "==============>" + field.getLength());
					byte[] lengthByte_LLVAR_NUMERIC = new byte[2];
					if (field.getLengthType().trim().equals("hex"))
						lengthByte_LLVAR_NUMERIC[0] = (byte) field.getLength();
					else {
						// lengthByte0 = EncodeUtil.bcd(field.getLength(), 1);
						PosLog.i("qiuyi", field.getLength() + "<=============="
								+ field.getLength());
						int lllength = field.getLength()/2;
						int llfristnum = lllength / 10;
						int llsecondnum = lllength % 10;
						byte[] fristnum = str2cbcd(llfristnum + "");
						byte[] secondnum = str2cbcd(llsecondnum + "");
						lengthByte_LLVAR_NUMERIC[0] = fristnum[0];
						lengthByte_LLVAR_NUMERIC[1] = secondnum[0];
					}
					byteOutPut.write(lengthByte_LLVAR_NUMERIC);
					System.out.println("LL设置包长度:" + EncodeUtil.hex(lengthByte_LLVAR_NUMERIC)
							+ "\n");
					PosLog.i("qiuyi", "LL设置包长度:" + EncodeUtil.hex(lengthByte_LLVAR_NUMERIC) + "\n");
					break;
				case LLVAR:
					PosLog.i("qiuyi", field.getId());
				case LLBINARY:
					PosLog.i("qiuyi",
							field.getId() + "==============>" + field.getLength());
					byte[] lengthByte_LLVAR = new byte[2];
					if (field.getLengthType().trim().equals("hex"))
						lengthByte_LLVAR[0] = (byte) field.getLength();
					else {
						// lengthByte0 = EncodeUtil.bcd(field.getLength(), 1);
						PosLog.i("qiuyi", field.getLength() + "<=============="
								+ field.getLength());
						int lllength = field.getLength();
						int llfristnum = lllength / 10;
						int llsecondnum = lllength % 10;
						byte[] fristnum = str2cbcd(llfristnum + "");
						byte[] secondnum = str2cbcd(llsecondnum + "");
						lengthByte_LLVAR[0] = fristnum[0];
						lengthByte_LLVAR[1] = secondnum[0];
					}
					byteOutPut.write(lengthByte_LLVAR);
					System.out.println("LL设置包长度:" + EncodeUtil.hex(lengthByte_LLVAR)
							+ "\n");
					PosLog.i("qiuyi", "LL设置包长度:" + EncodeUtil.hex(lengthByte_LLVAR) + "\n");
					break;
				case LLLVAR_NUMERIC:
					PosLog.i("qiuyi", field.getId());
					byte[] lengthByte_LLLVAR_NUMERIC = new byte[3];
					if (field.getLengthType().trim().equals("hex")) {
						lengthByte_LLLVAR_NUMERIC[0] = (byte) (field.getLength() / 255);
						lengthByte_LLLVAR_NUMERIC[1] = (byte) (field.getLength() % 255);
					} else {
						// lengthByte1 = EncodeUtil.bcd(field.getLength(), 3);
						int llllength = field.getLength() / 2;
						int lllfristnum = llllength / 100;

						int lllsecondnum = (llllength - lllfristnum * 100) / 10;
						int lllthirdnum = (llllength - lllfristnum * 100) % 10;
						byte[] fristnum = str2cbcd(lllfristnum + "");
						byte[] secondnum = str2cbcd(lllsecondnum + "");
						byte[] thirdnum = str2cbcd(lllthirdnum + "");
						lengthByte_LLLVAR_NUMERIC[0] = fristnum[0];
						lengthByte_LLLVAR_NUMERIC[1] = secondnum[0];
						lengthByte_LLLVAR_NUMERIC[2] = thirdnum[0];
					}
					byteOutPut.write(lengthByte_LLLVAR_NUMERIC);
					PosLog.i("qiuyi", "LLL设置包长度:" + EncodeUtil.hex(lengthByte_LLLVAR_NUMERIC) + "\n");
					System.out.println("LLL设置包长度:" + EncodeUtil.hex(lengthByte_LLLVAR_NUMERIC)
							+ "\n");
					break;


				case LLLVAR:
					PosLog.i("qiuyi", field.getId());
				case LLLBINARY:
					PosLog.i("qiuyi", field.getId());
					byte[] lengthByte_LLLVAR = new byte[3];
					if (field.getLengthType().trim().equals("hex")) {
						lengthByte_LLLVAR[0] = (byte) (field.getLength() / 255);
						lengthByte_LLLVAR[1] = (byte) (field.getLength() % 255);
					} else {
						// lengthByte1 = EncodeUtil.bcd(field.getLength(), 3);
						int llllength = field.getLength();
						int lllfristnum = llllength / 100;

						int lllsecondnum = (llllength - lllfristnum * 100) / 10;
						int lllthirdnum = (llllength - lllfristnum * 100) % 10;
						byte[] fristnum = str2cbcd(lllfristnum + "");
						byte[] secondnum = str2cbcd(lllsecondnum + "");
						byte[] thirdnum = str2cbcd(lllthirdnum + "");
						lengthByte_LLLVAR[0] = fristnum[0];
						lengthByte_LLLVAR[1] = secondnum[0];
						lengthByte_LLLVAR[2] = thirdnum[0];
					}
					byteOutPut.write(lengthByte_LLLVAR);
					PosLog.i("qiuyi", "LLL设置包长度:" + EncodeUtil.hex(lengthByte_LLLVAR) + "\n");
					System.out.println("LLL设置包长度:" + EncodeUtil.hex(lengthByte_LLLVAR)
							+ "\n");
					break;
				case LLTRACK:
					PosLog.i("qiuyi", field.getId());
					byte[] lengthTrack2 = new byte[1];
					if (field.getLengthType().trim().equals("hex"))
						lengthTrack2[0] = (byte) (field.getLength() / 2);
					else
						lengthTrack2 = EncodeUtil.bcd(field.getLength() / 2, 1);

					byteOutPut.write(lengthTrack2);
					System.out.println("2磁道设置包长度:" + EncodeUtil.hex(lengthTrack2)
							+ "\n");
					break;
				case LLLTRACK:
					PosLog.i("qiuyi", field.getId());
					byte[] lengthByte3_LLLTRACK = new byte[2];
					if (field.getLengthType().trim().equals("hex")) {
						lengthByte3_LLLTRACK[0] = (byte) (field.getLength() / 2 / 255);
						lengthByte3_LLLTRACK[1] = (byte) (field.getLength() / 2 % 255);
					} else {
						lengthByte3_LLLTRACK = EncodeUtil.bcd(field.getLength() / 2, 2);
					}
					byteOutPut.write(lengthByte3_LLLTRACK);
					System.out.println("3磁道打包设置包长度:" + EncodeUtil.hex(lengthByte3_LLLTRACK)
							+ "\n");
				case BINARY:
					PosLog.i("qiuyi", field.getId());
					break;
				case CHAR:
					PosLog.i("qiuyi", field.getId());
					break;
				case NUMERIC:
					PosLog.i("qiuyi", field.getId());
					break;
				default:
					break;
			}

			System.out.println("设置数据:========>"
					+ EncodeUtil.hex(field.getByteValue()) + "\n");
			PosLog.i("qiuyi",
					"getByteValue()设置数据:=======>" + EncodeUtil.hex(field.getByteValue())
							+ "\n");
			PosLog.i("qiuyi", "getValue设置数据:=======>" + field.getValue() + "\n");

			// if (field.getId().equalsIgnoreCase("bitmap")) {
			// byte[] bitmapByte = field.getByteValue();
			// byte[] bitMapByteMore = new byte[] { 00, 00, 00, 00, 00, 00,
			// 00, 00 };
			// byte[] bitMapByteNew = new byte[bitmapByte.length
			// + bitMapByteMore.length];
			// System.arraycopy(bitmapByte, 0, bitMapByteNew, 0,
			// bitmapByte.length);
			// System.arraycopy(bitMapByteMore, 0, bitMapByteNew,
			// bitmapByte.length, bitMapByteMore.length);
			// byteOutPut.write(bitMapByteNew);
			// } else
			byteOutPut.write(field.getByteValue());

		}

		byte[] beforeSend = byteOutPut.toByteArray();
		byte[] bts = new byte[beforeSend.length];
		System.arraycopy(beforeSend, 0, bts, 0, beforeSend.length);
		PosLog.i("qiuyi", "bts=======>:" + EncodeUtil.hex(bts) + "\n");
		return bts;
	}

	public byte[] merge(IsoPackage isoPackage, String start, String end)
			throws IOException {
		ByteArrayOutputStream byteOutPut = new ByteArrayOutputStream(100);
//		System.out
//				.println("merge(IsoPackage isoPackage, String start, String end)\n");
//		System.out.println("start = " + start + " end = " + end + "\n");

		List<String> keys = getMacList(isoPackage, start, end);
		for (int i = 0; i < keys.size(); ++i) {
			System.out.println("第" + (i + 1) + "个key = "
					+ ((String) keys.get(i)) + "\n");
		}

		for (Iterator<IsoField> localIterator = isoPackage.iterator(); localIterator
				.hasNext();) {
			IsoField field = (IsoField) localIterator.next();
			if ((field.isChecked()) && (keys.contains(field.getId()))) {
//				System.out.println("是需要打包的key:" + field.getId() + "\n");
				switch (field.getIsoType().ordinal()) {
					case 4:
					case 6:
					case 8:
						byte[] lengthByte0 = new byte[1];
						if (field.getLengthType().trim().equals("hex"))
							lengthByte0[0] = (byte) field.getLength();
						else
							lengthByte0 = EncodeUtil.bcd(field.getLength(), 1);

						byteOutPut.write(lengthByte0);
						break;
					case 5:
					case 7:
					case 9:
						byte[] lengthByte1 = new byte[2];
						if (field.getLengthType().trim().equals("hex")) {
							lengthByte1[0] = (byte) (field.getLength() / 255);
							lengthByte1[1] = (byte) (field.getLength() % 255);
						} else {
							lengthByte1 = EncodeUtil.bcd(field.getLength(), 2);
						}
						byteOutPut.write(lengthByte1);
						break;
					case 10:
						byte[] lengthTrack2 = new byte[1];
						if (field.getLengthType().trim().equals("hex"))
							lengthTrack2[0] = (byte) (field.getLength() / 2);
						else
							lengthTrack2 = EncodeUtil.bcd(field.getLength() / 2, 1);

						byteOutPut.write(lengthTrack2);
						System.out.println("2磁道设置包长度:"
								+ EncodeUtil.hex(lengthTrack2) + "\n");
						break;
					case 11:
						byte[] lengthByte2 = new byte[2];
						if (field.getLengthType().trim().equals("hex")) {
							lengthByte2[0] = (byte) (field.getLength() / 2 / 255);
							lengthByte2[1] = (byte) (field.getLength() / 2 % 255);
						} else {
							lengthByte2 = EncodeUtil.bcd(field.getLength() / 2, 2);
						}

						byteOutPut.write(lengthByte2);
						System.out.println("3磁道打包设置包长度:"
								+ EncodeUtil.hex(lengthByte2) + "\n");
				}

//				System.out.println(field.getId() + ":"
//						+ EncodeUtil.hex(field.getByteValue()));

				byteOutPut.write(field.getByteValue());
				break;
			}
//			System.out.println("不是需要打包的key:" + field.getId() + "\n");
		}

		byte[] beforeSend = byteOutPut.toByteArray();
		byte[] bts = new byte[beforeSend.length];
		System.arraycopy(beforeSend, 0, bts, 0, beforeSend.length);
		return bts;
	}

	private List<String> getMacList(IsoPackage isoPackage, String start,
									String end) {
		List<String> keys = new ArrayList<String>();
		boolean ifstart = false;
		for (int i = 0; i < isoPackage.size(); ++i)
			if (((IsoField) isoPackage.get(i)).getId().equals(start)) {
				ifstart = true;
				keys.add(((IsoField) isoPackage.get(i)).getId());
			} else if (ifstart) {
				keys.add(((IsoField) isoPackage.get(i)).getId());
				if (((IsoField) isoPackage.get(i)).getId().equals(end))
					break;
			}

		return keys;
	}

	protected abstract byte[] msgLength(int paramInt);

	public void addBitmap(Map<String, String> dataMap, IsoPackage pack)
			throws UnsupportedEncodingException {
		List<Integer> dataFieldList = new ArrayList<Integer>(dataMap.size());
		for (Iterator<String> localIterator = dataMap.keySet().iterator(); localIterator
				.hasNext();) {
			String key = (String) localIterator.next();

			if (!(SimpleUtil.isNumeric(key)))
				break;
			int val = Integer.valueOf(key).intValue();
			if ((pack.isBit64()) && (val > 64)) {
				pack.setBit64(false);

				dataFieldList.add(Integer.valueOf(1));
			}
			dataFieldList.add(Integer.valueOf(val));
		}

		BitMap bitMap = null;
		if (pack.isBit64())
			bitMap = new BitMap(64);
		else
			bitMap = new BitMap(128);

		byte[] bitMapByte = bitMap.addBits(dataFieldList);

		pack.getIsoField("bitmap").setByteValue(bitMapByte);
	}

	public static byte[] str2cbcd(String s) {
		if (s.length() % 2 != 0) {
			s = "0" + s;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		char[] cs = s.toCharArray();
		for (int i = 0; i < cs.length; i += 2) {
			// int high = cs[i] - 48;
			int low = cs[i + 1] - 48;
			baos.write(low);
		}
		return baos.toByteArray();
	}
}
