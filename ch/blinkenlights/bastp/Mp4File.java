package ch.blinkenlights.bastp;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Stack;

class Atom {
  String name;
  long start;
  int length;
  
  public Atom(String name, long start, int length) {
    this.name = name;
    this.start = start;
    this.length = length;
  }
}

public class Mp4File extends Common {
  
  public Mp4File() {
  }
  
  public HashMap getTags(RandomAccessFile s) throws IOException {
    HashMap tags = new HashMap();
    // maintain a trail of breadcrumbs to know what part of the file we're in,
    // so e.g. that we only parse "name" atoms that are part of a tag
    Stack<Atom> path = new Stack<Atom>();
    s.seek(0);
    int atomSize;
    byte[] atomNameRaw = new byte[4];
    String atomName;
    String tagName = null;
    
    // file structure info from http://atomicparsley.sourceforge.net/mpeg-4files.html
    while (s.getFilePointer() < s.length()) {

      // read an atom's details
      atomSize = s.readInt();
      s.read(atomNameRaw);
      atomName = new String(atomNameRaw);
      
      while (!path.empty() && s.getFilePointer() > (path.peek().start + path.peek().length)) {
        // if we've parsed the tags, we can leave
        if (path.peek().name == "ilst") { return tags; }
        path.pop();
      }
      
      // debug info
      /*
      System.out.println();
      if (!path.empty()) {
        Atom parent = path.peek();
        for (int i = 0; i <= path.size(); i++) { System.out.print("| "); }
        System.out.println("parent '" + parent.name + "' ends at " + (parent.start + parent.length));
        for (int i = 0; i <= path.size(); i++) { System.out.print("| "); }
        System.out.println("   new atom starts at " + (s.getFilePointer()-8));
      }
      for (int i = 0; i <= path.size(); i++) { System.out.print("| "); }
      System.out.println("   '" + atomName + "' has length " + atomSize);
      */
      
      // only dive into pertinent atoms; we only want extended metadata,
      // which is mostly in "----" atoms. for now we'll just grab replaygain
      if (path.empty() && atomName.equals("moov") ||
          (!path.empty() && (
            path.peek().name.equals("moov") && atomName.equals("udta") ||
            path.peek().name.equals("udta") && atomName.equals("meta") ||
            path.peek().name.equals("meta") && atomName.equals("ilst") ||
            path.peek().name.equals("ilst") && atomName.equals("----") ||
            path.peek().name.equals("----") && atomName.equals("name") ||
            path.peek().name.equals("----") && atomName.equals("data"))
          ))
      {
        path.push(new Atom(atomName, s.getFilePointer()-8, atomSize));
      }
      // we aren't interested in most blocks, skip them
      else { s.skipBytes(atomSize-8); }
      // the meta block has an extra 4 bytes that need to be skipped
      if (path.size() > 1 && path.get(path.size()-2).name.equals("udta") && atomName.equals("meta")) {
        s.skipBytes(4);
      }
      
      // read tag contents
      if (path.size() > 2 &&
        path.get(path.size()-3).name.equals("ilst") && 
        path.get(path.size()-2).name.equals("----")
      ) {
        // get a tag name
        if (atomName.equals("name")) {
          s.skipBytes(4);
          byte[] buffer = new byte[atomSize-12];
          s.read(buffer, 0, atomSize-12);
          tagName = new String(buffer);
        }
      
        // get a tag value
        else if (atomName.equals("data")) {
          // skip flags/null bytes
          s.skipBytes(8);
          byte[] buffer = new byte[atomSize-16];
          s.read(buffer, 0, atomSize-16);
          String tagValue = new String(buffer);
          //System.out.println(tagName);
          //System.out.println(tagValue);
          if (tagName.equals("replaygain_track_gain") ||
              tagName.equals("replaygain_album_gain"))
          {
            addTagEntry(tags, tagName.toUpperCase(), tagValue);
            tagName = null;
          }
        }
      }
    }
    return tags;
  }
}
