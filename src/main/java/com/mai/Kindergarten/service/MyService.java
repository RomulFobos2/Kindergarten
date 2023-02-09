package com.mai.Kindergarten.service;

import com.mai.Kindergarten.models.EducationDirection;
import com.mai.Kindergarten.models.Lesson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MyService {


    public static String[][] getTimeWeekArray(){
        //Формируем двумерный массив, столбцы это время, строки это денб недели (пн-пт)
        String result[][] = new String[5][35];
        for(int i = 0; i < 5; i++){
            int hour = 9;
            int minute = 0;
            for(int j = 0; j < 35; j++){
                result[i][j] = "" + String.format("%02d", hour) + ":" + String.format("%02d", minute);
                minute += 5;
                if(minute == 60){
                    hour++;
                    minute = 0;
                }
            }
        }
        return result;
    }

    //Сбираем id уроков в дне неделе.
    //Собираем все уникальные id уроков
    public static Set<String> getLessonInDay(String[] day){
        Set<String> lessons = new HashSet<>();
        for(String s : day){
            if(!s.contains(":")){
                String[] allIDFromCell = s.split(",");
                for(String lesson_id : allIDFromCell){
                    lessons.add(lesson_id);
                }
            }
        }
        return lessons;
    }

    //Урок музыки ставим всегда вторым для группы, чтобы выполнялось очередность неподвижный->подвижный урок
    //Передаем день (строку с масива который сформировали с getTimeWeekArray,
    //В дне ищем свободное время для урока с продолжительностью durationLesson+1, это позволяет сразу занять место у урока под перерыв
    public static ArrayList<Integer> freeTimeForMusicLesson(String[] day, int durationLesson){
        //Хранит координаты начала и конца времени в дне.
        durationLesson++; //увеличиваем на 1, чтобы занять уроком еще и ячейку под перерыв.
        ArrayList<Integer> result = new ArrayList<>();
        int count = 0;
        //for(int i = 0; i < day.length; i++){
        for(int i = durationLesson; i < day.length; i++){
            //Если в ячейке с временем, нет знака ':', значит там стоит id урока, которое заняло это время
            if(!day[i].contains(":")){
                count = 0;
                result.clear();
            }
            else {
                count++;
                result.add(i);
                if(count == durationLesson){
                    return result;
                }
            }
        }
        return result;
    }

    //Ставит урок в любое свободное место
    public static ArrayList<Integer> freeTimeForLesson(String[] day, int durationLesson){
        //Хранит координаты начала и конца времени в дне.
        durationLesson++; //увеличиваем на 1, чтобы занять уроком еще и ячейку под перерыв.
        ArrayList<Integer> result = new ArrayList<>();
        int count = 0;
        for(int i = 0; i < day.length; i++){
            //Если в ячейке с временем, нет знака ':', значит там стоит id урока, которое заняло это время
            if(!day[i].contains(":")){
                count = 0;
                result.clear();
            }
            else {
                count++;
                result.add(i);
                if(count == durationLesson){
                    return result;
                }
            }
        }
        return result;
    }


    //Передаем день (строку с масива который сформировали с getTimeWeekArray в котором уже присутствуют уроки музыки других групп и уроки физры,
    //В дне ищем свободное время для урока с продолжительностью durationLesson+1, это позволяет сразу занять место у урока под перерыв
    public static ArrayList<Integer> freeTimeForPhysicalCultureLesson(String[] day, int durationLesson, List<Lesson> allPhysicalCultureLessons, Long ageGroup_id){
        //В массиве будем хранить все ID уроков физры. Будем его использовать для проверки, есть ли в ячейки урок физры,
        // чтобы достать потом о нем инфу - принадлежит ли урок тоейже возрастной группе что и урок для которого сейчас ищем место или нет
        List<String> allIdPhysicalCultureLesson = new ArrayList<>();
        for(Lesson physicalCultureLesson : allPhysicalCultureLessons){
            allIdPhysicalCultureLesson.add(String.valueOf(physicalCultureLesson.getId()));
        }
        //Хранит координаты начала и конца времени в дне.
        durationLesson++; //увеличиваем на 1, чтобы занять уроком еще и ячейку под перерыв.
        ArrayList<Integer> result = new ArrayList<>();
        int count = 0;
        //Урок физры всегда идет вторым для группы, поэтому для каждой группы,
        // его начинаем отсчитывать после как бы первого урока (т.е. начина с индекса durationLesson, который как раз соответствует одному уроку для данной группы и ее возрастной группе
        for(int i = durationLesson; i < day.length; i++){
            //Если в ячейке с временем, нет знака ':', значит там стоит id урока или уроков
            //может стоять и урок физры и урок музыки
            if(!day[i].contains(":")){
                String[] allIDFromCell = day[i].split(",");
                boolean check = false; //Есть урок физры другой возрастной группы в ячейки?
                for (String id_anyLesson : allIDFromCell){
                    if(allIdPhysicalCultureLesson.contains(id_anyLesson)){
                        Long current_ageGroup_id = allPhysicalCultureLessons.stream().filter(x -> x.getId() == Long.parseLong(id_anyLesson)).collect(Collectors.toList()).get(0).getEducationDirection().getAgeGroup().getId();
                        if(current_ageGroup_id != ageGroup_id){
                            check = true; //В ячейки есть id урока физкультуры другой возрастной группы
                        }
                    }
                }
                if(check){
                    count = 0;
                    result.clear();
                }
                else {
                    count++;
                    result.add(i);
                    if(count == durationLesson){
                        return result;
                    }
                }
            }
            else {
                count++;
                result.add(i);
                if(count == durationLesson){
                    return result;
                }
            }
        }
        return result;
    }


    //Есть ли на данном дне неделе для группы урок.
    //Если да, значит нужно следующий сделать через 2 дня. ТОЛЬКО ДЛЯ УРОКОВ МУЗЫКИ, которы как раз 2 в неделю.
    public static boolean getPrevLessonForGroup(List<Lesson> allMusicLessons, Long group_id, String[] day){
        Set<String> lessons = getLessonInDay(day);
        List<Lesson> allMusicLessonsGroup = allMusicLessons.stream().filter(x -> x.getGroup().getId() == group_id).collect(Collectors.toList());
//        System.out.println("Кол-во уроков музыки у группу = " + allMusicLessonsGroup.size() + "(ВСЕГДА ДОЛЖНЫ БЫТЬ 2)");
        boolean result = false;
        for(Lesson lesson : allMusicLessonsGroup){
            if(lessons.contains(String.valueOf(lesson.getId()))){
                result = true;
            }
        }
        return result;
    }

    //Есть ли на данном дне неделе есть урок ФИЗРЫ текущей группы
    public static boolean getPrevLessonForPhysicalCulture(List<Lesson> allPhysicalCultureLessons, Long group_id, String[] day){
        //Получаю id всех уроков, которые есть в дне неделе (могут быть уроки музыки и уроки физры, среди id)
        Set<String> lessons = getLessonInDay(day);
        //В метод пришел массив с уроками физкультуры всех групп. Из них оставляем, только те уроки физры, id которых нашли в массиве дня
        List<Lesson> physicalCultureLessonOnDay = allPhysicalCultureLessons.stream().filter(x -> lessons.contains(String.valueOf(x.getId()))).collect(Collectors.toList());
        //Если среди уроков физкультуры, которые взяли с дня недели, есть урок, у которого ageGroup_id совпадает с переданной, то возращаме тру
        for (Lesson physicalCultureLesson : physicalCultureLessonOnDay){
            if(physicalCultureLesson.getGroup().getId() == group_id){
                return true;
            }
        }
        return false;
    }

    //Есть ли урок для переданного напрваления развития в этом дне
    public static boolean haveLessonByEducationDirection(List<Lesson> groupLessons, Long educationDirection_id, String[] day){
        Set<String> lessonsInTheDay = getLessonInDay(day);
        for(Lesson lesson : groupLessons){
            if(lessonsInTheDay.contains(String.valueOf(lesson.getId()))){
                if(lesson.getEducationDirection().getId() == educationDirection_id){
                    return true;
                }
            }

        }
        return false;
    }

    //Беру каждую ячейку массива, вытаскиваю от туда id уроков и смотрю принадлежат они переданной группе или нет
    //Если нет, то убираем их с массива. На выходе будет массив с уроками которые принадлежат только группе id которой передали
    public static void cleanForGroup(String[][] workWeekForGroup, Long group_id, List<String> idGroupLessons){
        //Из этого массива будем брать время, чтобы поместить в пустые ячейки
        String[][] forTime = getTimeWeekArray();
        for(int i = 0; i < workWeekForGroup.length; i++){
            for(int j = 0; j < workWeekForGroup[i].length; j++){
                String cell = workWeekForGroup[i][j];
                if(!cell.contains(":")){
                    String[] id_Lessons = cell.split(",");
                    for(String id_Lesson : id_Lessons){
                        if(!idGroupLessons.contains(id_Lesson)){
                            workWeekForGroup[i][j] = workWeekForGroup[i][j].replaceAll(id_Lesson, "");
                        }
                    }
                    workWeekForGroup[i][j] = workWeekForGroup[i][j].replaceAll(",", "");
                    if (workWeekForGroup[i][j].equals("")){
                        workWeekForGroup[i][j] = forTime[i][j];
                    }
                }
            }
        }
    }

    //Берем коллекцию с уроками и чередуем в ней уроки с разными направлениями развития
    public static List<Lesson> sortAndGroupByEducationDirection(List<Lesson> allHardLessons){
        List<Lesson> result = new ArrayList<>();
        Lesson lesson = allHardLessons.get(0);
        result.add(lesson);
        long idEducationDirection_prevLesson = lesson.getEducationDirection().getId();
        allHardLessons.remove(0);
        while (allHardLessons.size() > 0){
            lesson = allHardLessons.get(0);
            int countDifferentEducationDirection = getCountDifferentEducationDirection(allHardLessons);
            System.out.println("Кол-во разных направлений - " + countDifferentEducationDirection);
            if(countDifferentEducationDirection > 1){
                int index = 1;
                while (idEducationDirection_prevLesson == lesson.getEducationDirection().getId()){
                    lesson = allHardLessons.get(index);
                    index++;
                }
                idEducationDirection_prevLesson = lesson.getEducationDirection().getId();
            }
            result.add(lesson);
            allHardLessons.remove(lesson);
        }
        return result;
    }

    //Узнать сколько разных направлений развития есть в коллекции с уроками
    public static int getCountDifferentEducationDirection(List<Lesson> allHardLessons){
        Set<EducationDirection> allEducationDirections = new HashSet<>();
        for(Lesson lesson : allHardLessons){
            allEducationDirections.add(lesson.getEducationDirection());
        }
        return allEducationDirections.size();
    }


    public static void print(String[][] workWeek){
        System.out.println("*************************************");
        for(int i = 0; i < workWeek.length; i++){
            for(int j = 0; j < workWeek[i].length; j++){
                if(j < 34){
                    System.out.print(workWeek[i][j] + " ; ");
                }
                else {
                    System.out.print(workWeek[i][j]);
                }
            }
            System.out.println();
        }
        System.out.println("*************************************");
    }

    public static String getDayOfWeek(int index){
        switch (index){
            case 0 : return "Понедельник";
            case 1 : return "Вторник";
            case 2 : return "Среда";
            case 3 : return "Четверг";
            case 4 : return "Пятница";
            default: return "Вся неделя";
        }
    }

    public static int getDayOfWeek(String day){
        switch (day){
            case "Понедельник" : return 0;
            case "Вторник" : return 1;
            case "Среда" : return 2;
            case "Четверг" : return 3;
            case "Пятница" : return 4;
            default: return -1;
        }
    }

    public static String[][] deepCopy(String[][] src, String[][] dst){
        if(src.length != dst.length){
            System.out.println("Ошибка в размерах копируемых массивов");
            return null;
        }
        for(int i = 0; i < src.length; i++){
            for(int j = 0; j < src[i].length; j++){
                if(src[i].length != dst[i].length){
                    System.out.println("Ошибка в размерах копируемых массивов");
                    return null;
                }
            }
        }
        for(int i = 0; i < src.length; i++){
            for(int j = 0; j < src[i].length; j++){
                dst[i][j] = src[i][j];
            }
        }
        return dst;
    }

    //Метод использую чтобы динамически чертить таблицу на странице allLessons.html )
    public static List<String> getForThymeleafList(int size){
        List<String> result = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            result.add("");
        }
        return result;
    }


    public static List<String> getWorkHour(){
        List<String> workHours = new ArrayList<>();
        for(int i = 9; i < 18; i++){
            workHours.add(String.format("%02d", i));
        }
        return workHours;
    }

    public static String getWorkHour(int index){
        List<String> workHours = new ArrayList<>();
        for(int i = 9; i < 18; i++){
            workHours.add(String.format("%02d", i));
        }
        return workHours.get(index);
    }

    public static List<String> getWorkMinute(){
        List<String> workMinute = new ArrayList<>();
        for(int i = 0; i < 60; i = i + 5){
            workMinute.add(String.format("%02d", i));
        }
        return workMinute;
    }

    public static String getWorkMinute(int index){
        List<String> workMinute = new ArrayList<>();
        for(int i = 0; i < 60; i = i + 5){
            workMinute.add(String.format("%02d", i));
        }
        return workMinute.get(index);
    }

}
