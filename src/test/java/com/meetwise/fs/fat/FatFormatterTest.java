
package com.meetwise.fs.fat;

import com.meetwise.fs.BootSector;
import com.meetwise.fs.BlockDevice;
import com.meetwise.fs.FSDirectory;
import java.nio.ByteBuffer;
import java.util.Iterator;
import com.meetwise.fs.FSDirectoryEntry;
import com.meetwise.fs.util.RamDisk;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public class FatFormatterTest {
    
    @Test
    public void testFat16Format() throws Exception {
        System.out.println("fat16Format");
        
        BlockDevice d = new RamDisk(5242880);
        
        FatFormatter ff = FatFormatter.superFloppyFormatter(d);
        ff.format(d, null);

        FatFileSystem fs = new FatFileSystem(d, false);
        final BootSector bs = fs.getBootSector();
        
        assertEquals(2, bs.getNrFats());
        assertEquals(d.getSectorSize(), bs.getBytesPerSector());
        assertTrue(bs.getNrRootDirEntries() % d.getSectorSize() == 0);
        
        assertTrue(fs.getClusterSize() < 32 * 1024);
    }

    @Test
    public void testFat32Format() throws Exception {
        System.out.println("fat32Format");

        BlockDevice d = new RamDisk(8 * 1024 * 1024);
        FatFormatter ff = FatFormatter.superFloppyFormatter(d, FatType.FAT32);
        ff.format(d, null);
        
        FatFileSystem fs = new FatFileSystem(d, false);
        final BootSector bs = fs.getBootSector();
    }
    
    @Test
    public void testVolumeLabel() throws Exception {
        System.out.println("testVolumeLabel");
        
        BlockDevice d = new RamDisk(512 * 1024);

        FatFormatter ff = FatFormatter.superFloppyFormatter(d);
        ff.format(d, "TestVol");

        FatFileSystem fs = new FatFileSystem(d, false);

        ByteBuffer bb = ByteBuffer.allocate(64);
        d.read(FatUtils.getRootDirOffset(fs.getBootSector()), bb);
        bb.flip();
        Utils.hexDump(System.out, bb);
        
        assertEquals("TestVol", fs.getVolumeLabel());
        
        final FSDirectory root = fs.getRoot();
        final Iterator<FSDirectoryEntry> i = root.iterator();

        assertFalse(i.hasNext());
    }

    public static void main(String[] args) throws Exception {
        FatFormatterTest t = new FatFormatterTest();
        t.testVolumeLabel();
    }
}