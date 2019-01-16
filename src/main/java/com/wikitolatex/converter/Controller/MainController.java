package com.wikitolatex.converter.Controller;

import com.wikitolatex.converter.Pandoc.DocumentConverter;
import com.wikitolatex.converter.Pandoc.InputFormat;
import com.wikitolatex.converter.Pandoc.OutputFormat;
import com.wikitolatex.converter.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/converter")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DocumentConverter documentConverter;

    //private static final String EXTERNAL_FILE_PATH = ".\\ResourceFiles\\";

    @RequestMapping("/index")
    String index() {
        return "index page";
    }

    AtomicInteger i = new AtomicInteger(0);


    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file){
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/converter/downloadFile/")
                .path(fileName)
                .toUriString();

        fileName = parser(fileName);
        try {
            TimeUnit.SECONDS.sleep(1);
            documentConverter.fromFile(new File("./ResourceFiles/" + fileName), InputFormat.MEDIAWIKI)
                    .toFile(new File("./ResourceFiles/" + fileName + ".tex"), OutputFormat.LATEX)
                    .convert();
            TimeUnit.SECONDS.sleep(1);
            documentConverter.fromFile(new File("./ResourceFiles/" + fileName + ".tex"), InputFormat.LATEX)
                    .toFile(new File("./ResourceFiles/" + fileName + ".pdf"), OutputFormat.PDF)
                    .convert();

            System.out.println("\nConversion successful!\nFile converted: " + fileName);
        }
        catch (Exception ex){
            System.out.println("\nConversion unsuccessful!\nFile not converted");
        }
        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        String fileName;

        for (MultipartFile file : files){
            fileName = fileStorageService.storeFile(file);
            fileName = parser(fileName);

            /*documentConverter.fromFile(new File("./ResourceFiles/"+fileName), InputFormat.TWIKI)
                    .toFile(new File("./ResourceFiles/"+fileName+".tex"), OutputFormat.LATEX)
                    .convert();
            documentConverter.fromFile(new File("./ResourceFiles/"+fileName+".tex"), InputFormat.LATEX)
                    .toFile(new File("./ResourceFiles/"+fileName+".docx"),OutputFormat.DOCX)
                    .convert();*/

            System.out.println("\nConversion successful!\nFile converted: " + fileName);
        }

        return Arrays.stream(files)
                .map(this::uploadFile)
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                /*
                    in header: use attachment instead of inline for instant download in browser
                 */
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    public String parser(String fileName){
        String outputFile = fileName+".parsed";

        Runtime runtime = Runtime.getRuntime();

        try {
            runtime.exec("cmd /c start cmd.exe /c \"parser " + "./ResourceFiles/" + fileName + " > ./ResourceFiles/" + outputFile + "\"").waitFor();

            return outputFile;
        }
        catch (Exception e){
            System.out.println("HEY Buddy ! U r Doing Something Wrong ");
            e.printStackTrace();
            return "0";
        }
    }
}