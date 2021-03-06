package com.Zaseth;

import java.io.UTFDataFormatException;
import java.nio.charset.Charset;
import java.util.*;

public class ByteArrayJava {
    
    private byte[] data;
    
    private int position;
    private int nullBytes;
    
    private int BUFFER_SIZE = 1024;
    
    private boolean endian;
    private boolean BIG_ENDIAN = true;
    private boolean LITTLE_ENDIAN = false;
    
    /*
    Constructor
     */
    public ByteArrayJava(ByteArrayJava buff) {
        if (buff == null) {
            throw new IllegalArgumentException("Can't read from empty byte stream");
        }
        if (buff instanceof ByteArrayJava) {
            this.data = buff.data;
        }
        this.position = 0;
        this.nullBytes = 0;
        this.endian = this.BIG_ENDIAN;
    }
    
    public ByteArrayJava(int length) {
        this.data = new byte[length];
        this.position = 0;
        this.nullBytes = 0;
        this.endian = this.BIG_ENDIAN;
    }
    
    public ByteArrayJava() {
        this.data = new byte[this.BUFFER_SIZE];
        this.position = 0;
        this.nullBytes = 0;
        this.endian = this.BIG_ENDIAN;
    }
    
    /*
    Set | Get & Constructor
     */
    public void clear() {
        this.position = 0;
        this.data = new byte[this.BUFFER_SIZE];
    }
    public void clear(byte[] data, int position) {
        this.data = data;
        this.position = position;
    }
    
    public void setEndian(boolean e) { this.endian = e; }
    public boolean getEndian() { return this.endian; }
    
    public int moveLeft(int v) { return this.position -= v; }
    public int moveRight(int v) { return this.position += v; }
    
    public void grow(int what, int by) { this.BUFFER_SIZE = Math.max(this.position + by, this.BUFFER_SIZE); }
    public int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    
    public int length() { return this.data == null ? 0 : this.data.length; }
    
    public int bytesAvailable() {
        int value = this.length() - this.position;
        if (value > this.length() || value < 0) {
            return 0;
        }
        if (this.position >= this.length()) {
            return -1;
        }
        return value;
    }
    
    public void swap(int n, int m) {
        byte i = this.data[n];
        this.data[n] = this.data[m];
        this.data[m] = i;
    }
    public byte[] swap16() {
        int length = this.length();
        if (length % 2 != 0) {
            throw new IllegalArgumentException("Byte stream size must be a multiple of 16-bits");
        }
        for (int i = 0; i < length; i += 2) {
            this.swap(i, i + 1);
        }
        return this.data;
    }
    public byte[] swap32() {
        int length = this.length();
        if (length % 4 != 0) {
            throw new IllegalArgumentException("Byte stream size must be a multiple of 32-bits");
        }
        for (int i = 0; i < length; i += 4) {
            this.swap(i, i + 3);
            this.swap(i + 1, i + 2);
        }
        return this.data;
    }
    public byte[] swap64() {
        int length = this.length();
        if (length % 8 != 0) {
            throw new IllegalArgumentException("Byte stream size must be a multiple of 64-bits");
        }
        for (int i = 0; i < length; i += 8) {
            this.swap( i, i + 7);
            this.swap( i + 1, i + 6);
            this.swap( i + 2, i + 5);
            this.swap( i + 3, i + 4);
        }
        return this.data;
    }
    
    /*
    Data retrieval
     */
    @Override
    public String toString() {
        boolean showFullArray = false;
        if (showFullArray) {
            return this.Debug("Bytes available: " + this.bytesAvailable() + "\r\nPosition: " + this.position + "\r\nNullbytes: " + this.nullBytes + "\r\nByte stream: " + Arrays.toString(this.data)).substring(0, 120);
        } else {
            return this.Debug("Bytes available: " + this.bytesAvailable() + "\r\nPosition: " + this.position + "\r\nNullbytes: " + this.nullBytes + "\r\nByte stream: " + Arrays.toString(this.data));
        }
    }
    public String Debug(String debugMessage) { return "<DEBUG>\r\n" + debugMessage + "\r\n</DEBUG>"; }
    
