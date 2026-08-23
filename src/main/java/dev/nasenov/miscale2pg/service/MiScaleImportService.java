package dev.nasenov.miscale2pg.service;

import dev.nasenov.miscale2pg.dto.MiScaleMeasurement;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementCsv;
import dev.nasenov.miscale2pg.dto.MiScaleMeasurementImport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import org.springframework.stereotype.Service;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectReader;

@Service
@RequiredArgsConstructor
public class MiScaleImportService {

  private final ObjectReader miScaleMeasurementReader;

  public MiScaleMeasurementImport build(byte[] content, String password) throws IOException {
    if (!isZipFile(content)) {
      try (MappingIterator<MiScaleMeasurement> iterator =
          miScaleMeasurementReader.readValues(content)) {
        return MiScaleMeasurementImport.of(List.of(MiScaleMeasurementCsv.of(iterator.readAll())));
      }
    }

    List<MiScaleMeasurementCsv> csvs = new ArrayList<>();

    try (ZipInputStream zipInputStream =
        new ZipInputStream(new ByteArrayInputStream(content), password.toCharArray())) {
      LocalFileHeader entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (isMiScaleMeasurementCsv(entry)) {
          try (MappingIterator<MiScaleMeasurement> iterator =
              miScaleMeasurementReader.readValues(zipInputStream.readAllBytes())) {
            csvs.add(MiScaleMeasurementCsv.of(iterator.readAll()));
          }
        }
      }
    }

    return MiScaleMeasurementImport.of(csvs);
  }

  private boolean isZipFile(byte[] content) {
    return content.length >= 4
        && content[0] == 'P'
        && content[1] == 'K'
        && ((content[2] == 3 && content[3] == 4)
            || (content[2] == 5 && content[3] == 6)
            || (content[2] == 7 && content[3] == 8));
  }

  private boolean isMiScaleMeasurementCsv(LocalFileHeader entry) {
    return !entry.isDirectory()
        && entry.getFileName().startsWith("BODY/")
        && entry.getFileName().endsWith(".csv");
  }
}
