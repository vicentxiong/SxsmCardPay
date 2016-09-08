package com.mwdev.sxsmcardpay.network;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;
import org.apache.mina.filter.codec.textline.TextLineEncoder;

import java.nio.charset.Charset;

/**
 * Created by xiongxin on 16-8-15.
 */
public class ByteCodecFactory implements ProtocolCodecFactory{
    private ByteDecoder mDecoder;
    private ByteEncoder mEncoder;

    public ByteCodecFactory(){
        mEncoder = new ByteEncoder();
        mDecoder = new ByteDecoder();
    }
    /**
     * Returns a new (or reusable) instance of {@link ProtocolEncoder} which
     * encodes message objects into binary or protocol-specific data.
     *
     * @param session The current session
     * @return The encoder instance
     * @throws Exception If an error occurred while retrieving the encoder
     */
    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return mEncoder;
    }

    /**
     * Returns a new (or reusable) instance of {@link ProtocolDecoder} which
     * decodes binary or protocol-specific data into message objects.
     *
     * @param session The current session
     * @return The decoder instance
     * @throws Exception If an error occurred while retrieving the decoder
     */
    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return mDecoder;
    }
}
