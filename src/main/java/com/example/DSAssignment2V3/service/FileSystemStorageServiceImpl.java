package com.example.DSAssignment2V3.service;

//declare all imports
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileSystemStorageServiceImpl implements FileStorageService {

    //create location where the files are stored on the server
    private final Path fileStorageLocation; 

    //constructor to set up the file storage location
    public FileSystemStorageServiceImpl() {
        //set the directory path for storing files
        this.fileStorageLocation = Paths.get("storage_directory").toAbsolutePath().normalize();
        try {
            //create the directory if it doesnt exist
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        //normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            //check if the file's path contains invalid characters
            if(fileName.contains("..")) {
                throw new RuntimeException("Filename has invalid path" + fileName);
            }

            //copy file to the target location replacing existing file with the same name
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("cant store file " + fileName + ex);
        }
    }

    //for reading file info
    public String readFileContent(String fileName) {
        //resolve the path to the file
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        try {
            //read file content
            return Files.readString(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("error reading file " + fileName, ex);
        }
    }

    //for updating file info
    @Override
    public void updateFileContent(String filename, String content) {
        //resolve the path to the file
        Path path = fileStorageLocation.resolve(filename);
        try {
            //write the new content to the file
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new RuntimeException("couldnt update file info for " + filename, e);
        }
    }    

    //list all files in the directory and get their names
    public List<String> getAllFileNames() {
    try {
        return Files.list(fileStorageLocation)
            .filter(Files::isRegularFile)
            .map(path -> path.getFileName().toString())
            .collect(Collectors.toList());
    } catch (IOException e) {
        throw new RuntimeException("cant list files in directory", e);
    }
}

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            //resolve the path to the file and convert it to a Resource
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        Path file = fileStorageLocation.resolve(fileName).normalize();
        try {
            //delete the file if it exists
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file " + fileName, e);
        }
    }

    @Override
    public String updateFile(String fileName, MultipartFile file) {
        return null;
    }
}