    /*
    ByteArray as3
     */
    public byte atomicCompareAndSwapIntAt(int byteIndex, int expectedValue, int newValue) {
        byte value = this.data[byteIndex];
        if (value == expectedValue) {
            this.data[byteIndex] = (byte) newValue;
        }
        return value;
    }
    public int atomicCompareAndSwapLength(int expectedLength, int newLength) {
        int prevLength = this.length();
        if (prevLength != expectedLength) {
            return prevLength;
        } else if (prevLength < newLength) {
            this.data = new byte[this.length() + (newLength - prevLength)];
            int i = 0;
            for (Byte b : new ArrayList<byte[]>(Arrays.asList(this.data)).toArray(new Byte[this.length() + (newLength - prevLength)])) {
                this.data[i++] = b.byteValue();
            }
        } else if (prevLength > newLength) {
            this.data = Arrays.copyOfRange(this.data, newLength - 1, prevLength - 1);
        }
        return prevLength;
    }
    
    /*
    Check
     */
    private void checkInt(int value, int offset, int ext, int max, int min) {
        this.bytesAvailable();
        if (value > max || value < min) {
            throw new ArrayIndexOutOfBoundsException("Value argument is out of bounds");
        }
        if (offset + ext > this.length()) {
            throw new ArrayIndexOutOfBoundsException("Index argument is out of range");
        }
    }
    private void checkInt(int value, int offset, int ext, double max, double min) {
        this.bytesAvailable();
        if (value > max || value < min) {
            throw new ArrayIndexOutOfBoundsException("Value argument is out of bounds");
        }
        if (offset + ext > this.length()) {
            throw new ArrayIndexOutOfBoundsException("Index argument is out of range");
        }
    }
    private void checkOffset(int offset, int ext, int length) {
        if ((offset % 1) != 0 || offset < 0) {
            throw new IllegalArgumentException("Offset is not uint");
        }
        if (offset + ext > length) {
            throw new ArrayIndexOutOfBoundsException("Trying to access beyond buffer length");
        }
    }
    private void checkIEEE754(int offset, int ext) {
        if (offset + ext > this.length()) {
            throw new ArrayIndexOutOfBoundsException("Index out of range");
        }
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("Index out of range");
        }
    }
    
    /*
    Length calculation
     */
    public byte get7BitValueSize(int value) {
        return this.get7BitValueSize((long) value);
    }
    public static byte get7BitValueSize(long value) {
        long limit = 0x80;
        byte result = 1;
        while (value >= limit) {
            limit <<= 7;
            ++result;
        }
        return result;
    }
    
    /*
    Int & UInt
     */
    public void writeIntBE(int value, int offset, int byteLength) {
        value = +value;
        double limit = Math.pow(2, (8 * byteLength) - 1);
        this.checkInt(value, offset, byteLength, limit -1, -limit);
        int i = byteLength - 1;
        double mul = 1 * 1.0;
        int sub = 0;
        this.data[offset + i] = (byte) ((byte) value & 0xFF);
        while (--i >= 0) {
            mul *= 0x100;
            if (value < 0 && sub == 0 && this.data[offset + i + 1] != 0) {
                sub = 1;
            }
            this.data[offset + i] = (byte) (((byte) ((value / mul))) - sub & 0xFF);
        }
        this.position = offset + byteLength;
    }
    public void writeIntLE(int value, int offset, int byteLength) {
        value = +value;
        double limit = Math.pow(2, (8 * byteLength) - 1);
        this.checkInt(value, offset, byteLength, limit -1, -limit);
        int i = 0;
        double mul = 1 * 1.0;
        int sub = 0;
        this.data[offset] = (byte) ((byte) value & 0xFF);
        while (++i < byteLength) {
            mul *= 0x100;
            if (value < 0 && sub == 0 && this.data[offset + i - 1] != 0) {
                sub = 1;
            }
            this.data[offset + i] = (byte) (((byte) ((value / mul))) - sub & 0xFF);
        }
        this.position = offset + byteLength;
    }
    
    public void writeInt8(int v) {
        v = +v;
        this.checkInt(v, this.position, 1, 0x7f, -0x80);
        if (v < 0) v = 0xff + v + 1;
        this.data[this.position++] = (byte) v;
    }
    public void writeInt16(int v) {
        v = +v;
        this.checkInt(v, this.position, 2, 0x7fff, -0x8000);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) v;
        } else {
            this.data[this.position++] = (byte) v;
            this.data[this.position++] = (byte) (v >> 8);
        }
    }
    public void writeInt24(int v) {
        v = +v;
        this.checkInt(v, this.position, 3, 0x7fffff, -0x800000);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) v;
        } else {
            this.data[this.position++] = (byte) v;
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
        }
    }
    public void writeInt29(int v) {
        if (v < -0x10000000 || v > 0x0fffffff) {
            throw new IllegalArgumentException("Integer must be between -0x10000000 and 0x0fffffff but got " + v + " instead");
        }
        v += v < 0 ? 0x20000000 : 0;
        if (v > 0x1fffff) {
            v >>= 1;
            this.writeInt8(0x80 | ((v >> 21) & 0xff));
        }
        if (v > 0x3fff) {
            this.writeInt8(0x80 | ((v >> 14) & 0xff));
        }
        if (v > 0x7f) {
            this.writeInt8(0x80 | ((v >> 7) & 0xff));
        }
        if (v > 0x1fffff) {
            this.writeInt8(v & 0xff);
        } else {
            this.writeInt8(v & 0x7f);
        }
    }
    public void writeInt32(int v) {
        v = +v;
        this.checkInt(v, this.position, 4, 0x7fffffff, -0x80000000);
        if (this.endian) {
            if (v < 0) v = 0xffffffff + v + 1;
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) v;
        } else {
            this.data[this.position++] = (byte) v;
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
        }
    }
    public void writeInt40(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) v;
        } else {
            this.data[this.position++] = (byte) v;
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 32);
        }
    }
    public void writeInt48(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) v;
        } else {
            this.data[this.position++] = (byte) v;
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 40);
        }
    }
    public void writeInt56(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 48);
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) v;
        } else {
            this.data[this.position++] = (byte) v;
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 48);
        }
    }
    public void writeInt64(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 56);
            this.data[this.position++] = (byte) (v >> 48);
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) v;
        } else {
            this.data[this.position++] = (byte) v;
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 48);
            this.data[this.position++] = (byte) (v >> 56);
        }
    }
    
    public void writeUIntBE(int value, int offset, int byteLength) {
        value = +value;
        double limit = Math.pow(2, (8 * byteLength) - 1);
        this.checkInt(value, offset, byteLength, limit, 0);
        int i = byteLength - 1;
        double mul = 1 * 1.0;
        this.data[offset + i] = (byte) ((byte) value & 0xFF);
        while (--i >= 0) {
            mul *= 0x100;
            this.data[offset + i] = (byte) (((byte) ((value / mul))) & 0xFF);
        }
        this.position = offset + byteLength;
    }
    public void writeUIntLE(int value, int offset, int byteLength) {
        value = +value;
        double limit = Math.pow(2, (8 * byteLength) - 1);
        this.checkInt(value, offset, byteLength, limit, 0);
        double mul = 1 * 1.0;
        int i = 0;
        this.data[offset] = (byte) ((byte) value & 0xFF);
        while (++i < byteLength) {
            mul *= 0x100;
            this.data[offset + i] = (byte) (((byte) ((value / mul))) & 0xFF);
        }
        this.position = offset + byteLength;
    }
    
    public void writeUInt8(int v) {
        v = +v;
        this.checkInt(v, this.position, 1, 0xff, 0);
        this.data[this.position++] = (byte) (v & 0xff);
    }
    public void writeUInt16(int v) {
        v = +v;
        this.checkInt(v, this.position, 2, 0xffff, 0);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
        }
    }
    public void writeUInt24(int v) {
        v = +v;
        this.checkInt(v, this.position, 3, 0xffffff, 0);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
        }
    }
    public void writeUInt29(int v) {
        if (128 > v) {
            this.writeInt8(v);
        } else if (16384 > v) {
            this.writeInt8(v >>> 7 & 127 | 128);
            this.writeInt8(v & 127);
        } else if (2097152 > v) {
            this.writeInt8(v >>> 14 & 127 | 128);
            this.writeInt8(v >>> 7 & 127 | 128);
            this.writeInt8(v & 127);
        } else if (1073741824 > v) {
            this.writeInt8(v >>> 22 & 127 | 128);
            this.writeInt8(v >>> 15 & 127 | 128);
            this.writeInt8(v >>> 8 & 127 | 128);
            this.writeInt8(v & 255);
        } else {
            throw new IllegalArgumentException("Integer out of range: " + v);
        }
    }
    public void writeUInt32(int v) {
        v = +v;
        this.checkInt(v, this.position, 4, 0xffffffff, 0);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
        }
    }
    public void writeUInt32Fixed(int v) {
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
        }
    }
    public void writeUInt40(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 32);
        }
    }
    public void writeUInt48(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 40);
        }
    }
    public void writeUInt56(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 48);
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 48);
        }
    }
    public void writeUInt64(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 56);
            this.data[this.position++] = (byte) (v >>> 48);
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 48);
            this.data[this.position++] = (byte) (v >>> 56);
        }
    }
    
    public int readIntBE(int offset, int byteLength) {
        this.checkOffset(offset, byteLength, this.length());
        int i = byteLength;
        double mul = 1 * 1.0;
        int val = this.data[offset + --i];
        while (i > 0) {
            mul *= 0x100;
            val += this.data[offset + --i] * mul;
        }
        mul *= 0x80;
        if (val >= mul) {
            val -= Math.pow(2, 8 * byteLength);
        }
        return val;
    }
    public int readIntLE(int offset, int byteLength) {
        this.checkOffset(offset, byteLength, this.length());
        int val = this.data[offset];
        double mul = 1 * 1.0;
        int i = 0;
        while (++i < byteLength) {
            mul *= 0x100;
            val += this.data[offset + i] * mul;
        }
        mul *= 0x80;
        if (val >= mul) {
            val -= Math.pow(2, 8 * byteLength);
        }
        return val;
    }
    
    public int readInt8() {
        this.checkOffset(this.position, 1, this.length());
        return this.data[this.position++];
    }
    public int readInt16() {
        this.checkOffset(this.position, 2, this.length());
        if (this.endian) {
            return this.data[this.position++] << 8 | this.data[this.position++];
        } else {
            return this.data[this.position++] | this.data[this.position++] << 8;
        }
    }
    public int readInt24() {
        this.checkOffset(this.position, 3, this.length());
        if (this.endian) {
            return this.data[this.position++] << 16 | this.data[this.position++] << 8 | this.data[this.position++];
        } else {
            return this.data[this.position++] | this.data[this.position++] << 8 | this.data[this.position++] << 16;
        }
    }
    public int readInt29() {
        int total = this.readInt8();
        if (total < 128) {
            return total;
        }
        total = (total & 0x7f) << 7;
        int nextByte = this.readInt8();
        if (nextByte < 128) {
            total = total | nextByte;
        } else {
            total = (total | nextByte & 0x7f) << 7;
            nextByte = this.readInt8();
            if (nextByte < 128) {
                total = total | nextByte;
            } else {
                total = (total | nextByte & 0x7f) << 8;
                nextByte = this.readInt8();
                total = total | nextByte;
            }
        }
        int mask = 1 << 28;
        return -(total & mask) | total;
    }
    public int readInt32() {
        this.checkOffset(this.position, 4, this.length());
        if (this.endian) {
            return this.data[this.position++] << 24 | this.data[this.position++] << 16 | this.data[this.position++] << 8
                    | this.data[this.position++];
        } else {
            return this.data[this.position++] | this.data[this.position++] << 8 | this.data[this.position++] << 16
                    | this.data[this.position++] << 24;
        }
    }
    public long readInt40() {
        if (this.endian) {
            return this.data[this.position++] << 32 | this.data[this.position++] << 24 | this.data[this.position++] << 16
                    | this.data[this.position++] << 8 | this.data[this.position++];
        } else {
            return this.data[this.position++] | this.data[this.position++] << 8 | this.data[this.position++] << 16
                    | this.data[this.position++] << 24 | this.data[this.position++] << 32;
        }
    }
    public long readInt48() {
        if (this.endian) {
            return this.data[this.position++] << 40 | this.data[this.position++] << 32 | this.data[this.position++] << 24
                    | this.data[this.position++] << 16 | this.data[this.position++] << 8 | this.data[this.position++];
        } else {
            return this.data[this.position++] | this.data[this.position++] << 8 | this.data[this.position++] << 16
                    | this.data[this.position++] << 24 | this.data[this.position++] << 32 | this.data[this.position++] << 40;
        }
    }
    public long readInt56() {
        if (this.endian) {
            return this.data[this.position++] << 48 | this.data[this.position++] << 40 | this.data[this.position++] << 32
                    | this.data[this.position++] << 24 | this.data[this.position++] << 16 | this.data[this.position++] << 8
                    | this.data[this.position++];
        } else {
            return this.data[this.position++] | this.data[this.position++] << 8 | this.data[this.position++] << 16
                    | this.data[this.position++] << 24 | this.data[this.position++] << 32 | this.data[this.position++] << 40
                    | this.data[this.position++] << 48;
        }
    }
    public long readInt64() {
        if (this.endian) {
            return (((long)this.data[this.position++] & 0xFF) << 56) | (((long)this.data[this.position++] & 0xFF) << 48) | (((long)this.data[this.position++] & 0xFF) << 40)
                    | (((long)this.data[this.position++] & 0xFF) << 32) | (((long)this.data[this.position++] & 0xFF) << 24) | (((long)this.data[this.position++] & 0xFF) << 16)
                    | (((long)this.data[this.position++] & 0xFF) << 8) | ((long)this.data[this.position++] & 0xFF);
        } else {
            return ((long)this.data[this.position++] & 0xFF) | (((long)this.data[this.position++] & 0xFF) << 8) | (((long)this.data[this.position++] & 0xFF) << 16)
                    | (((long)this.data[this.position++] & 0xFF) << 24) | (((long)this.data[this.position++] & 0xFF) << 32) | (((long)this.data[this.position++] & 0xFF) << 40)
                    | (((long)this.data[this.position++] & 0xFF) << 48) | (((long)this.data[this.position++] & 0xFF) << 56);
        }
    }
    
    public int readUIntBE(int offset, int byteLength) {
        this.checkOffset(offset, byteLength, this.length());
        int val = this.data[offset + --byteLength];
        double mul = 1 * 1.0;
        while (byteLength > 0) {
            mul *= 0x100;
            val += this.data[offset + --byteLength] * mul;
        }
        return val;
    }
    public int readUIntLE(int offset, int byteLength) {
        this.checkOffset(offset, byteLength, this.length());
        int val = this.data[offset];
        double mul = 1 * 1.0;
        int i = 0;
        while (++i < byteLength) {
            mul *= 0x100;
            val += this.data[offset + i] * mul;
        }
        return val;
    }
    
    public int readUInt8() {
        this.checkOffset(this.position, 1, this.length());
        return this.data[this.position++] & 0xff;
    }
    public int readUInt16() {
        this.checkOffset(this.position, 2, this.length());
        if (this.endian) {
            return this.data[this.position++] << 8 | this.data[this.position++] & 0xff;
        } else {
            return this.data[this.position++] & 0xff | this.data[this.position++] << 8;
        }
    }
    public int readUInt24() {
        this.checkOffset(this.position, 3, this.length());
        if (this.endian) {
            return this.data[this.position++] << 16 | this.data[this.position++] << 8 | this.data[this.position++] & 0xff;
        } else {
            return this.data[this.position++] & 0xff | this.data[this.position++] << 8 | this.data[this.position++] << 16;
        }
    }
    public int readUInt29() {
        int b = this.readUInt8();
        if (b < 128) {
            return b;
        }
        int value = (b & 0x7F) << 7;
        b = this.readUInt8();
        if (b < 128) {
            return (value | b);
        }
        value = (value | (b & 0x7F)) << 7;
        b = this.readUInt8();
        if (b < 128) {
            return (value | b);
        }
        value = (value | (b & 0x7F)) << 8;
        b = this.readUInt8();
        return (value | b);
    }
    public int readUInt32() {
        this.checkOffset(this.position, 4, this.length());
        if (this.endian) {
            return this.data[this.position++] << 24 | this.data[this.position++] << 16 | this.data[this.position++] << 8
                    | this.data[this.position++] & 0xff;
        } else {
            return this.data[this.position++] & 0xff | this.data[this.position++] << 8 | this.data[this.position++] << 16
                    | this.data[this.position++] << 24;
        }
    }
    public long readUInt40() {
        if (this.endian) {
            return this.data[this.position++] << 32 | this.data[this.position++] << 24 | this.data[this.position++] << 16
                    | this.data[this.position++] << 8 | this.data[this.position++] & 0xff;
        } else {
            return this.data[this.position++] & 0xff | this.data[this.position++] << 8 | this.data[this.position++] << 16
                    | this.data[this.position++] << 24 | this.data[this.position++] << 32;
        }
    }
    public long readUInt48() {
        if (this.endian) {
            return this.data[this.position++] << 40 | this.data[this.position++] << 32 | this.data[this.position++] << 24
                    | this.data[this.position++] << 16 | this.data[this.position++] << 8 | this.data[this.position++] & 0xff;
        } else {
            return this.data[this.position++] & 0xff | this.data[this.position++] << 8 | this.data[this.position++] << 16
                    | this.data[this.position++] << 24 | this.data[this.position++] << 32 | this.data[this.position++] << 40;
        }
    }
    public long readUInt56() {
        if (this.endian) {
            return this.data[this.position++] << 48 | this.data[this.position++] << 40 | this.data[this.position++] << 32
                    | this.data[this.position++] << 24 | this.data[this.position++] << 16 | this.data[this.position++] << 8
                    | this.data[this.position++] & 0xff;
        } else {
            return this.data[this.position++] & 0xff | this.data[this.position++] << 8 | this.data[this.position++] << 16
                    | this.data[this.position++] << 24 | this.data[this.position++] << 32 | this.data[this.position++] << 40
                    | this.data[this.position++] << 48;
        }
    }
    public long readUInt64() {
        if (this.endian) {
            return this.data[this.position++] << 56 | this.data[this.position++] << 48 | this.data[this.position++] << 40
                    | this.data[this.position++] << 32 | this.data[this.position++] << 24 | this.data[this.position++] << 16
                    | this.data[this.position++] << 8 | this.data[this.position++] & 0xff;
        } else {
            return this.data[this.position++] & 0xff | this.data[this.position++] << 8 | this.data[this.position++] << 16
                    | this.data[this.position++] << 24 | this.data[this.position++] << 32 | this.data[this.position++] << 40
                    | this.data[this.position++] << 48 | this.data[this.position++] << 56;
        }
    }
    
    /*
    Float & Double
     */
    public void writeFloat(float value) {
        value = +value;
        this.checkIEEE754(this.position, 4);
        this.writeInt32(Float.floatToIntBits(value));
    }
    public void writeDouble(double value) {
        value = +value;
        this.checkIEEE754(this.position, 8);
        this.writeInt64(Double.doubleToLongBits(value));
    }
    
    public float readFloat() {
        this.checkOffset(this.position, 4, this.length());
        return Float.intBitsToFloat(this.readInt32());
    }
    public double readDouble() {
        this.checkOffset(this.position, 8, this.length());
        return java.lang.Double.longBitsToDouble(this.readInt64());
    }
    
    /*
    VarInt & VarUInt
     */
    public void write7BitEncodedInt(int value) {
        byte shift = (byte) ((this.get7BitValueSize(value) - 1) * 7);
        boolean max = false;
        if (shift >= 21) {
            shift = 22;
            max = true;
        }
        while (shift >= 7) {
            this.writeInt8((0x80 | ((value >> shift) & 0x7F)));
            shift -= 7;
        }
        this.writeInt8((max ? (value & 0xFF) : (value & 0x7F)));
    }
    public void write7BitEncodedLong(long value) {
        byte shift = (byte) ((this.get7BitValueSize(value) - 1) * 7);
        boolean max = (shift >= 63);
        if (max) {
            shift++;
        }
        while (shift >= 7) {
            this.writeInt8((byte) (0x80 | ((value >> shift) & 0x7F)));
            shift -= 7;
        }
        this.writeInt8((byte) (max ? (value & 0xFF) : (value & 0x7F)));
    }
    
    public void writeVarInt32(int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                this.writeInt8(value);
                return;
            } else {
                this.writeInt8((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }
    public void writeVarInt64(long value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                this.writeInt8((int) value);
                return;
            } else {
                this.writeInt8(((int) value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }
    
    public void writeVarUInt32(int value) {
        this.writeVarInt32(value << 1 ^ value >> 31);
    }
    public void writeVarUInt64(long value) {
        this.writeVarInt64(value << 1 ^ value >> 63);
    }
    
    public int read7BitEncodedInt() {
        int n = 0;
        int b = this.readUInt8();
        int result = 0;
        while (b >= 128 && n < 3) {
            result <<= 7;
            result |= (b & 0x7F);
            b = this.readUInt8();
            ++n;
        }
        result <<= ((n < 3) ? 7 : 8);
        result |= b;
        return result;
    }
    public long read7BitEncodedLong() {
        int n = 0;
        int b = this.readUInt8();
        long result = 0;
        while (b >= 128 && n < 8) {
            result <<= 7;
            result |= (b & 0x7F);
            b = this.readUInt8();
            ++n;
        }
        result <<= ((n < 8) ? 7 : 8);
        result |= b;
        return result;
    }
    
    public int readVarInt32() {
        byte tmp = (byte) this.readInt8();
        if (tmp >= 0) {
            return tmp;
        }
        int result = tmp & 0x7f;
        if ((tmp = (byte) this.readInt8()) >= 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = (byte) this.readInt8()) >= 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = (byte) this.readInt8()) >= 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 0x7f) << 21;
                    result |= (tmp = (byte) this.readInt8()) << 28;
                    if (tmp < 0) {
                        for (int i = 0; i < 5; i++) {
                            if (this.readInt8() >= 0) {
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    public long readVarInt64() {
        int shift = 0;
        long result = 0;
        while (shift < 64) {
            final byte b = (byte) this.readInt8();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        return result;
    }
    
    public int readVarUInt32() {
        return this.readVarInt32() >>> 1 ^ -(this.readVarInt32() & 1);
    }
    public long readVarUInt64() {
        return this.readVarInt64() >>> 1 ^ -(this.readVarInt64() & 1L);
    }
    
    /*
    Extra
     */
    public void writeUTF(String s) throws UTFDataFormatException {
        int utfLength = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch > 0 && ch < 0x80) {
                utfLength++;
            } else if (ch == 0 || (ch >= 0x80 && ch < 0x800)) {
                utfLength += 2;
            } else {
                utfLength += 3;
            }
        }
        if (utfLength > 65535) {
            throw new UTFDataFormatException();
        }
        this.writeInt16(utfLength);
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            if (ch > 0 && ch < 0x80) {
                this.writeInt8(ch);
            } else if (ch == 0 || (ch >= 0x80 && ch < 0x800)) {
                this.writeInt8(0xc0 | (0x1f & (ch >> 6)));
                this.writeInt8(0x80 | (0x3f & ch));
            } else {
                this.writeInt8(0xe0 | (0x0f & (ch >> 12)));
                this.writeInt8(0x80 | (0x3f & (ch >> 6)));
                this.writeInt8(0x80 | (0x3f & ch));
            }
        }
    }
    public void writeMultiByte(String v, String charset) {
        Charset cs = Charset.forName(charset);
        if (this.endian) {
            if (charset.equals("UTF-16LE") || charset.equals("UTF-32LE")) {
                throw new IllegalArgumentException("Unmatched charset for current endian"); // Using Big endian but trying to use Little endian charset
            }
        } else {
            if (charset.equals("UTF-16BE") || charset.equals("UTF-32BE")) {
                throw new IllegalArgumentException("Unmatched charset for current endian"); // Using Little endian but trying to use Big endian charset
            }
        }
        this.writeInt8Array(v.getBytes(cs));
    }
    public void writeInt8Array(byte[] v) {
        int var3 = v.length;
        for (int var4 = 0; var4 < var3; ++var4) {
            byte element$iv = v[var4];
            this.writeInt8(element$iv);
            if (this.data[var4] == 0) {
                this.nullBytes++;
            }
        }
    }
    public void writeBytes(byte bytes[], int offset, int length) {
        if ((offset < 0) || (offset > bytes.length) || (length < 0) || ((offset + length) > bytes.length) || ((offset + length) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (length == 0) {
            return;
        }
        System.arraycopy(bytes, offset, this.data, this.position, length);
        this.position = this.position + length;
    }
    public void writeBoolean(boolean v) {
        if (v) {
            this.writeInt8(1);
        } else {
            this.writeInt8(0);
        }
    }
    
    public void writeDate(Date d)  {
        this.writeInt64(d.getTime());
    }
    
    public String readUTF() throws UTFDataFormatException {
        int utfLength = this.readInt16() & 0xffff;
        int goalPosition = this.position + utfLength;
        StringBuffer string = new StringBuffer(utfLength);
        while (this.position < goalPosition) {
            int a = this.readInt8() & 0xff;
            if ((a & 0x80) == 0) {
                string.append((char) a);
            } else {
                int b = this.readInt8() & 0xff;
                if ((b & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                if ((a & 0xe0) == 0xc0) {
                    char ch = (char) (((a & 0x1f) << 6) | (b & 0x3f));
                    string.append(ch);
                } else if ((a & 0xf0) == 0xe0) {
                    int c = this.readInt8() & 0xff;
                    if ((c & 0xc0) != 0x80) {
                        throw new UTFDataFormatException();
                    }
                    char ch = (char) (((a & 0x0f) << 12) | ((b & 0x3f) << 6) | (c & 0x3f));
                    string.append(ch);
                } else {
                    throw new UTFDataFormatException();
                }
            }
        }
        return string.toString();
    }
    public List<Character> readMultiByte(int length) {
        List<Character> array = new ArrayList<Character>();
        for (int i = 0; i < length; i++) {
            array.add((char) this.data[i]);
        }
        return array;
    }
    public List<Integer> readInt8Array(int length) {
        ArrayList<Integer> array = new ArrayList<Integer>();
        for (int i = 0; i < length; i++) {
            array.add(this.readInt8());
        }
        return array;
    }
    public byte[] readBytes(int length) {
        byte bytes[] = Arrays.copyOfRange(this.data, this.position, this.position + length);
        this.position += length;
        return bytes;
    }
    public boolean readBoolean() {
        return this.readInt8() == 1;
    }
    
    public Date readDate() {
        return new Date(this.readInt64());
    }
    
    /*
    Vector
     */
    public void writeVectorInt(int[] array) {
        this.writeInt8(array.length);
        for (int i = 0; i < array.length; i++) {
            this.writeInt32(array[i]);
        }
    }
    public void writeVectorUInt(int[] array) {
        this.writeInt8(array.length);
        for (int i = 0; i < array.length; i++) {
            this.writeUInt32Fixed(array[i]);
        }
    }
    public void writeVectorDouble(Double[] array) {
        this.writeInt8(array.length);
        for (int i = 0; i < array.length; i++) {
            this.writeDouble(array[i]);
        }
    }
    public void writeVectorString(String[] array) throws UTFDataFormatException {
        this.writeInt8(array.length);
        for (int i = 0; i < array.length; i++) {
            this.writeUTF(array[i]);
        }
    }
    
    public Vector<Integer> readVectorInt() {
        int length = this.readInt8();
        Vector<Integer> vector = new Vector<Integer>();
        for (int i = 0; i < length; i++) {
            vector.add(this.readInt32());
        }
        return vector;
    }
    public Vector<Integer> readVectorUInt() {
        int length = this.readInt8();
        Vector<Integer> vector = new Vector<Integer>();
        for (int i = 0; i < length; i++) {
            vector.add(this.readUInt32());
        }
        return vector;
    }
    public Vector<Double> readVectorDouble() {
        int length = this.readInt8();
        Vector<Double> vector = new Vector<Double>();
        for (int i = 0; i < length; i++) {
            vector.add(this.readDouble());
        }
        return vector;
    }
    public Vector<String> readVectorString() throws UTFDataFormatException {
        int length = this.readInt8();
        Vector<String> vector = new Vector<String>();
        for (int i = 0; i < length; i++) {
            vector.add(this.readUTF());
        }
        return vector;
    }
    
    public static void main(String[] args) throws UTFDataFormatException {
        ByteArrayJava wba = new ByteArrayJava();
        wba.writeDate(new Date());
        System.out.println(wba.toString());
        ByteArrayJava rba = new ByteArrayJava(wba);
        System.out.println(rba.readDate());
    }
}