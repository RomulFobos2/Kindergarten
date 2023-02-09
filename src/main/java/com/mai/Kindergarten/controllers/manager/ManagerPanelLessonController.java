package com.mai.Kindergarten.controllers.manager;

import com.mai.Kindergarten.models.*;
import com.mai.Kindergarten.repo.*;
import com.mai.Kindergarten.service.MyService;
import com.mai.Kindergarten.service.TemplateCreator;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

//Класс для обработки страниц менеджера для работы с направлениями развития
@Controller
public class ManagerPanelLessonController {
    @Autowired
    EducationAreaRepository educationAreaRepository;
    @Autowired
    EducationIndicatorRepository educationIndicatorRepository;
    @Autowired
    EducationDirectionRepository educationDirectionRepository;
    @Autowired
    AgeGroupRepository ageGroupRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    LessonRepository lessonRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/manager/allLessons")
    public String lessonList(Model model, HttpServletRequest request) throws ParseException {
        List<String> weekDays = new ArrayList<>();
        weekDays.add("Понедельник");
        weekDays.add("Вторник");
        weekDays.add("Среда");
        weekDays.add("Четверг");
        weekDays.add("Пятница");
        List<Group> allGroups = groupRepository.findAll();
        List<Integer> fiveRows = new ArrayList<>();
        List<Integer> threeRows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            fiveRows.add(i);
        }
        for (int i = 0; i < 2; i++) {
            threeRows.add(i);
        }
        model.addAttribute("allGroups", allGroups);
        model.addAttribute("weekDays", weekDays);
        MyService myService = new MyService();
        model.addAttribute("myService", myService);
        List<String> workHour = MyService.getWorkHour();
        model.addAttribute("workHour", workHour);
        List<String> workMinute = MyService.getWorkMinute();
        model.addAttribute("workMinute", workMinute);

        model.addAttribute("maxIDGroup", allGroups.get(allGroups.size() - 1).getId());

