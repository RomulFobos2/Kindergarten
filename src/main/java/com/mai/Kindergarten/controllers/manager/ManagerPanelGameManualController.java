package com.mai.Kindergarten.controllers.manager;

import com.mai.Kindergarten.models.EducationDirection;
import com.mai.Kindergarten.models.GameManual;
import com.mai.Kindergarten.repo.EducationDirectionRepository;
import com.mai.Kindergarten.repo.GameManualRepository;
import com.mai.Kindergarten.repo.UserRepository;
import com.mai.Kindergarten.service.Transcriptor;
import com.mai.Kindergarten.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

//Класс для обработки страниц пользователя(воспитателя) для работы с дипломами
@Controller
public class ManagerPanelGameManualController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private GameManualRepository gameManualRepository;
    @Autowired
    private EducationDirectionRepository educationDirectionRepository;


    @GetMapping("/manager/allGameManuals")
    public String gameManualsList(Model model) {
        Iterable<GameManual> allGameManuals = gameManualRepository.findAll();
        model.addAttribute("allGameManuals", allGameManuals);
        return "manager/manual/allGameManuals";
    }

    @GetMapping("/manager/allGameManuals/gameManual-details/{id}")
    public String gameManualDetails(@PathVariable(value = "id") long id, Model model) throws UnsupportedEncodingException {
        if (!gameManualRepository.existsById(id)) {
            return "redirect:/manager/allGameManuals";
        }
        GameManual gameManual = gameManualRepository.findById(id).orElseThrow();
        model.addAttribute("gameManual", gameManual);
        return "manager/manual/gameManual-details";
    }

    @PostMapping("/manager/allGameManuals/gameManual-details/{id}/remove")
    public String delete_gameManual(@PathVariable(value = "id") long id, Model model) {
        GameManual gameManual = gameManualRepository.findById(id).orElseThrow();
        gameManualRepository.delete(gameManual);
        return "redirect:/manager/allGameManuals";
    }

    @GetMapping("/manager/allGameManuals/gameManual-details/{id}/download")
    public ResponseEntity<Object> downloadFile_Game(@PathVariable(value = "id") Long id, Model model) throws Exception {
        GameManual gameManual = gameManualRepository.findById(id).orElseThrow();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(gameManual.getFile()));
        HttpHeaders headers = new HttpHeaders();
        String nameFileDownload = Transcriptor.transliterate(gameManual.getFileName());
        //Либо так оставляем либо имя файла в латиницу переводим
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", nameFileDownload));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object>
                responseEntity = ResponseEntity.ok().headers(headers).contentLength(gameManual.getFile().length).contentType(
                MediaType.parseMediaType("application/all")).body(resource);
        return responseEntity;
    }

    @GetMapping("/manager/editGameManual/{id}")
    public String gameManualEdit(@PathVariable(value = "id") long id, Model model) {
        if (!gameManualRepository.existsById(id)) {
            return "redirect:/manual/allGameManuals";
        }
        GameManual gameManual = gameManualRepository.findById(id).orElseThrow();
        model.addAttribute("gameManual", gameManual);
        Iterable<EducationDirection> allEducationDirections = educationDirectionRepository.findAll();
        model.addAttribute("allEducationDirections", allEducationDirections);
        return "manager/manual/editGameManual";
    }

    @PostMapping("/manager/editGameManual/{id}")
    public String editGameManual(@PathVariable(value = "id") long id,
                                 @RequestParam String inputName,
                                 @RequestParam String inputTypeGame,
                                 @RequestParam Long inputEducationDirection,
                                 @RequestParam("inputFileField") MultipartFile file, Model model) throws IOException {
        GameManual gameManual = gameManualRepository.findById(id).orElseThrow();
        gameManual.setName(inputName);
        gameManual.setType(inputTypeGame);
        EducationDirection educationDirection = educationDirectionRepository.findById(inputEducationDirection).orElseThrow();
        gameManual.setEducationDirection(educationDirection);
        if (!file.isEmpty()) {
            gameManual.setFile(file.getBytes());
            gameManual.setFileName(file.getOriginalFilename());
        }
        gameManualRepository.save(gameManual);
        return "redirect:/manager/allGameManuals/gameManual-details/{id}";
    }

}
