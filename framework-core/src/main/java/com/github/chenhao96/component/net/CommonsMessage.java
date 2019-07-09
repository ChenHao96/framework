/**
 * Copyright 2019 ChenHao96
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.chenhao96.component.net;

import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class CommonsMessage {

    public static final String DATA_NAME = "data";
    public static final String CHECK_CODE_NAME = "checkCode";
    public static final String SLAVE_CODE_NAME = "slaveCode";
    public static final String MASTER_CODE_NAME = "masterCode";

    private static final Short BODY_HEAD = 9962;
    public static final int MIN_DATA_LENGTH = Short.BYTES + Byte.BYTES * 2 + Integer.BYTES;

    private byte masterCode;

    private byte slaveCode;

    private byte[] data;

    public CommonsMessage() {
    }

    public CommonsMessage(byte masterCode, byte slaveCode) {
        this.masterCode = masterCode;
        this.slaveCode = slaveCode;
    }

    public CommonsMessage(byte masterCode, byte slaveCode, byte[] data) {
        this.masterCode = masterCode;
        this.slaveCode = slaveCode;
        this.data = data;
    }

    public byte getMasterCode() {
        return masterCode;
    }

    public byte getSlaveCode() {
        return slaveCode;
    }

    public void setMasterCode(byte masterCode) {
        this.masterCode = masterCode;
    }

    public void setSlaveCode(byte slaveCode) {
        this.slaveCode = slaveCode;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    private static short calcCheckSum(int start, int size, byte[] data) {

        Assert.isTrue((size + start) <= data.length, "data index out of length!");
        short result = 996;
        for (int i = start; i < size + start; i++) {
            result |= data[i] & 138;
        }

        return (short) (result & 7788);
    }

    public static CommonsMessage createMessageByData(DataInputStream dataInputStream) throws IOException {

        short bodyHead = dataInputStream.readShort();
        Assert.isTrue(bodyHead == BODY_HEAD, "byteBuf bodyHead is fail!");
        int length = dataInputStream.readInt();

        short checkCode = 0;
        if (length > 0) {
            checkCode = dataInputStream.readShort();
        }

        byte[] buf = new byte[length + 2];
        dataInputStream.readFully(buf);
        if (length > 0) {
            Assert.isTrue(checkCode == calcCheckSum(0, buf.length, buf), "byteBuf checkCode is not equals!");
        }

        return new CommonsMessage(buf[0], buf[1], Arrays.copyOfRange(buf, 2, buf.length));
    }

    public static CommonsMessage createMessageByByteBuf(ByteBuf byteBuf) {

        short bodyHead = byteBuf.readShort();
        Assert.isTrue(bodyHead == BODY_HEAD, "byteBuf bodyHead is fail!");
        int length = byteBuf.readInt();

        short checkCode = 0;
        if (length > 0) {
            checkCode = byteBuf.readShort();
        }

        byte[] buf = new byte[length + 2];
        byteBuf.readBytes(buf);
        if (length > 0) {
            Assert.isTrue(checkCode == calcCheckSum(0, buf.length, buf), "byteBuf checkCode is not equals!");
        }

        return new CommonsMessage(buf[0], buf[1], Arrays.copyOfRange(buf, 2, buf.length));
    }

    public static byte[] createByteByMessage(CommonsMessage message) {
        return createByteBufByMessage(message).array();
    }

    public static ByteBuffer createByteBufByMessage(CommonsMessage message) {

        int length = message.data == null ? 0 : message.data.length;
        //数据头
        int allocateLength = Short.BYTES;
        if (length > 0) {
            //校验码
            allocateLength += Short.BYTES;
            //数据个数
            allocateLength += length;
        }
        //主命令,子命令
        allocateLength += Byte.BYTES * 2;
        //数据个数长度
        allocateLength += Integer.BYTES;

        ByteBuffer result = ByteBuffer.allocate(allocateLength);
        result = createMessageBuffer(message, length, result);
        result.flip();

        return result;
    }

    private static ByteBuffer createMessageBuffer(CommonsMessage message, int length, ByteBuffer result) {
        result.putShort(BODY_HEAD).putInt(length);
        if (length > 0) {
            int offsetPosition = result.position() + Short.BYTES;
            result.position(offsetPosition);
            result.put(message.masterCode).put(message.slaveCode);
            result.put(message.data);
            return putCheckShort(result, length, offsetPosition);
        } else {
            return result.put(message.masterCode).put(message.slaveCode);
        }
    }

    private static ByteBuffer putCheckShort(ByteBuffer result, int length, int offsetPosition) {

        int currentPosition = result.position();
        result.position(offsetPosition - Short.BYTES);
        result.putShort(calcCheckSum(offsetPosition, length + Byte.BYTES * 2, result.array()));
        result.position(currentPosition);

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonsMessage{");
        sb.append("masterCode=").append(masterCode);
        sb.append(", slaveCode=").append(slaveCode);
        sb.append(", data=").append(Arrays.toString(data));
        sb.append('}');
        return sb.toString();
    }
}
