/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Reimi
 */
public class TimeTest {
    
    @Test
    public void testJdk8LocalDate() {
        
        LocalDate date = LocalDate.now();
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), date.toString());
    }
    
    @Test
    public void testJdk8ZoneId() {
        
        String zoneId = "America/New_York";
        assertEquals(zoneId, ZoneId.of(zoneId).toString());
    }
    
    @Test
    public void testInstant() {
        
        Instant instant = Instant.parse("2001-01-14T00:00:00Z");
        assertEquals(2001, instant.atZone(ZoneId.of("America/New_York")).getYear());
        
        long nowLong = new Date().getTime();
        Instant now = Instant.ofEpochMilli(nowLong);
        assertEquals(nowLong, now.toEpochMilli());
        
    }
    
    @Test
    public void testLocalDateTime() throws ParseException {
        
        Date date = new SimpleDateFormat("MM/dd/yy").parse("04/14/14");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2014, cal.get(Calendar.YEAR));
        assertEquals(03, cal.get(Calendar.MONTH));
        assertEquals(14, cal.get(Calendar.DATE));
        
        LocalDate lDate = LocalDate.parse("04/14/14", DateTimeFormatter.ofPattern("MM/dd/yy"));
        assertEquals(2014, lDate.getYear());
        assertEquals(Month.APRIL, lDate.getMonth());
        assertEquals(14, lDate.getDayOfMonth());
        
        LocalDateTime lDateTime = LocalDateTime.parse("2014-05-27T12:30:55", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertEquals(12, lDateTime.getHour());
        assertEquals(55, lDateTime.getSecond());
        assertEquals("MAY", lDateTime.getMonth().toString());
        assertEquals(DayOfWeek.TUESDAY, lDateTime.getDayOfWeek());
        
    }

    @Test
    public void testJdk8Stream1() {
        
        Predicate<Integer> greaterThan50 = (Integer i)->{
            return i>50;
        };
        
        getRandomList(50).stream().filter(greaterThan50).forEach(i->System.out.println(i));
    }
    
    @Test
    public void testJdk8Stream() {

        class Person {
            String name;
            int num;

            public Person(String name, int num) {
                this.name = name;
                this.num = num;
            }

            @Override
            public String toString() {
                return String.format("Person(%s, %s)", name, num);
            }
            
        }
        
        List<Person> people = Arrays.asList(new Person("A", 101),
                new Person("B", 10),new Person("A", 103),
                new Person("C", 11),new Person("K", 104),
                new Person("D", 21),new Person("B", 105),
                new Person("E", 44),new Person("E", 102),
                new Person("F", 66)
                );
        
        
        people.stream().collect(groupingBy(p->p.name)).forEach((name, list)->{
            System.out.println(name);
            System.out.println(list);
        });
    }
    
    List<Integer> getRandomList(int maxSize) {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < maxSize; i++) {
            list.add((int)(Math.random() * 100));
        }
        return list;
    }
}








