package de.waldheinz.fs.fat;

import de.waldheinz.fs.fat.FatFileSystem;
import de.waldheinz.fs.fat.FatType;
import de.waldheinz.fs.fat.SuperFloppyFormatter;
import de.waldheinz.fs.fat.FatLfnDirectory;
import de.waldheinz.fs.FsDirectoryEntry;
import de.waldheinz.fs.FsFile;
import de.waldheinz.fs.util.FileDisk;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public class DosFsckTest {

    private final static String DOSFSCK_CMD = "/sbin/dosfsck";
    private File file;
    private FileDisk dev;

    @Before
    public void setUp() throws IOException {
        this.file = File.createTempFile("fat32-lib-test-", ".img");
    }

    @After
    public void tearDown() throws IOException {
        this.dev.close();
        this.dev = null;

        this.file.delete();
        this.file = null;
    }

    @Test
    @Ignore
    public void testVolumeLabel() throws Exception {
        System.out.println("volumeLabel");

        this.dev = FileDisk.create(file, 128 * 1024 * 1024);
        SuperFloppyFormatter.get(dev).setFatType(FatType.FAT32).
                setVolumeLabel("Cool Vol").format();

        runFsck();
    }

    @Test
    @Ignore
    public void testFat32Write() throws Exception {
        System.out.println("fat32Write");

        this.dev = FileDisk.create(file, 128 * 1024 * 1024);
        SuperFloppyFormatter.get(dev).setFatType(FatType.FAT32).format();
        
        FatFileSystem fs = new FatFileSystem(dev, false);
        final FatLfnDirectory rootDir = (FatLfnDirectory) fs.getRoot();

        FatLfnDirectory.LfnEntry entry = rootDir.addDirectory("Directory");
        
        for (int i = 0; i < 1; i++) {
            final FsDirectoryEntry e = entry.getDirectory().addFile(
                    "This is file number " + i);
            final FsFile fsFile = e.getFile();
            
            byte[] nullBytes = new byte[516];
            ByteBuffer buff = ByteBuffer.wrap(nullBytes);
            buff.rewind();
            fsFile.write(0, buff);
        }
        
        fs.flush();
        fs.close();
        runFsck();
    }

    @Test
    @Ignore
    public void testCreateFat32() throws Exception {
        System.out.println("createFat32");

        this.dev = FileDisk.create(file, 128 * 1024 * 1024);
        SuperFloppyFormatter.get(dev).setFatType(FatType.FAT32).format();
        runFsck();
    }

    @Test
    @Ignore
    public void testCreateFat16() throws Exception {
        System.out.println("createFat16");

        this.dev = FileDisk.create(file, 16 * 1024 * 1024);
        SuperFloppyFormatter.get(dev).setFatType(FatType.FAT16).format();
        runFsck();
    }

    @Test
    @Ignore
    public void testCreateFat12() throws Exception {
        System.out.println("createFat12");

        this.dev = FileDisk.create(file, 2 * 1024 * 1024);
        SuperFloppyFormatter.get(dev).setFatType(FatType.FAT12).format();
        runFsck();
    }
    
    private void runFsck() throws Exception {
        System.out.println("running fsck on " + file);
        final ProcessBuilder pb = new ProcessBuilder(
                DOSFSCK_CMD, "-v", "-n", file.toString());

        pb.redirectErrorStream(true);
        final Process proc = pb.start();

        while (true) {
            final int c = proc.getInputStream().read();
            if (c < 0) {
                break;
            }

            System.out.write(c);
        }

        assertEquals(0, proc.waitFor());
    }
}