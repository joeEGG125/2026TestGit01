package com.syscom.fep.gateway.netty;

import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.vo.communication.BaseCommu;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 解碼從FEP接收過來的電文
 *
 * @author Richard
 */
public class NettyTransmissionBaseCommuByteToMessageDecoder extends NettyTransmissionByteToMessageDecoder {
    private int totalLength, remainder = -1;
    private final ByteArrayOutputStream os = new ByteArrayOutputStream();

    @Override
    protected void decode(ChannelHandlerContext ctx, byte[] in, List<Object> out) throws Exception {
        try {
            // 開始從頭讀入新的電文
            if (remainder == -1) {
                int offset = 0;
                // 取出電文類型
                byte[] bytes = ArrayUtils.subarray(in, offset, offset + BaseCommu.HEADER_BASECOMMU_IDENTITY_SIZE);
                offset += BaseCommu.HEADER_BASECOMMU_IDENTITY_SIZE;
                // 如果是BaseCommu類型的電文
                if (BaseCommu.getHeaderBaseCommuIdentity().equals(ConvertUtil.toString(bytes, StandardCharsets.UTF_8))) {
                    // 4 bytes Entity Identity
                    bytes = ArrayUtils.subarray(in, offset, offset + BaseCommu.HEADER_ENTITY_IDENTITY_SIZE);
                    offset += BaseCommu.HEADER_ENTITY_IDENTITY_SIZE;
                    // 1 bytes Compressed Flag
                    bytes = ArrayUtils.subarray(in, offset, offset + BaseCommu.HEADER_COMPRESSED_FLAG);
                    offset += BaseCommu.HEADER_COMPRESSED_FLAG;
                    // 再是電文長度
                    bytes = ArrayUtils.subarray(in, offset, offset + BaseCommu.HEADER_LENGTH_SIZE);
                    offset += BaseCommu.HEADER_LENGTH_SIZE;
                    // 塞入電文Body長度
                    int length = Integer.parseInt(ConvertUtil.toString(bytes, StandardCharsets.UTF_8), 16);
                    offset += length;
                    // 塞入電文總長度
                    totalLength = offset;
                    remainder = offset - in.length;
                    // 完整的一筆A電文, 長度剛好相等, 說明電文沒有被截斷, 是完整的電文, 直接放入out
                    if (remainder == 0) {
                        bytes = ArrayUtils.subarray(in, 0, in.length);
                        out.add(NettyTransmissionUtil.toByteBuf(bytes));
                        NettyTransmissionUtil.warnMessage(ctx.channel(), "[decode new]No remaining bytes, there is only one message, length:", bytes.length, ", message:", ConvertUtil.toString(bytes, StandardCharsets.UTF_8));
                    }
                    // 不完整的An電文, 還有剩餘的部分, 則下一次再讀進來
                    else if (remainder > 0) {
                        // 剩餘的部分還沒讀完, 先存起來
                        os.write(in, 0, in.length);
                        NettyTransmissionUtil.warnMessage(ctx.channel(), "[decode new]There are more remaining bytes, remainder:", remainder, ", length:", totalLength, ", kept:", ConvertUtil.toString(os.toByteArray(), StandardCharsets.UTF_8));
                    }
                    // 完整的A電文+有可能完整或者不完整的B電文, 長度小, 說明有兩筆以上
                    else {
                        // 先取出完整的A電文, 放入out
                        bytes = ArrayUtils.subarray(in, 0, offset);
                        out.add(NettyTransmissionUtil.toByteBuf(bytes));
                        NettyTransmissionUtil.warnMessage(ctx.channel(), "[decode new]First to get one message, length:", bytes.length, ", message:", ConvertUtil.toString(bytes, StandardCharsets.UTF_8));
                        // 剩下多餘的繼續遞歸處理
                        bytes = ArrayUtils.subarray(in, offset, in.length);
                        NettyTransmissionUtil.warnMessage(ctx.channel(), "[decode new]There are more left bytes, length:", bytes.length, ", left message:", ConvertUtil.toString(bytes, StandardCharsets.UTF_8));
                        remainder = -1;
                        decode(ctx, bytes, out);
                    }
                } else {
                    out.add(NettyTransmissionUtil.toByteBuf(in));
                }
            }
            // 上一次處理完後, 有剩餘的部分, 繼續處理
            else {
                // An剩餘的部分, An剩餘的部分剛好讀完, 存入out
                if (remainder == in.length) {
                    os.write(in);
                    byte[] bytes = os.toByteArray();
                    out.add(NettyTransmissionUtil.toByteBuf(bytes));
                    NettyTransmissionUtil.warnMessage(ctx.channel(), "[decode remaining]Combine remaining bytes, remainder:", remainder, " which combined:", bytes.length, " equals to length:", totalLength, ", combined message:", ConvertUtil.toString(bytes, StandardCharsets.UTF_8));
                    os.reset();
                    remainder = -1;
                }
                // An, 剩餘還有, 則繼續讀
                else if (remainder > in.length) {
                    os.write(in);
                    remainder -= in.length;
                    NettyTransmissionUtil.warnMessage(ctx.channel(), "[decode remaining]There are more remaining bytes, remainder:", remainder, ", length:", totalLength, ", kept:", ConvertUtil.toString(os.toByteArray(), StandardCharsets.UTF_8));
                }
                // A剩餘的部分+有可能完整或者不完整的B電文, 長度小, 說明有兩筆以上
                else {
                    // 先取出A剩餘的部分, 跟上一次剩餘的合併組成完整的A電文, 放入out
                    os.write(in, 0, remainder);
                    byte[] bytes = os.toByteArray();
                    out.add(NettyTransmissionUtil.toByteBuf(bytes));
                    NettyTransmissionUtil.warnMessage(ctx.channel(), "[decode remaining]First to Combine remaining bytes, remainder:", remainder, " which combined:", bytes.length, " equals to length:", totalLength, ", combined message:", ConvertUtil.toString(bytes, StandardCharsets.UTF_8));
                    // 剩下多餘的繼續遞歸處理
                    bytes = ArrayUtils.subarray(in, remainder, in.length);
                    NettyTransmissionUtil.warnMessage(ctx.channel(), "[decode remaining]There are more left bytes, length:", bytes.length, ", left message:", ConvertUtil.toString(bytes, StandardCharsets.UTF_8));
                    remainder = -1;
                    os.reset();
                    decode(ctx, bytes, out);
                }
            }
        } catch (Exception e) {
            NettyTransmissionUtil.warnMessage(ctx.channel(), e, "[decode remaining]failed with exception occur, ", e.getMessage());
            os.reset(); // 注意這裡一定要reset
        }
    }
}
