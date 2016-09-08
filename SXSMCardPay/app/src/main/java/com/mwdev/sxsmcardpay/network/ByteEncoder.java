package com.mwdev.sxsmcardpay.network;

import android.util.Log;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Created by xiongxin on 16-8-15.
 */
public class ByteEncoder extends ProtocolEncoderAdapter {
    /**
     * Encodes higher-level message objects into binary or protocol-specific data.
     * MINA invokes {@link #encode(IoSession, Object, ProtocolEncoderOutput)}
     * method with message which is popped from the session write queue, and then
     * the encoder implementation puts encoded messages (typically {@link }s)
     * into {@link ProtocolEncoderOutput}.
     *
     * @param session The current Session
     * @param message the message to encode
     * @param out     The {@link ProtocolEncoderOutput} that will receive the encoded message
     * @throws Exception if the message violated protocol specification
     */
    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        byte[] bytes = (byte[])message;

        IoBuffer buffer = IoBuffer.allocate(256);
        buffer.setAutoExpand(true);

        buffer.put(bytes);
        buffer.flip();

        out.write(buffer);
        out.flush();

        buffer.free();
    }
}
