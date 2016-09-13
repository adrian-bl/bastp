/*
 * Copyright (C) 2013 Adrian Ulrich <adrian@blinkenlights.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>. 
 */


package ch.blinkenlights.bastp;

import ch.blinkenlights.bastp.OggFile;
import ch.blinkenlights.bastp.FlacFile;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;
import java.io.*;


public class Test {
	public static void main(String[] args) {
		int cnt_ogg   = 0;
		int cnt_flac  = 0;
		int cnt_other = 0;
		int cnt_tags  = 0;
		int cnt_id3v2 = 0;
		int cnt_broken= 0;
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s;
		
		try {
			while ((s = in.readLine()) != null && s.length() != 0) {
				System.out.println("");
				System.out.println("------------------------------------------------------------");
				System.out.println("<FILE> "+s);
				
				HashMap tags = (new Bastp()).getTags(s);
				final Enumeration<String> e = Collections.enumeration(tags.keySet());
				
				if(! tags.containsKey("type")) {
					cnt_broken++;
				}
				else if(tags.get("type").equals("FLAC")) {
					cnt_flac++;
				}
				else if(tags.get("type").equals("OGG") || tags.get("type").equals("OPUS")) {
					cnt_ogg++;
				}
				else if(tags.get("type").equals("MP3/Lame")) {
					cnt_id3v2++;
				}
				else if(tags.get("type").equals("MP3/ID3v2")) {
					cnt_id3v2++;
				}
				else {
					cnt_other++;
				}
				
				while(e.hasMoreElements()) {
					String k = (String)e.nextElement();
					System.out.println("<"+k+"> = "+tags.get(k));
					cnt_tags++;
				}
			}
			
			System.out.println("== TEST RESULTS, PARSED "+(cnt_flac+cnt_ogg+cnt_other)+" FILE(S)");
			System.out.println("== BROKEN = "+cnt_broken+", FLAC = "+cnt_flac+", OGG = "+cnt_ogg+", ID3v2 = "+cnt_id3v2+", OTHER = "+cnt_other+", TAGS = "+cnt_tags);
		} catch(Exception dontcate) {}
	}
}
