import java.util.*;
class Main {
    public static void main(String[] args) {
         Scanner s=new Scanner(System.in);
        int n=s.nextInt();
        int arr[]=new int[n];
        for(int i=0;i<n;i++){
            arr[i]=s.nextInt();
        }
        long ans=0;
        for(int i=1;i<n;i++){
            if(arr[i]<arr[i-1]){
                ans+=(arr[i-1]-arr[i]);
                arr[i]=arr[i-1];
            }
        }
        System.out.println(ans-1);
    }

}