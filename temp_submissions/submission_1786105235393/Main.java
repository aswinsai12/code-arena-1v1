import java.util.*;
class Main {
    public static void main(String[] args) {
         Scanner s=new Scanner(System.in);
        int n=s.nextInt();
        long res=n;
        System.out.print(n+" ");
        while(res!=1&&res>0){
            if(res%2!=0){
                res=3*res+1;
                System.out.print(res+" ");
            }
            else{
                res=res/2;
                System.out.print(res+" ");
            }
        }
    }
}