package com.galfarslair.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import nl.weeaboo.jktx.KTXFile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class KtxTextureData implements TextureData {
	private int width = 0;
	private int height = 0;
	private boolean isPrepared = false;
		
	private KTXFile file;
	private int mipLevels;
	private int format;
	private int internalFormat;
	
	public KtxTextureData(KTXFile file) {
		this.file = file;
	}
	
	@Override
	public TextureDataType getType() {		
		return TextureDataType.Compressed;
	}
	
	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public boolean isPrepared() {		
		return isPrepared;
	}

	@Override
	public void prepare() {
		if (isPrepared) throw new GdxRuntimeException("Already prepared");
		if (file == null) throw new GdxRuntimeException("Can only load once from KTX file");
		
		width = file.getHeader().getPixelWidth();
		height = file.getHeader().getPixelHeight();
		mipLevels = file.getHeader().getNumberOfMipmapLevels();
		format = file.getHeader().getGLFormat();
		internalFormat = file.getHeader().getGLInternalFormat();
		boolean supported = TextureFormatHelper.isFormatSupported(format, internalFormat);
		
		if (!supported) throw new GdxRuntimeException("Texture format of KTX file not supported:  " + String.format("fmt:0x%x/int:0x%x",  format, internalFormat));
		
		isPrepared = true;
	}
	
	@Override
	public void consumeCompressedData(int target) {		
		int type = file.getHeader().getGLType(); 
		
		for (int i = 0; i < mipLevels; i++) {
			int levelWidth = file.getHeader().getPixelWidth(i);
			int levelHeight = file.getHeader().getPixelHeight(i);
			int levelSize = file.getTextureData().getBytesPerFace(i);
			ByteBuffer data = file.getTextureData().getFace(i, 0);
			
			if (format == 0) {
				// compressed format
				Gdx.gl.glCompressedTexImage2D(target, i, internalFormat, 
						levelWidth, levelHeight, 0, levelSize, data);
				
			} else {
				Gdx.gl.glTexImage2D(target, i, internalFormat, 
						levelWidth, levelHeight, 0,	format, type, data);				
			}
		}
		
		file.clear();
		file = null;
		isPrepared = false;
	}
	
	@Override
	public Pixmap consumePixmap() {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public boolean disposePixmap() {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}
	
	@Override
	public Format getFormat() {
		// Hopefully isn't used anywhere, cannot map to pixmap format
		return Format.RGBA8888; 		
	}

	@Override
	public boolean useMipMaps() {
		// Mipmaps are not generated, but should be included in KTX file if needed
		return mipLevels > 1;
	}

	@Override
	public boolean isManaged() {
		return true;
	}
	
	public static class TextureFormatHelper {	
		// ETC1
		public static final String GL_EXT_ETC1 = "GL_OES_compressed_ETC1_RGB8_texture";
		public static final int GL_ETC1_RGB_OES = 0x8D64;
		// PVRTC 
		public static final String GL_EXT_PVRTC = "GL_IMG_texture_compression_pvrtc";
		public static final int GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG = 0x8C00;
		public static final int GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG = 0x8C01;
		public static final int GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = 0x8C02;
		public static final int GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG = 0x8C03;
		// PVRTC V2
		public static final String GL_EXT_PVRTC2 = "GL_IMG_texture_compression_pvrtc2";
		public static final int GL_COMPRESSED_RGBA_PVRTC_2BPPV2_IMG = 0x9137;
		public static final int GL_COMPRESSED_RGBA_PVRTC_4BPPV2_IMG = 0x9138;
		// DXTC/S3TC
		public static final String GL_EXT_S3TC = "GL_EXT_texture_compression_s3tc";
		public static final int GL_COMPRESSED_RGB_S3TC_DXT1_EXT = 0x83F0;
		public static final int GL_COMPRESSED_RGBA_S3TC_DXT1_EXT = 0x83F1;
		public static final int GL_COMPRESSED_RGBA_S3TC_DXT3_EXT = 0x83F2;
		public static final int GL_COMPRESSED_RGBA_S3TC_DXT5_EXT = 0x83F3;
		// AMD/ATC
		public static final int GL_ATC_RGBA_EXPLICIT_ALPHA_AMD = 0x8C93;
		public static final int GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD = 0x87EE;
		// ETC2/EAC (for future)
		public static final int GL_COMPRESSED_R11_EAC = 0x9270;
		public static final int GL_COMPRESSED_SIGNED_R11_EAC = 0x9271;
		public static final int GL_COMPRESSED_RG11_EAC = 0x9272;
		public static final int GL_COMPRESSED_SIGNED_RG11_EAC = 0x9273;
		public static final int GL_COMPRESSED_RGB8_ETC2 = 0x9274;
		public static final int GL_COMPRESSED_SRGB8_ETC2 = 0x9275;
		public static final int GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9276;
		public static final int GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9277;
		public static final int GL_COMPRESSED_RGBA8_ETC2_EAC = 0x9278;
		public static final int GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC = 0x9279;
		
		private static Set<Integer> compressedFormats;
		
		static {
			compressedFormats = new HashSet<Integer>();
			IntBuffer buff = BufferUtils.newIntBuffer(16);			
			Gdx.gl.glGetIntegerv(GL10.GL_NUM_COMPRESSED_TEXTURE_FORMATS, buff);
			int count = buff.get(0);			
			buff = BufferUtils.newIntBuffer(Math.max(count, 16));
			Gdx.gl.glGetIntegerv(GL10.GL_COMPRESSED_TEXTURE_FORMATS, buff);
			for (int i = 0; i < count; i++) {
				compressedFormats.add(buff.get());
			}
		}
		
		public static boolean isFormatSupported(int format, int internalFormat) {
			if (format != 0) {
				// not compressed
				return true;
			} else {
				// NOTE: is this dependable on ALL GPUs? Or check for extension is also needed?
				return compressedFormats.contains(internalFormat);
			}
		}
	}

}
