/* JKTX
 * 
 * Copyright (c) 2011 Timon Bijlsma
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package nl.weeaboo.jktx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/*import java.nio.ByteBuffer;
import java.nio.ByteOrder;*/

public class KTXFile {

	private KTXHeader header;
	private KTXMetaData meta;
	private KTXTextureData textureData;
	
	public KTXFile() {
		clear0();
	}
	
	//Functions
	public void clear() {
		clear0();
	}
	
	private void clear0() {
		header = new KTXHeader();
		meta = new KTXMetaData();
		textureData = new KTXTextureData();		
	}
	
	// Not supported on Android
	/*public void initFromImage(BufferedImage image) {
		initFromImage(image, false, false);
	}
	public void initFromImage(BufferedImage image, boolean supportsBGRA, boolean supportsUINT8888) {
		clear();
		
		int iw = image.getWidth();
		int ih = image.getHeight();
		boolean hasAlpha = image.getColorModel().hasAlpha();
		int fmt, type, typeSize;
		
		if (hasAlpha) {
			fmt = (supportsBGRA ? GLConstants.GL_BGRA : GLConstants.GL_RGBA);
			type = GLConstants.GL_UNSIGNED_BYTE;
			typeSize = 1;
			if (supportsUINT8888) {
				type = GLConstants.GL_UNSIGNED_INT_8_8_8_8_REV;
				typeSize = 4;
			}			
			header.setGLFormat(GLConstants.GL_RGBA8, GLConstants.GL_RGBA, fmt, type, typeSize);
		} else {
			fmt = GLConstants.GL_RGB;
			type = GLConstants.GL_UNSIGNED_BYTE;
			typeSize = 1;
			header.setGLFormat(GLConstants.GL_RGB8, GLConstants.GL_RGB, fmt, type, typeSize);
		}
		header.setDimensions(iw, ih, 0);
		
		int bytesPerPixel = (hasAlpha ? 4 : 3);
		int bytesPerRow = KTXUtil.align4(iw * bytesPerPixel);
		int rowPad = bytesPerRow - (iw * bytesPerPixel);
		
		ByteBuffer buf = ByteBuffer.allocateDirect(bytesPerRow * ih);
		buf.order(ByteOrder.nativeOrder());
		for (int y = 0; y < ih; y++) {
			for (int x = 0; x < iw; x++) {
				int argb = image.getRGB(x, y);
				if (hasAlpha) {
					if (type == GLConstants.GL_BGRA) {
						buf.putInt(argb);
					} else {
						buf.put((byte)(argb>>16));
						buf.put((byte)(argb>>8 ));
						buf.put((byte)(argb    ));
						buf.put((byte)(argb>>24));
					}
				} else {
					buf.put((byte)(argb>>16));
					buf.put((byte)(argb>>8 ));
					buf.put((byte)(argb    ));
				}
			}
			buf.position(buf.position() + rowPad);
		}
		buf.rewind();
		
		textureData.setPixels(buf);
	}*/
	
	public void read(File file) throws KTXFormatException, IOException {
		FileInputStream fin = new FileInputStream(file);
		try {
			read(fin);
		} finally {
			fin.close();
		}
	}
	
	public void read(InputStream in) throws KTXFormatException, IOException {
		clear();

		header.read(in);
		meta.read(in, header.getByteOrder(), header.getBytesOfKeyValueData());
		textureData.readMipmaps(in, header.getByteOrder(), header.getGLTypeSize(),
				Math.max(1, header.getNumberOfMipmapLevels()), header.getNumberOfFaces());
	}
	
	public void write(File file) throws IOException {
		FileOutputStream fout = new FileOutputStream(file);
		try {
			write(fout);
		} finally {
			fout.close();
		}
	}
	
	public void write(OutputStream out) throws IOException {
		header.setBytesOfKeyValueData(meta.calculateRequiredBytes());
		
		header.write(out);
		meta.write(out);
		textureData.writeMipmaps(out, header.getByteOrder(), header.getGLTypeSize());
	}
	
	@Override
	public String toString() {
		return String.format("%s:\n\theader=%s\n\tmeta=%s\n\ttextureData=%s", getClass().getSimpleName(),
				header, meta, textureData);
	}
	
	//Getters
	public KTXHeader getHeader() {
		return header;
	}
	public KTXMetaData getMetaData() {
		return meta;
	}
	public KTXTextureData getTextureData() {
		return textureData;
	}
	
	//Setters
	
}
