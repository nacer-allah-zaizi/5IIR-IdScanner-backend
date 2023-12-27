package com.example.demo.Services;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.Model.File;
import com.example.demo.Repository.FileRepository;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private FileRepository fileRepository;

    public void storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Enregistrez les informations du fichier dans la base de données
        File uploadedFile = new File();
        uploadedFile.setFileaname(fileName);
        fileRepository.save(uploadedFile);
    }
    public File binarizeImage(MultipartFile imageFile) throws IOException {
        // Enregistrez l'image dans un fichier temporaire
        java.io.File tempImageFile = Files.createTempFile("temp-image", ".png").toFile();
        Files.copy(imageFile.getInputStream(), tempImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Utilisez OpenCV pour binariser l'image
        Mat originalMat = Imgcodecs.imread(tempImageFile.getAbsolutePath());
        Mat grayMat = new Mat();
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        Mat binaryMat = new Mat();
        Imgproc.threshold(grayMat, binaryMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // Convert back to BufferedImage
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", binaryMat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        BufferedImage binarizedImage = ImageIO.read(inputStream);

        // Utilisez binarizedImage pour l'OCR avec Tesseract
        return scanDocumentImage((MultipartFile) binarizedImage);
    }

    public File scanDocumentImage(MultipartFile imageFile) throws IOException {
        // Enregistrez l'image dans un fichier temporaire
        java.io.File tempImageFile = Files.createTempFile("temp-image", ".png").toFile();
        Files.copy(imageFile.getInputStream(), tempImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Utilisez Tesseract pour effectuer l'OCR sur l'image
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Users\\Dell\\Desktop\\PFA\\Tessract\\Tessdata\\");
        //tesseract.setLanguage("fra");
        tesseract.setLanguage("ara+fra");

        String text = null;
        
        try {
            text = tesseract.doOCR(tempImageFile);
            System.out.println("/////////////////////////////////////////////////////////////");
            System.out.println(text);
            System.out.println("/////////////////////////////////////////////////////////////");

        } catch (TesseractException e) {
            e.printStackTrace();
        }

        // Enregistrez les informations extraites dans l'entité File
        File scannedFile = new File();
        scannedFile.setFileaname("scanned-image.png"); // Nom de fichier généré après le scan
        scannedFile.setScannedText(text);
        fileRepository.save(scannedFile);

        // Supprimez l'image temporaire
        tempImageFile.delete();

        return scannedFile;
    }

  

   

    public File ajouterFile(File f) {
        return fileRepository.save(f);
    }

    public List<File> getFile() {
        return fileRepository.findAll();
    }

    public void modifierFile(File f) {
        Optional<File> r = fileRepository.findById((long) f.getId());

        if (r.isPresent()) {
            File c = r.get();
            c.setFileaname(f.getFileaname());
            fileRepository.save(c);
        }
    }

    public void SupprimerFile(int id) {
        fileRepository.deleteById((long) id);
    }
}
