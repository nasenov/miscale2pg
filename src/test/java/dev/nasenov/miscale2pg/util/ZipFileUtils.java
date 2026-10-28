package dev.nasenov.miscale2pg.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public final class ZipFileUtils {

  public static final String PASSWORD = "password";

  private ZipFileUtils() {}

  public static byte[] create() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (ZipOutputStream _ = new ZipOutputStream(outputStream, PASSWORD.toCharArray())) {}

    return outputStream.toByteArray();
  }

  public static byte[] create(String csv) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (ZipOutputStream zipOutputStream =
        new ZipOutputStream(outputStream, PASSWORD.toCharArray())) {
      ZipParameters zipParameters = new ZipParameters();
      zipParameters.setEncryptFiles(true);
      zipParameters.setEncryptionMethod(EncryptionMethod.AES);
      zipParameters.setFileNameInZip("BODY/measurements.csv");

      zipOutputStream.putNextEntry(zipParameters);
      zipOutputStream.write(csv.getBytes());
      zipOutputStream.closeEntry();
    }

    return outputStream.toByteArray();
  }
}
