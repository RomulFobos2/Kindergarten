package com.mai.Kindergarten.controllers.user;

import com.mai.Kindergarten.models.Child;
import com.mai.Kindergarten.models.Diploma;
import com.mai.Kindergarten.models.User;
import com.mai.Kindergarten.repo.ChildRepository;
import com.mai.Kindergarten.repo.DiplomaRepository;
import com.mai.Kindergarten.repo.UserRepository;
import com.mai.Kindergarten.service.TemplateCreator;
import com.mai.Kindergarten.service.Transcriptor;
import com.mai.Kindergarten.service.UserService;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

//Класс для обработки страниц пользователя(воспитателя) для работы с дипломами
@Controller
public class UserPanelDiplomaController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private DiplomaRepository diplomaRepository;
    @Autowired
    private ChildRepository childRepository;

    @GetMapping("/user/allDiplomas")
    public String diplomasList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        Iterable<Diploma> allDiplomas = diplomaRepository.findAll().stream().filter(x -> x.getUser().getId().equals(currentUser.getId())).collect(Collectors.toList());
        model.addAttribute("allDiplomas", allDiplomas);
        return "user/diploma/allDiplomas";
    }

    @GetMapping("/user/addDiploma")
    public String addDiploma(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());
        if (user.getGroup() != null) {
            List<Child> allChildren = user.getGroup().getChildren();
            model.addAttribute("allChildren", allChildren);
        }
        model.addAttribute("user", user);
        return "user/diploma/addDiploma";
    }

    @PostMapping("/user/addDiploma")
    public String addDiploma(@RequestParam String inputLastName, @RequestParam String inputFirstName,
                             @RequestParam String inputPatronymicName, @RequestParam String inputDiplomaText,
                             @RequestParam String strDate, @RequestParam String place,
                             @RequestParam long inputChild,
                             @RequestParam("inputFileField") MultipartFile file, Model model) throws IOException {
        SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat_inner.parse(strDate);
        } catch (ParseException e) {
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());
        Child child = null;
        if(inputChild != 0){
            child = childRepository.findById(inputChild).orElseThrow();
        }
        Diploma diploma;
        if (child == null) {
            diploma = new Diploma(inputLastName, inputFirstName, inputPatronymicName, inputDiplomaText, place, date, file.getBytes(), file.getOriginalFilename(), user);
        } else {
            diploma = new Diploma(inputLastName, inputFirstName, inputPatronymicName, inputDiplomaText, place, date, file.getBytes(), file.getOriginalFilename(), user, child);
        }
        diplomaRepository.save(diploma);
        return "redirect:/user/allDiplomas";
    }

    @GetMapping("/user/allDiplomas/diploma-details/{id}")
    public String diplomaDetails(@PathVariable(value = "id") long id, Model model) throws UnsupportedEncodingException {
        if (!diplomaRepository.existsById(id)) {
            return "redirect:/user/allDiplomas";
        }
        Diploma diploma = diplomaRepository.findById(id).orElseThrow();
        model.addAttribute("diploma", diploma);
        //Добавляем картинку в виде объекта на страницу
        byte[] bytes = diplomaRepository.findById(id).orElseThrow().getFile();
        byte[] encodeBase64 = Base64.encode(bytes);
        String base64Encoded = new String(encodeBase64, "UTF-8");
        model.addAttribute("image", base64Encoded);
        return "user/diploma/diploma-details";
    }

    @PostMapping("/user/allDiplomas/diploma-details/{id}/remove")
    public String delete_diploma(@PathVariable(value = "id") long id, Model model) {
        Diploma diploma = diplomaRepository.findById(id).orElseThrow();
        diplomaRepository.delete(diploma);
        return "redirect:/user/allDiplomas";
    }

    //Скачивание диплома
    @GetMapping("/user/allDiplomas/diploma-details/{id}/download")
    public ResponseEntity<Object> downloadFile(@PathVariable(value = "id") Long id, Model model) throws Exception {
        Diploma diploma = diplomaRepository.findById(id).orElseThrow();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(diploma.getFile()));
        HttpHeaders headers = new HttpHeaders();
        String nameFileDownload = Transcriptor.transliterate(diploma.getFileName());
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", nameFileDownload));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object>
                responseEntity = ResponseEntity.ok().headers(headers).contentLength(diploma.getFile().length).contentType(
                MediaType.parseMediaType("application/txt")).body(resource);
        return responseEntity;
    }

    @GetMapping("/user/editDiploma/{id}")
    public String diplomaEdit(@PathVariable(value = "id") long id, Model model) {
        if (!diplomaRepository.existsById(id)) {
            return "redirect:/user/allDiplomas";
        }
        Diploma diploma = diplomaRepository.findById(id).orElseThrow();
        model.addAttribute("diploma", diploma);
        return "user/diploma/editDiploma";
    }

    @PostMapping("/user/editDiploma/{id}")
    public String editDiploma(@PathVariable(value = "id") long id,
                              @RequestParam String inputLastName, @RequestParam String inputFirstName,
                              @RequestParam String inputPatronymicName, @RequestParam String inputDiplomaText,
                              @RequestParam String strDate, @RequestParam String place,
                              @RequestParam("inputFileField") MultipartFile file, Model model) throws IOException {
        SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat_inner.parse(strDate);
        } catch (ParseException e) {
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());

        Diploma diploma = diplomaRepository.findById(id).orElseThrow();
        diploma.setLastName(inputLastName);
        diploma.setFirstName(inputFirstName);
        diploma.setPatronymicName(inputPatronymicName);
        diploma.setText(inputDiplomaText);
        diploma.setDate(date);
        diploma.setPlace(place);
        if (!file.isEmpty()) {
            diploma.setFile(file.getBytes());
            diploma.setFileName(file.getOriginalFilename());
        }
        diplomaRepository.save(diploma);
        return "redirect:/user/allDiplomas/diploma-details/{id}";
    }


    @PostMapping("/user/diploma-report")
    public ResponseEntity<Object> report(@RequestParam String resultFromInput, @RequestParam String resultToInput,
                                         Model model, HttpServletRequest request) throws IOException, Docx4JException, JAXBException, ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());

        SimpleDateFormat simpleDateFormat_PeriodInner = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat simpleDateFormat_PeriodOut = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        final Date dateFrom = simpleDateFormat_PeriodInner.parse(resultFromInput);
        final Date dateTo = simpleDateFormat_PeriodInner.parse(resultToInput);
        dateTo.setHours(23);
        dateTo.setMinutes(59);
        String strDateFrom = simpleDateFormat_PeriodOut.format(dateFrom);
        String strDateTo = simpleDateFormat_PeriodOut.format(dateTo);
        System.out.println("strDateFrom - " + strDateFrom);
        System.out.println("strDateTo - " + strDateTo);
        //Дипломы педагога
        List<Diploma> allDiplomas = diplomaRepository.findAll().stream()
                .filter(x -> x.getChild() == null)
                .filter(x -> x.getUser().getId() == currentUser.getId())
                .filter(x -> x.getDate().getTime() >= dateFrom.getTime())
                .filter(x -> x.getDate().getTime() <= dateTo.getTime())
                .collect(Collectors.toList());
        //Дипломы детей , в которых педагог помогал
        List<Diploma> allChildrenDiplomas = diplomaRepository.findAll().stream()
                .filter(x -> x.getChild() != null)
                .filter(x -> x.getUser().getId() == currentUser.getId())
                .filter(x -> x.getDate().getTime() >= dateFrom.getTime())
                .filter(x -> x.getDate().getTime() <= dateTo.getTime())
                .collect(Collectors.toList());

        Date date = new Date();
        //Формируем документ
        WordprocessingMLPackage wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_7.docx");
        SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("«dd» MMMM yyyy");
        String strDate = simpleDateFormat_out.format(date);
        TemplateCreator.replacePlaceholder(wordDocument, strDate, "date");
        TemplateCreator.replacePlaceholder(wordDocument, strDateTo, "dateTo");
        TemplateCreator.replacePlaceholder(wordDocument, strDateFrom, "dateFrom");
        TemplateCreator.replacePlaceholder(wordDocument, currentUser.getFullName(), "userFullName");
        TemplateCreator.replacePlaceholder(wordDocument, currentUser.getFullName(), "userFullName");
        TemplateCreator.addUserDiplomaRow(wordDocument, allDiplomas, "num");
        TemplateCreator.addUserChildDiplomaRow(wordDocument, allChildrenDiplomas, "numChild");
        return TemplateCreator.downloadReport(wordDocument, "Report");
    }


}
