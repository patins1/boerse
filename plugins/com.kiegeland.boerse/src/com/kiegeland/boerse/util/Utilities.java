/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import com.kiegeland.boerse.domain.Stock;

public class Utilities {

	public static void toFile(File file, String content) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(content);
		out.close();
	}

	/**
	 * Read the contents of a text file using a memory-mapped byte buffer.
	 * 
	 * A MappedByteBuffer, is simply a special ByteBuffer. MappedByteBuffer maps a region of a file directly in memory. Typically, that region comprises the entire file, although it could map a portion. You must, therefore, specify what part of the file to map. Moreover, as with the other Buffer objects, no constructor exists; you
	 * must ask the java.nio.channels.FileChannel for its map() method to get a MappedByteBuffer.
	 * 
	 * Direct buffers allocate their data directly in the runtime environment memory, bypassing the JVM|OS boundary, usually doubling file copy speed. However, they generally cost more to allocate.
	 */
	private static String fastStreamCopy(String filename) {
		String s = "";
		FileChannel fc = null;
		try {
			fc = new FileInputStream(filename).getChannel();

			// int length = (int)fc.size();

			MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			// CharBuffer charBuffer =
			// Charset.forName("ISO-8859-1").newDecoder().decode(byteBuffer);

			// ByteBuffer byteBuffer = ByteBuffer.allocate(length);
			// ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
			// CharBuffer charBuffer = byteBuffer.asCharBuffer();

			// CharBuffer charBuffer =
			// ByteBuffer.allocateDirect(length).asCharBuffer();
			/*
			 * int size = charBuffer.length(); if (size > 0) { StringBuffer sb = new StringBuffer(size); for (int count=0; count<size; count++) sb.append(charBuffer.get()); s = sb.toString(); }
			 * 
			 * if (length > 0) { StringBuffer sb = new StringBuffer(length); for (int count=0; count<length; count++) { sb.append(byteBuffer.get()); } s = sb.toString(); }
			 */
			int size = byteBuffer.capacity();
			if (size > 0) {
				// Retrieve all bytes in the buffer
				byteBuffer.clear();
				byte[] bytes = new byte[size];
				byteBuffer.get(bytes, 0, bytes.length);
				s = new String(bytes);
			}

			fc.close();
		} catch (FileNotFoundException fnfx) {
			System.err.println("File not found: " + fnfx);
		} catch (IOException iox) {
			System.err.println("I/O problems: " + iox);
		} finally {
			if (fc != null) {
				try {
					fc.close();
				} catch (IOException ignore) {
					// ignore
				}
			}
		}
		return s;
	}

	public static String fromFile(File file) throws IOException {
		return fastStreamCopy(file.toString());
		// String result = "";
		// BufferedReader br = new BufferedReader(new InputStreamReader(
		// new FileInputStream(file)));
		// String stockData;
		// while ((stockData = br.readLine()) != null) {
		// result += stockData;
		// }
		// return result;
	}

	public static String downloadURL(String s) throws MalformedURLException, IOException {
		URLConnection conn = new URL(s).openConnection();
		conn.setDoInput(true);
		conn.connect();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String asString = "";
		String stockData;
		while ((stockData = br.readLine()) != null) {
			asString = asString + stockData + "\n";
		}
		br.close();
		return asString;
	}

	static public String printPercentage(double success2) {
		return "" + String.format("%+.2f", success2 * 100) + "%";
	}

	static byte[] toByteArray(String hex) {
		hex = hex.toLowerCase();
		byte[] buf = new byte[hex.length() / 2];
		int j = 0;
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte) ((Character.digit(hex.charAt(j++), 16) << 4) | Character.digit(hex.charAt(j++), 16));
		}
		return buf;
	}

	public static void main(String[] args) throws IOException {
		toFile(new File("c:/BlackImp.reg"), new String(toByteArray("52454745444954340D0A0D0A5B484B45595F43555252454E545F555345525C536F6674776172655C41535061636B5D0D0A224B6579223D2230764B69524E6C3636754B7838736F743")));
	}

	public static Stock getOldestStock(List<Stock> stocks) {
		return stocks.get(0);
	}

	public static Stock getLatestStock(List<Stock> stocks) {
		return stocks.get(stocks.size() - 1);
	}

}
