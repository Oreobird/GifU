package com.zgs.gifu.utils;

import android.graphics.Bitmap;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.TapeTimecode;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static org.jcodec.common.model.ColorSpace.RGB;

/**
 * Created by zgs on 2017/3/9.
 */

public class MP4VideoEncoder {
    private SeekableByteChannel ch;
    private Picture toEncode;
    private Transform transform;
    private H264Encoder encoder;
    private ArrayList<ByteBuffer> spsList;
    private ArrayList<ByteBuffer> ppsList;
    private FramesMP4MuxerTrack outTrack;
    private ByteBuffer _out;
    private int frameNo;
    private MP4Muxer muxer;
    private int mFPS;

    public MP4VideoEncoder(File out, int fps) throws IOException {
        this.ch = NIOUtils.writableFileChannel(out);
        this.muxer = new MP4Muxer(this.ch, Brand.MP4);
        this.mFPS = fps;
        this.outTrack = this.muxer.addTrack(TrackType.VIDEO, this.mFPS);
        this._out = ByteBuffer.allocate(12441600);
        this.encoder = new H264Encoder();
        this.transform = ColorUtil.getTransform(ColorSpace.RGB, this.encoder.getSupportedColorSpaces()[0]);
        this.spsList = new ArrayList();
        this.ppsList = new ArrayList();

    }

    public void encodeFrame(Bitmap bitmap) throws IOException {
        Picture pic = null;
        if (bitmap != null) {
            pic = fromBitmap(bitmap);
        }
        if(this.toEncode == null && pic != null) {
            this.toEncode = Picture.create(pic.getWidth(), pic.getHeight(), this.encoder.getSupportedColorSpaces()[0]);
        }

        this.transform.transform(pic, this.toEncode);
        this._out.clear();
        ByteBuffer result = this.encoder.encodeFrame(this.toEncode, this._out);
        this.spsList.clear();
        this.ppsList.clear();
        H264Utils.wipePS(result, this.spsList, this.ppsList);
        H264Utils.encodeMOVPacket(result);
        this.outTrack.addFrame(new MP4Packet(result, (long)this.frameNo, this.mFPS, 1L, (long)this.frameNo, true, (TapeTimecode)null, (long)this.frameNo, 0));
        ++this.frameNo;
    }

    public void finish() throws IOException {
        this.outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(this.spsList, this.ppsList, 4));
        this.muxer.writeHeader();
        NIOUtils.closeQuietly(this.ch);
    }

    public static Picture fromBitmap(Bitmap src) {
        Picture dst = Picture.create((int)src.getWidth(), (int)src.getHeight(), RGB);
        fromBitmap(src, dst);
        return dst;
    }

    public static void fromBitmap(Bitmap src, Picture dst) {
        int[] dstData = dst.getPlaneData(0);
        int[] packed = new int[src.getWidth() * src.getHeight()];

        src.getPixels(packed, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());

        for (int i = 0, srcOff = 0, dstOff = 0; i < src.getHeight(); i++) {
            for (int j = 0; j < src.getWidth(); j++, srcOff++, dstOff += 3) {
                int rgb = packed[srcOff];
                dstData[dstOff]     = (rgb >> 16) & 0xff;
                dstData[dstOff + 1] = (rgb >> 8) & 0xff;
                dstData[dstOff + 2] = rgb & 0xff;
            }
        }
    }

    public static Bitmap toBitmap(Picture src) {
        Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), ARGB_8888);
        toBitmap(src, dst);
        return dst;
    }

    public static void toBitmap(Picture src, Bitmap dst) {
        int[] srcData = src.getPlaneData(0);
        int[] packed = new int[src.getWidth() * src.getHeight()];

        for (int i = 0, dstOff = 0, srcOff = 0; i < src.getHeight(); i++) {
            for (int j = 0; j < src.getWidth(); j++, dstOff++, srcOff += 3) {
                packed[dstOff] = (srcData[srcOff] << 16) | (srcData[srcOff + 1] << 8) | srcData[srcOff + 2];
            }
        }
        dst.setPixels(packed, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
    }

}
