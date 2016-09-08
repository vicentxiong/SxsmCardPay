package com.mwdev.sxsmcardpay.iso8583;

public class IsoMsgFactory extends AbstractIsoMsgFactory {
	private static IsoMsgFactory isoMsgFactory = new IsoMsgFactory();

	public static IsoMsgFactory getInstance() {
		return isoMsgFactory;
	}

	protected byte[] msgLength(int length) {
		byte[] rByte = new byte[2];
		rByte[0] = (byte) (length / 255);
		rByte[1] = (byte) (length % 255);
		return rByte;
	}
}