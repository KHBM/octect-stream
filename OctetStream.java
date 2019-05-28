import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by team.
 * User: team
 * Date: 2019-04-25
 * Time: 11:03
 */
@Slf4j
public class OctetStream {

  public String changeFileNameForExtension(File originFile) throws IOException {
    byte[] buf = new byte[32];
    try (FileInputStream fis = new FileInputStream(originFile)) {

      fis.read(buf);

    } catch (Exception e) {
      throw new IOException(" file read Error : " + originFile);
    }

    String extractedExt = getExtFromFileHeaderFormat(buf);

    if (extractedExt == null) {
      throw new IOException(
          "Unknown Image format found.. Cannot infer file type! " + originFile.getName());
    }

    log.info("Infer success : file format was {} so start renaming", extractedExt);

    /* rename */
    String onlyFileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(originFile.getName());
    File renamedFile = new File(originFile.getParent() + "/" + onlyFileNameWithoutExtension + "." + extractedExt);
    //    File renamedFile = new File(originFile.getParent() + "/" + onlyFileNameWithoutExtension + ".JPG");
    if (originFile.renameTo(renamedFile)) {
      log.debug("new file name from {} to {}", originFile.getName(), renamedFile.getName());
    }
    return renamedFile.getName();
  }

  private String getExtFromFileHeaderFormat(byte[] targets) {

    final byte[][][] checksum = new byte[][][]{
        {
            /* GIF CASE 1*/
            {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF},
            {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x00, (byte) 0x61}
        },
        {
            /* GIF CASE 2*/
            {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF},
            {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x00, (byte) 0x61}
        },
        {
            /* PNG CASE */
            {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF},
            {(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47, (byte) 0x0d, (byte) 0x0a,
                (byte) 0x1a, (byte) 0x0a}
        },
        {
            /* JPG CASE 1 */
            {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
                (byte) 0xFF, (byte) 0xFF},
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, (byte) 0x00, (byte) 0x00,
                (byte) 0x4A, (byte) 0x46}
        },
        {
            /* JPG CASE 2 */
            {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
                (byte) 0xFF, (byte) 0xFF},
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1, (byte) 0x00, (byte) 0x00,
                (byte) 0x45, (byte) 0x78}
        },
        {
            /* JPG CASE 3 */
            {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
                (byte) 0xFF, (byte) 0xFF},
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE8, (byte) 0x00, (byte) 0x00,
                (byte) 0x53, (byte) 0x50}
        },
        {
            /* WEBP CASE 3 */
            {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00},
            {(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00},
            {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00},
            {(byte) 0x57, (byte) 0x45, (byte) 0x42, (byte) 0x50, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00}
        },
    };

    final String[] returnValues = new String[]{
        "GIF", "GIF", "PNG", "JPG", "JPG", "JPG", "WEBP"
    };

    boolean isAllZero = false;

    for (int i = 0; i < checksum.length; i++) {

      isAllZero = true;

      for (int j = 0; j < checksum[i].length; j += 2) {

        for (int k = 0; k < checksum[i][j].length; k++) {
          byte b = targets[k];
          b &= checksum[i][j][k];
          b ^= checksum[i][j + 1][k];

          if (b != 0) {
            isAllZero = false;
          }
        }

        if (isAllZero) {
          return returnValues[i];
        }

      }
    }

    return null;
  }
}
