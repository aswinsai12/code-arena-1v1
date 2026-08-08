import java.util.*;
public class Main {
 
    public static void main(String[] args) {
        Scanner s=new Scanner(System.in);
        String s1=s.next();
        long count=1;
        long max=1;
        for(int i=1;i<s1.length();i++){
            if(s1.charAt(i)==s1.charAt(i-1)){
                count++;
            }
            else{
                max=Math.max(count,max);
                count=1;
            }
        }
          max=Math.max(count,max);
          System.out.println(max);
    }
}