        //Если хотябы у одного урока нету времени, вызываем метод автоматического формирования расписания.
        boolean checkNeedCallScheduleLessons = false;
        for (Lesson lesson : lessonRepository.findAll()) {
            if (lesson.getTimeOfStartLesson() == null | lesson.getTimeOfEndLesson() == null) {
                checkNeedCallScheduleLessons = true;
            }
        }
        if (checkNeedCallScheduleLessons) {
            generateScheduleLessons();
        }
        return "manager/lesson/allLessons";
    }

    @PostMapping("/manager/allLessons")
    public ResponseEntity<Object> saveCreateScheduleReport(@RequestParam List<Long> inputLesson,
                                           @RequestParam List<Integer> inputStartHour, @RequestParam List<Integer> inputStartMinute,
                                           @RequestParam List<Integer> inputEndHour, @RequestParam List<Integer> inputEndMinute,
                                           Model model, HttpServletRequest request) throws ParseException, JAXBException, Docx4JException, CloneNotSupportedException, IOException {
        int allCountLesson = lessonRepository.findAll().size();
        System.out.println("inputLesson - " + inputLesson.size());
        int indexDay = 0;
        for (int i = 0; i < inputLesson.size(); i++) {
            if (indexDay % 5 == 0) {
                indexDay = 0;
            }
            if(inputLesson.get(i) == null){
                indexDay++;
                System.out.println("Пропуск");
                continue;
            }
            if(inputLesson.get(i) == -1){
                indexDay++;
                System.out.println("Пропуск по -1");
                continue;
            }
            String dayWeek = MyService.getDayOfWeek(indexDay);
            Lesson lesson = lessonRepository.findById(inputLesson.get(i)).orElseThrow();
            SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String strStartLesson = MyService.getWorkHour(inputStartHour.get(i)) + ":" + MyService.getWorkMinute(inputStartMinute.get(i));
            String strEndLesson = MyService.getWorkHour(inputEndHour.get(i)) + ":" + MyService.getWorkMinute(inputEndMinute.get(i));
            Date startLesson = simpleDateFormat_inner.parse(strStartLesson);
            Date endLesson = simpleDateFormat_inner.parse(strEndLesson);

            lesson.setTimeOfStartLesson(startLesson);
            lesson.setTimeOfEndLesson(endLesson);
            lesson.setDayWeek(dayWeek);
            lessonRepository.save(lesson);
            indexDay++;
        }
        return scheduleLesson(model);
        //return "redirect:/manager/allLessons";
    }


    public ResponseEntity<Object> scheduleLesson(Model model) throws IOException, Docx4JException, JAXBException, CloneNotSupportedException, ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());
        List<Group> allGroups = groupRepository.findAll();

        Date date = new Date();
        SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("«dd» MMMM yyyy");
        String strDate = simpleDateFormat_out.format(date);
        WordprocessingMLPackage wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_9.docx");
        TemplateCreator.replacePlaceholder(wordDocument, user.getFullName(), "userFullName");
        TemplateCreator.replacePlaceholder(wordDocument, strDate, "date");

        TemplateCreator.addForScheduleGroupRow(wordDocument, allGroups, "groupName");

        return TemplateCreator.downloadReport(wordDocument, "Report");
    }


    //Метод для автоматического формирования расписания
    public void generateScheduleLessons() throws ParseException {
        List<Group> allGroups = groupRepository.findAll();
        EducationArea musicArea = educationAreaRepository.findByName("Музыка");
        EducationArea physicalCultureArea = educationAreaRepository.findByName("Физическое развитие");

        //Берем все уроки музыки и начнем их расспределять
        List<Lesson> allMusicLessons = lessonRepository.findAll().stream().filter(x -> x.getEducationDirection().getEducationArea().getId() == musicArea.getId()).collect(Collectors.toList());
        //Сортируем уроки, первый тот у кого младше возрастная группа
        Collections.sort(allMusicLessons, new Comparator<Lesson>() {
            @Override
            public int compare(Lesson o1, Lesson o2) {
                return Integer.parseInt(o1.getEducationDirection().getAgeGroup().getChildrenMaxAge()) - Integer.parseInt(o2.getEducationDirection().getAgeGroup().getChildrenMaxAge());
            }
        });
        String[][] workWeekFor_Music_PhysicalCulture = MyService.getTimeWeekArray();
        String[][] workWeekImmutable = MyService.getTimeWeekArray();
        System.out.println("Начинаем добавлять уроки музыки");
        for (Lesson musicLesson : allMusicLessons) {
            //Лимит кол-ва уроков музыки в день для всего Садика
            int limitOnDay = 3;
            //Сколько длится урок у группы
            int duration = musicLesson.getGroup().getAgeGroup().getDurationLesson();
            //Сколько ячеек массива займем в массиве workWeekFor_Music_PhysicalCulture
            int durationLessonCell = (duration / 5) + 1; //10-минутный урок будет занимать три ячейки ( 10 делим на 5 + 1) (09:00, 09:05, 09:10)

            //i = День недели от 0 до 4 - понедельник - пятница
            for (int i = 0; i < workWeekFor_Music_PhysicalCulture.length; i++) {
                //Берем день недели из массива
                String[] day = workWeekFor_Music_PhysicalCulture[i];
                //Если в текущий день неделе для группы которой принадлежит текущий урок Музыки, уже есть урок Музыки, значит ставим новый урок через 2 дня
                if (MyService.getPrevLessonForGroup(allMusicLessons, musicLesson.getGroup().getId(), day)) {
                    i = i + 2;
                    continue;
                }
                //Если в нем уже есть limitOnDay урока, переходим к следующему дню, чтобы сильно не загружать учителя Музыки
                if (MyService.getLessonInDay(day).size() >= limitOnDay) {
                    continue;
                }
                //Устанавливаем id урока в массив который принадлежит дню неделе и получаем координаты ячеек с временем
                ArrayList<Integer> indexLessonTimeList = MyService.freeTimeForMusicLesson(day, durationLessonCell);
                //Устанавливаем время уроку музыкы
                if (indexLessonTimeList.size() > 0) {
                    for (int indexLessonTime : indexLessonTimeList) {
                        workWeekFor_Music_PhysicalCulture[i][indexLessonTime] = String.valueOf(musicLesson.getId());
                    }
                    //СОбираем дату, чтобы установить ее для урока
                    //используем массив workWeekImmutable в которых хранится время
                    SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date startLesson = simpleDateFormat_inner.parse(workWeekImmutable[i][indexLessonTimeList.get(0)]);
                    //Для времени окончания урока, берем предпоследний индекс. Т.к. последний отвечает за время перерыва
                    Date endLesson = simpleDateFormat_inner.parse(workWeekImmutable[i][indexLessonTimeList.get(indexLessonTimeList.size() - 2)]);
                    musicLesson.setTimeOfStartLesson(startLesson);
                    musicLesson.setTimeOfEndLesson(endLesson);
                    musicLesson.setDayWeek(MyService.getDayOfWeek(i));
                    lessonRepository.save(musicLesson);
                    break;
                }
            }
        }

        MyService.print(workWeekFor_Music_PhysicalCulture);
        System.out.println("Начинаем добавлять уроки физры");

        //Берем все уроки физкультуры и начнем их расспределять
        List<Lesson> allPhysicalCultureLessons = lessonRepository.findAll().stream().filter(x -> x.getEducationDirection().getEducationArea().getId() == physicalCultureArea.getId()).collect(Collectors.toList());
        //Сортируем уроки, первый тот у кого младше возрастная группа
        Collections.sort(allPhysicalCultureLessons, new Comparator<Lesson>() {
            @Override
            public int compare(Lesson o1, Lesson o2) {
                return Integer.parseInt(o1.getEducationDirection().getAgeGroup().getChildrenMaxAge()) - Integer.parseInt(o2.getEducationDirection().getAgeGroup().getChildrenMaxAge());
            }
        });
        for (Lesson physicalCultureLesson : allPhysicalCultureLessons) {
            int duration = physicalCultureLesson.getGroup().getAgeGroup().getDurationLesson();
            //Сколько ячеек массива займем в массиве workWeekFor_Music_PhysicalCulture
            int durationLessonCell = (duration / 5) + 1; //10-минутный урок будет занимать три ячейки ( 10 делим на 5 + 1) (09:00, 09:05, 09:10)
            for (int i = 0; i < workWeekFor_Music_PhysicalCulture.length; i++) {
                //Берем день недели из массива
                String[] day = workWeekFor_Music_PhysicalCulture[i];
                //Если для группы, которой принадлежит текущий урок физкультуры, в этот день недели есть урок музыки, берем следующий день.
                if (MyService.getPrevLessonForGroup(allMusicLessons, physicalCultureLesson.getGroup().getId(), day)) {
                    continue;
                }
                //Если в текущий день недели уже есть урок физры у текущей группы, то берем следующий день
                if (MyService.getPrevLessonForPhysicalCulture(allPhysicalCultureLessons, physicalCultureLesson.getGroup().getId(), day)) {
                    System.out.println("ПРопускаем урок физры, т.к есть уже у этой группы тут урок");
                    continue;
                }
                //При проверке занятых времен, учитывая предыдущую проверку, можно говорить, что если мы встретили id урока, в массиве дня недели,
                //то это либо id урока Музыки другой группы и на это место можно ставить урок физкультуры
                //либо это урок физкультуры, другой возрастной группы, и мы ставим урок после них
                //Устанавливаем id урока в массив который принадлежит дню неделе и получаем координаты ячеек с временем
                ArrayList<Integer> indexLessonTimeList = MyService.freeTimeForPhysicalCultureLesson(day, durationLessonCell, allPhysicalCultureLessons, physicalCultureLesson.getEducationDirection().getAgeGroup().getId());
                if (indexLessonTimeList.size() > 0) {
                    System.out.println("Записываем урок физры");
                    for (int indexLessonTime : indexLessonTimeList) {
                        if (workWeekFor_Music_PhysicalCulture[i][indexLessonTime].contains(":")) {
                            workWeekFor_Music_PhysicalCulture[i][indexLessonTime] = String.valueOf(physicalCultureLesson.getId());
                        } else {
                            workWeekFor_Music_PhysicalCulture[i][indexLessonTime] = workWeekFor_Music_PhysicalCulture[i][indexLessonTime] + "," + String.valueOf(physicalCultureLesson.getId());
                        }
                    }
                    //СОбираем дату, чтобы установить ее для урока
                    //используем массив workWeekImmutable в которых хранится время
                    SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date startLesson = simpleDateFormat_inner.parse(workWeekImmutable[i][indexLessonTimeList.get(0)]);
                    //Для времени окончания урока, берем предпоследний индекс. Т.к. последний отвечает за время перерыва
                    Date endLesson = simpleDateFormat_inner.parse(workWeekImmutable[i][indexLessonTimeList.get(indexLessonTimeList.size() - 2)]);
                    physicalCultureLesson.setTimeOfStartLesson(startLesson);
                    physicalCultureLesson.setTimeOfEndLesson(endLesson);
                    physicalCultureLesson.setDayWeek(MyService.getDayOfWeek(i));
                    lessonRepository.save(physicalCultureLesson);
                    break;
                }
            }
        }
        MyService.print(workWeekFor_Music_PhysicalCulture);

        //workWeekFor_Music_PhysicalCulture Хранит УРОКИ МУЗЫКИ И ФИЗРЫ

        //К этому этапу у нас имеется массив с часами и днями неделями, в котором стоят id уроков Музыки и Физкультуры всех групп
        //Теперь нам, нужно брать этотм массив отдельно дла каждой группы и вычленять от туда уроки, которые не принадлежат этой группе.
        //Должен получиться масси с уроками музыки и физры для конкретной группы.
        for (Group group : allGroups) {
            System.out.println("********СТавим тяжеые УРОКИ*****************");
            String[][] workWeekForGroup = new String[5][35];
            int duration = group.getAgeGroup().getDurationLesson();
            int durationLessonCell = (duration / 5) + 1;
            workWeekForGroup = MyService.deepCopy(workWeekFor_Music_PhysicalCulture, workWeekForGroup);
            System.out.println("Группа - " + group.getGroupName());
            //Формируем массив, убирая из него id уроков, которые принадлежат другим группам
            MyService.cleanForGroup(workWeekForGroup, group.getId(), group.getLessons().stream().map(x -> String.valueOf(x.getId())).collect(Collectors.toList()));
            MyService.print(workWeekForGroup);
            //Берем все сложные уроки. Они будут ставиться во вторник, среда, четверг
            List<Lesson> allHardLessons = group.getLessons().stream().filter(x -> x.getEducationDirection().isHard()).collect(Collectors.toList());
            //Сортирую уроки, так, чтобы направления развития у них чередоавались. Таким образом не будет например две математики два дня подряд.
            allHardLessons = MyService.sortAndGroupByEducationDirection(allHardLessons);
            int indexDay = 1;
            //Начинаме заполнять вторник, среду, четрвег сложными уроками, пока они не закончатся в коллекции
            while (allHardLessons.size() > 0) {
                //Начинаме заполнять со вторника
                String[] day = workWeekForGroup[indexDay];
                Lesson currentHardLesson = allHardLessons.get(0);

                //Если урок с таким направлением развития уже есть в дне неделе, то начиаем для урока искать место в следующем дне неделе
                if (MyService.haveLessonByEducationDirection(allHardLessons, currentHardLesson.getEducationDirection().getId(), day)) {
                    indexDay++;
                    if (indexDay == 4) {
                        indexDay = 1;
                    }
                    continue;
                }
                //Иначе вписываем урок в этот день в свободное место
                ArrayList<Integer> indexLessonTimeList = MyService.freeTimeForLesson(day, durationLessonCell);
                if (indexLessonTimeList.size() > 0) {
                    System.out.println("Записываем тяжелый урок");
                    for (int indexLessonTime : indexLessonTimeList) {
                        workWeekForGroup[indexDay][indexLessonTime] = String.valueOf(currentHardLesson.getId());
                    }
                    //СОбираем дату, чтобы установить ее для урока
                    //используем массив workWeekImmutable в которых хранится время
                    SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date startLesson = simpleDateFormat_inner.parse(workWeekImmutable[indexDay][indexLessonTimeList.get(0)]);
                    //Для времени окончания урока, берем предпоследний индекс. Т.к. последний отвечает за время перерыва
                    Date endLesson = simpleDateFormat_inner.parse(workWeekImmutable[indexDay][indexLessonTimeList.get(indexLessonTimeList.size() - 2)]);
                    currentHardLesson.setTimeOfStartLesson(startLesson);
                    currentHardLesson.setTimeOfEndLesson(endLesson);
                    currentHardLesson.setDayWeek(MyService.getDayOfWeek(indexDay));
                    lessonRepository.save(currentHardLesson);
                }
                allHardLessons.remove(currentHardLesson);
                indexDay++;
                if (indexDay == 4) {
                    indexDay = 1;
                }
            }
            MyService.print(workWeekForGroup);
            System.out.println("Заполняем легкие уроки");
            //Начинаем заполнять дни недели всеми оставшимися уроками (легкими)
            List<Lesson> allEasyLessons = group.getLessons().stream().filter(x -> !x.getEducationDirection().isHard()).filter(x -> x.getEducationDirection().getEducationArea().getId() != musicArea.getId()).filter(x -> x.getEducationDirection().getEducationArea().getId() != physicalCultureArea.getId()).collect(Collectors.toList());
            allEasyLessons = MyService.sortAndGroupByEducationDirection(allEasyLessons);
            //Тут логика такая, легкие уроки начинаем вставлять сначала в понедельник, следующий ставим в пятницу,
            //следующая итерация будет уже искать тот день, в котором меньше всего уроков и ставить урок туда, если везде одинаково, то
            //начинать уже по порядку понедельник, вторник, среда и т.д.
            allEasyLessons = MyService.sortAndGroupByEducationDirection(allEasyLessons);
            indexDay = 0;
            boolean oneCircle = true; //признак, того что один раз ужа назначили уроки в порядке понедельник -> пятница
            while (allEasyLessons.size() > 0) {
                String[] day = workWeekForGroup[indexDay];
                Lesson currentEasyLesson = allEasyLessons.get(0);

                //Если урок с таким направлением развития уже есть в дне неделе, то начиаем для урока искать место в следующем дне неделе
                if (MyService.haveLessonByEducationDirection(allEasyLessons, currentEasyLesson.getEducationDirection().getId(), day)) {
                    indexDay++;
                    if (indexDay == 5) {
                        indexDay = 0;
                    }
                    continue;
                }
                //Если в дне неделе уже макимально кол-во уроков, то берем следующий день, при этом
                //если следуюищй день понедельник, значит мы уже сделали круг и значит уроков больше чем должно быть, нужно выдать ошибку!
                boolean errorCountLesson = false;
                if (MyService.getLessonInDay(day).size() >= group.getAgeGroup().getMaxCountLesson()) {
                    indexDay++;
                    if (indexDay == 5) {
                        errorCountLesson = true;
                        break;
                    }
                    continue;
                }
                //Иначе вписываем урок в этот день в свободное место
                ArrayList<Integer> indexLessonTimeList = MyService.freeTimeForLesson(day, durationLessonCell);
                if (indexLessonTimeList.size() > 0) {
                    System.out.println("Записываем легкий урок");
                    for (int indexLessonTime : indexLessonTimeList) {
                        workWeekForGroup[indexDay][indexLessonTime] = String.valueOf(currentEasyLesson.getId());
                    }
                    //СОбираем дату, чтобы установить ее для урока
                    //используем массив workWeekImmutable в которых хранится время
                    SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date startLesson = simpleDateFormat_inner.parse(workWeekImmutable[indexDay][indexLessonTimeList.get(0)]);
                    //Для времени окончания урока, берем предпоследний индекс. Т.к. последний отвечает за время перерыва
                    Date endLesson = simpleDateFormat_inner.parse(workWeekImmutable[indexDay][indexLessonTimeList.get(indexLessonTimeList.size() - 2)]);
                    currentEasyLesson.setTimeOfStartLesson(startLesson);
                    currentEasyLesson.setTimeOfEndLesson(endLesson);
                    currentEasyLesson.setDayWeek(MyService.getDayOfWeek(indexDay));
                    lessonRepository.save(currentEasyLesson);
                }
                allEasyLessons.remove(currentEasyLesson);
                System.out.printf("Индекс дня для легкий уроков - " + indexDay);
                if (indexDay == 0 & oneCircle) {
                    indexDay = 4;
                    oneCircle = false;
                } else {
                    indexDay++;
                    if (indexDay == 5) {
                        indexDay = 0;
                    }
                }
            }
            MyService.print(workWeekForGroup);
            System.out.println("*************************");
        }
    }



    @GetMapping("/manager/allLessons/removeLesson")
    public String removeLesson() throws ParseException {
        for(Lesson lesson : lessonRepository.findAll()){
            lessonRepository.delete(lesson);
        }

        List<Group> allGroups = groupRepository.findAll();
        for(Group group : allGroups){
            if(group.getLessons().size() == 0){
                List<EducationDirection> educationDirectionsForCurrentGroup = educationDirectionRepository.findAll()
                        .stream().filter(x -> x.getAgeGroup().getId() == group.getAgeGroup().getId()).filter(x -> x.isHaveLesson()).collect(Collectors.toList());
                //В коллекции храним ID направлений, для которых процесс формирования уроков нужно пропустить
                //т.к. у этого направление было общее кол-во уроков с направление, по которому формирование уроков уже прошло
                List<Long> breakID = new ArrayList<>();
                for(EducationDirection educationDirection : educationDirectionsForCurrentGroup){
                    String lessonName = educationDirection.getName();
                    if(breakID.contains(educationDirection.getId())){
                        continue;
                    }
                    if(educationDirection.getEducationDirectionTotalCountLesson() != null){
                        breakID.add(educationDirection.getEducationDirectionTotalCountLesson().getId());
                        lessonName += " / " + educationDirection.getEducationDirectionTotalCountLesson().getName();
                    }
                    for(int i = 0; i < educationDirection.getCountLesson(); i++){
                        Lesson lesson = new Lesson(educationDirection, group, lessonName);
                        group.getLessons().add(lesson);
                        lessonRepository.save(lesson);
                    }
                }
//                //Отдельно добавляем Музыку
//                for(int i = 0; i < 2; i++){
//                    Lesson lesson = new Lesson("Музыка", group);
//                    group.getLessons().add(lesson);
//                    lessonRepository.save(lesson);
//                }
                groupRepository.save(group);
            }
        }

        return "redirect:/manager/allLessons";
    }
}
