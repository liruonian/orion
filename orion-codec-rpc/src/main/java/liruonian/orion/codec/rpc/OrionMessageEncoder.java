package liruonian.orion.codec.rpc;

import java.io.Serializable;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import liruonian.orion.codec.CodecException;
import liruonian.orion.commons.StringManager;
import liruonian.orion.commons.utils.CrcUtils;
import liruonian.orion.remoting.Status;
import liruonian.orion.remoting.message.RpcMessage;
import liruonian.orion.remoting.message.RpcRequest;
import liruonian.orion.remoting.message.RpcResponse;
import liruonian.orion.remoting.message.codec.MessageEncoder;
import liruonian.orion.remoting.serializer.Serializer;
import liruonian.orion.remoting.serializer.Serializers;
import liruonian.orion.rpc.MarkerByte;

/**
 * Rpc消息编码器，将{@link RpcMessage}编码为字节流
 *
 * @author lihao
 * @date 2020年8月14日
 * @version 1.0
 */
public class OrionMessageEncoder implements MessageEncoder {

    private static final StringManager sm = StringManager.getManager(Constants.PACKAGE);

    @Override
    public void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out)
            throws CodecException {
        if (msg instanceof RpcMessage) {
            // crc校验的起始位
            int startIndex = out.writerIndex();

            RpcMessage message = (RpcMessage) msg;

            // 写出协议位(magic & version)
            out.writeByte(OrionProtocol.PROTOCOL_CODE);
            out.writeByte(OrionProtocol.PROTOCOL_VERSION);

            // 根据消息的值生成实际的标志位字节
            MarkerByte marker = MarkerByte.from(message.isReqOrRsp(),
                    message.isOneway(), message.isHeartbeat(), message.isCrcRequired(),
                    message.getSerializationType());
            out.writeByte(marker.getActualByte());

            // 写消息状态，注意请求消息并没有状态字段，此处填充Status.PADDING
            byte status = Status.PADDING.getValue();
            if (message instanceof RpcResponse) {
                status = ((RpcResponse) message).getStatus().getValue();
            }
            out.writeByte(status);

            // 消息唯一标识
            out.writeLong(message.getId());

            // 获取待序列化的对象
            Object seralizableObject = null;
            if (message instanceof RpcRequest) {
                seralizableObject = ((RpcRequest) message).getRequestBody();
            } else if (message instanceof RpcResponse) {
                seralizableObject = ((RpcResponse) message).getResponseBody();
            }

            // 当待序列化的对象为null时，data length为0，否则正常序列化处理
            if (seralizableObject == null) {
                out.writeInt(0);
            } else {
                Serializer serializer = Serializers
                        .getSerializer(marker.getSerializationType());
                if (serializer == null) {
                    throw new CodecException(
                            sm.getString("orionMessageEncoder.encode.noSerializerFound",
                                    String.valueOf(marker.getSerializationType())));
                }

                byte[] data = serializer.serialize(seralizableObject);
                out.writeInt(data.length);
                out.writeBytes(data);
            }

            // 计算crc32
            if (message.isCrcRequired()) {
                byte[] frame = new byte[out.readableBytes()];
                out.getBytes(startIndex, frame);

                int crc = CrcUtils.crc32(frame);
                out.writeInt(crc);
            }
        }
    }

}
