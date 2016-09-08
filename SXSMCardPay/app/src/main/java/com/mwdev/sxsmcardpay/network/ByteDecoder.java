package com.mwdev.sxsmcardpay.network;

import android.util.Log;

import com.mwdev.sxsmcardpay.util.PosUtil;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * Created by xiongxin on 16-8-15.
 */
public class ByteDecoder implements ProtocolDecoder {
    /**
     * Decodes binary or protocol-specific content into higher-level message objects.
     * MINA invokes {@link #decode(IoSession, IoBuffer, ProtocolDecoderOutput)}
     * method with read data, and then the decoder implementation puts decoded
     * messages into {@link ProtocolDecoderOutput}.
     *
     * @param session The current Session
     * @param in      the buffer to decode
     * @param out     The {@link ProtocolDecoderOutput} that will receive the decoded message
     * @throws Exception if the read data violated protocol specification
     */
    @Override
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        int limit = in.limit();
        byte[] bytes = new byte[limit];

        in.get(bytes);
        String hex = PosUtil.byteArray2Hex(bytes);

        out.write(hex);
    }

    /**
     * Invoked when the specified <tt>session</tt> is closed.  This method is useful
     * when you deal with the protocol which doesn't specify the length of a message
     * such as HTTP response without <tt>content-length</tt> header. Implement this
     * method to process the remaining data that {@link #decode(IoSession, IoBuffer, ProtocolDecoderOutput)}
     * method didn't process completely.
     *
     * @param session The current Session
     * @param out     The {@link ProtocolDecoderOutput} that contains the decoded message
     * @throws Exception if the read data violated protocol specification
     */
    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {

    }

    /**
     * Releases all resources related with this decoder.
     *
     * @param session The current Session
     * @throws Exception if failed to dispose all resources
     */
    @Override
    public void dispose(IoSession session) throws Exception {

    }
}
