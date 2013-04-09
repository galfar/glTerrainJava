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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class KTXTextureData {

	private MipmapLevel[] mipmaps;
	private int numberOfMipmapLevels;
	private int numberOfFaces;
	
	public KTXTextureData() {
		mipmaps = new MipmapLevel[0];
	}
	
	//Functions
	public void readMipmaps(InputStream in, ByteOrder inputOrder, int glTypeSize,
			int numberOfMipmapLevels, int numberOfFaces) throws KTXFormatException, IOException
	{
		for (int level = 0; level < numberOfMipmapLevels; level++) {
			readMipmapLevel(in, inputOrder, glTypeSize, level, numberOfFaces);
		}
	}
	
	public void readMipmapLevel(InputStream in, ByteOrder inputOrder, int glTypeSize, int level,
			int numberOfFaces) throws KTXFormatException, IOException
	{
		int bytesPerFace = KTXUtil.readInt(in, inputOrder);
		ByteBuffer[] faces = new ByteBuffer[numberOfFaces];
		for (int face = 0; face < numberOfFaces; face++) {
			ByteBuffer buf = ByteBuffer.allocateDirect(KTXUtil.align4(bytesPerFace));
			buf.order(ByteOrder.nativeOrder());
			readFace(in, inputOrder, glTypeSize, buf);
			buf.limit(bytesPerFace);
			faces[face] = buf;
		}
		setMipmapLevel(level, faces, bytesPerFace);
	}
	
	protected void readFace(InputStream in, ByteOrder inputOrder, int glTypeSize, ByteBuffer out)
		throws KTXFormatException, IOException
	{
		KTXUtil.readFully(in, out);
		if (inputOrder != out.order()) {
			KTXUtil.swapEndian(out, glTypeSize);
		}
	}
	
	public void writeMipmaps(OutputStream out, ByteOrder outputOrder, int glTypeSize) throws IOException {
		for (int level = 0; level < getNumberOfMipmapLevels(); level++) {
			writeMipmapLevel(out, outputOrder, glTypeSize, level);
		}
	}
	
	public void writeMipmapLevel(OutputStream out, ByteOrder outputOrder, int glTypeSize,
			int mipmapLevel) throws IOException
	{
		MipmapLevel ml = getMipmapLevel(mipmapLevel);
		int bytesPerFace = ml.getBytesPerFace();
		KTXUtil.writeInt(out, outputOrder, bytesPerFace);
		
		byte[] padding = new byte[KTXUtil.align4(bytesPerFace) - bytesPerFace];
		for (int face = 0; face < ml.getNumberOfFaces(); face++) {
			ByteBuffer buf = ml.getFace(face);
			if (buf.order() != outputOrder) {
				KTXUtil.swapEndian(buf, glTypeSize);
			}
			KTXUtil.writeFully(out, buf);
			if (buf.order() != outputOrder) {
				KTXUtil.swapEndian(buf, glTypeSize);
			}
			out.write(padding);
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s[mipmapLevels=%d, faces=%d]", getClass().getSimpleName(),
				getNumberOfMipmapLevels(), getNumberOfFaces());
	}
	
	//Getters
	private MipmapLevel getMipmapLevel(int level) {
		return mipmaps[level];
	}
	public ByteBuffer getFace(int mipmapLevel, int faceIndex) {		
		return getMipmapLevel(mipmapLevel).getFace(faceIndex);
	}
	public int getBytesPerFace(int mipmapLevel) {
		return getMipmapLevel(mipmapLevel).getBytesPerFace();
	}
	public int getNumberOfMipmapLevels() {
		return numberOfMipmapLevels;
	}
	public int getNumberOfFaces() {
		return numberOfFaces;
	}
	
	//Setters
	public void setPixels(ByteBuffer pixels) {
		setMipmapLevel(0, pixels);
	}
	public void setMipmapLevel(int level, ByteBuffer pixels) {
		setMipmapLevel(level, new ByteBuffer[] {pixels}, pixels.remaining());
	}
	public void setMipmapLevel(int level, ByteBuffer[] faces, int bytesPerFace) {
		MipmapLevel ml = new MipmapLevel(faces.length, bytesPerFace);
		for (int face = 0; face < ml.getNumberOfFaces(); face++) {
			ml.setFace(face, faces[face]);
		}
		setMipmapLevel(level, ml);
	}
	private void setMipmapLevel(int level, MipmapLevel ml) {
		numberOfMipmapLevels = Math.max(numberOfMipmapLevels, level+1);
		numberOfFaces = Math.max(numberOfFaces, ml.getNumberOfFaces());
		
		if (mipmaps.length < numberOfMipmapLevels) {
			mipmaps = Arrays.copyOf(mipmaps, numberOfMipmapLevels);
		}
		
		mipmaps[level] = ml;
	}
	
	//Inner Classes
	private class MipmapLevel {
		
		private ByteBuffer[] faces;
		private int bytesPerFace;
		
		public MipmapLevel(int numberOfFaces, int bytesPerFace) {
			this.faces = new ByteBuffer[numberOfFaces];
			this.bytesPerFace = bytesPerFace;
		}

		public int getBytesPerFace() {
			return bytesPerFace;
		}
		
		public ByteBuffer getFace(int index) {
			return faces[index];
		}		
		public int getNumberOfFaces() {
			return faces.length;
		}
		
		public void setFace(int index, ByteBuffer buf) {
			faces[index] = buf;
		}
		
	}
	
}
