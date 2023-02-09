package com.mai.Kindergarten.controllers.manager;

import com.mai.Kindergarten.models.*;
import com.mai.Kindergarten.repo.EducationDirectionRepository;
import com.mai.Kindergarten.repo.TalkManualRepository;
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
public class ManagerPanelTalkManualController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private TalkManualRepository talkManualRepository;
    @Autowired
    private EducationDirectionRepository educationDirectionRepository;


    @GetMapping("/manager/allTalkManuals")
    public String talkManualsList(Model model) {
        Iterable<TalkManual> allTalkManuals = talkManualRepository.findAll();
        model.addAttribute("allTalkManuals", allTalkManuals);
        return "manager/manual/allTalkManuals";
    }

    @GetMapping("/manager/allTalkManuals/talkManual-details/{id}")
    public String talkManualDetails(@PathVariable(value = "id") long id, Model model) throws UnsupportedEncodingException {
        if (!talkManualRepository.existsById(id)) {
            return "redirect:/manager/allTalkManuals";
        }
        TalkManual talkManual = talkManualRepository.findById(id).orElseThrow();
        model.addAttribute("talkManual", talkManual);
        return "manager/manual/talkManual-details";
    }

    @PostMapping("/manager/allTalkManuals/talkManual-details/{id}/remove")
    public String delete_talkManual(@PathVariable(value = "id") long id, Model model) {
        TalkManual talkManual = talkManualRepository.findById(id).orElseThrow();
        talkManualRepository.delete(talkManual);
        return "redirect:/manager/allTalkManuals";
    }

    @GetMapping("/manager/allTalkManuals/talkManual-details/{id}/download")
    public ResponseEntity<Object> downloadFile_Talk(@PathVariable(value = "id") Long id, Model model) throws Exception {
        TalkManual talkManual = talkManualRepository.findById(id).orElseThrow();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(talkManual.getFile()));
        HttpHeaders headers = new HttpHeaders();
        String nameFileDownload = Transcriptor.transliterate(talkManual.getFileName());
        //Либо так оставляем либо имя файла в латиницу переводим
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", nameFileDownload));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object>
                responseEntity = ResponseEntity.ok().headers(headers).contentLength(talkManual.getFile().length).contentType(
                MediaType.parseMediaType("application/all")).body(resource);
        return responseEntity;
    }

    @GetMapping("/manager/editTalkManual/{id}")
    public String talkManualEdit(@PathVariable(value = "id") long id, Model model) {
        if (!talkManualRepository.existsById(id)) {
            return "redirect:/manual/allManuals";
        }
        TalkManual talkManual = talkManualRepository.findById(id).orElseThrow();
        model.addAttribute("talkManual", talkManual);
        Iterable<EducationDirection> allEducationDirections = educationDirectionRepository.findAll();
        model.addAttribute("allEducationDirections", allEducationDirections);
        return "manager/manual/editTalkManual";
    }

    @PostMapping("/manager/editTalkManual/{id}")
    public String editTalkManual(@PathVariable(value = "id") long id,
                             @RequestParam String inputName, @RequestParam Long inputEducationDirection,
                             @RequestParam("inputFileField") MultipartFile file, Model model) throws IOException {
        TalkManual talkManual = talkManualRepository.findById(id).orElseThrow();
        talkManual.setName(inputName);
        EducationDirection educationDirection = educationDirectionRepository.findById(inputEducationDirection).orElseThrow();
        talkManual.setEducationDirection(educationDirection);
        if (!file.isEmpty()) {
            talkManual.setFile(file.getBytes());
            talkManual.setFileName(file.getOriginalFilename());
        }
        talkManualRepository.save(talkManual);
        return "redirect:/manager/allTalkManuals/talkManual-details/{id}";
    }

}
