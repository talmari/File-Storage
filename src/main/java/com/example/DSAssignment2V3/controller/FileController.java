package com.example.DSAssignment2V3.controller;

//declare all imports
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import com.example.DSAssignment2V3.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import org.springframework.ui.Model;


//create spring client and map all requests under /files to here
@Controller
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;

    //constructor for FileStorageService interface
    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    //map the GET requests to '/files/' to list all uploaded files
    @GetMapping("/")
    public String listUploadedFiles(Model model) {

    //get a list of file names from the directory
    List<String> fileNames = fileStorageService.getAllFileNames(); 

    //add the list of file names
    model.addAttribute("fileNames", fileNames);
    //return the html view
    return "index";
    }

    //map GET requests to '/files/{filename}' to server file
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename, HttpServletRequest request) {
    //load the file as a resource
    Resource resource = fileStorageService.loadFileAsResource(filename);

    //try to determine file's content type
    String contentType = null;
    try {
        contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
    } catch (IOException ex) {
        //set the default content type if file's content type couldn't be found
        contentType = "application/octet-stream";
    }

    //fallback to the default content type if type could not be found
    if(contentType == null) {
        contentType = "application/octet-stream";
    }
    //return the file as a ResponseEntity
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
}

    
    //map GET requests to '/files/edit/{filename}' for editing file info
    @GetMapping("/edit/{filename:.+}")
    public String editFile(@PathVariable String filename, Model model) {
        //load file content
        String content = fileStorageService.readFileContent(filename);

        //add filename and info
        model.addAttribute("filename", filename);
        model.addAttribute("content", content);
        //return HTML view
        return "editFile"; 
    }

    // Map the POST requests to '/files/' for uploading a file
    @PostMapping("/")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        String fileName = fileStorageService.storeFile(file);
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + fileName + "!");
        //this should match the GetMapping for the HTML page
        return "redirect:/files/"; 
    }    

    //putmapping
    //maps PUT requests to '/files/update' for updating file infoormation
    @PostMapping("/update")
    public String updateFile(
        @RequestParam("filename") String filename, 
        @RequestParam("content") String content,
        RedirectAttributes redirectAttributes) {
        fileStorageService.updateFileContent(filename, content);
        redirectAttributes.addFlashAttribute("message", "You successfully updated " + filename + "!");
        return "redirect:/files/";
    }
    
    //map DELETE requests to '/files/{filename}' for deleting a file
    @DeleteMapping("/{filename:.+}")
    public String deleteFile(@PathVariable String filename, RedirectAttributes redirectAttributes) {
        fileStorageService.deleteFile(filename);
        redirectAttributes.addFlashAttribute("message", "You successfully deleted " + filename + "!");
        return "redirect:/files/";
    }
 

}
