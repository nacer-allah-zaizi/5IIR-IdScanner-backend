package com.example.demo.Controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.Model.File;
import com.example.demo.Services.FileService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping ("/File")
@CrossOrigin(origins = "http://localhost:3000") // Remplacez cela par l'URL réelle de votre frontend
public class FileController {

    @Autowired
    private FileService fileService;

    
    @PostMapping("/upload")
    public ModelAndView handleFileUpload(@RequestParam("file") MultipartFile file) {
        ModelAndView modelAndView = new ModelAndView("fileUploadResult");

        try {
            // Stocker le fichier dans le répertoire configuré
            fileService.storeFile(file);

            // Analyser le document image et obtenir les informations extraites
            File scannedFile = fileService.scanDocumentImage(file);

            // Ajouter le fichier extrait à la vue
            //modelAndView.addObject("scannedFile", scannedFile);
            
        } catch (IOException e) {
            e.printStackTrace();
            modelAndView.addObject("error", "Erreur lors du traitement du fichier.");
        }

        return modelAndView;
    }
   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @PostMapping("/ajouter")
	public File ajouterFile(@RequestBody File clt)
	{
		return fileService.ajouterFile(clt);
	}
	
	@GetMapping("/all")
	public List <File>getClient()
	{
		return fileService.getFile();
	}
	@PutMapping("/modifier")
	public	void  modifierClient(@RequestBody File cpt)
	{
		
		fileService.modifierFile(cpt);
	}
	@DeleteMapping ("/{id}")
	public void supprimer(@PathVariable int id) {
	
		fileService.SupprimerFile(id);

	}

    // Ajoutez d'autres méthodes pour extraire des informations, etc.
}
