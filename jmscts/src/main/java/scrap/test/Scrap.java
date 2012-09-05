package scrap.test;

import java.util.TreeSet;

public class Scrap {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(Scrap.class.getCanonicalName());
        System.out.println(Scrap.class.getSimpleName());
        
        TreeSet<Long> l = new TreeSet<Long>();
        l.add(5l);
        l.add(2l);
        l.add(-12l);
        System.out.println("First:"+l.first());
        System.out.println("Last:"+l.last());
    }

}
