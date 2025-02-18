package services.export;

import java.io.IOException;
import java.util.Optional;
import models.Program;
import services.program.CsvExportConfig;
import services.program.PdfExportConfig;

public class ExporterFactory {

  public PdfExporter pdfExporter(Program program) throws NotConfiguredException, IOException {
    Optional<PdfExportConfig> exportConfig =
        program.getProgramDefinition().exportDefinitions().stream()
            .filter(exportDefinition -> exportDefinition.pdfConfig().isPresent())
            .map(exportDefinition -> exportDefinition.pdfConfig().get())
            .findAny();
    if (exportConfig.isEmpty()) {
      throw new NotConfiguredException();
    }
    return new PdfExporter(exportConfig.get().baseDocument(), exportConfig.get().mappings());
  }

  public CsvExporter csvExporter(Program program) throws NotConfiguredException {
    Optional<CsvExportConfig> exportConfig =
        program.getProgramDefinition().exportDefinitions().stream()
            .filter(exportDefinition -> exportDefinition.csvConfig().isPresent())
            .map(exportDefinition -> exportDefinition.csvConfig().get())
            .findAny();
    if (exportConfig.isEmpty()) {
      throw new NotConfiguredException();
    }
    return csvExporter(exportConfig.get());
  }

  public CsvExporter csvExporter(CsvExportConfig exportConfig) {
    return new CsvExporter(exportConfig.columns());
  }
}
