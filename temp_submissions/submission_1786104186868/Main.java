import java.util.*;
class Main {
    public static void main(String[] args) {
     Scanner s=new Scanner(System.in);
        int m=(int)1e9+7;
        int n=s.nextInt();
        int arr[]=new int[n];
        for(int i=0;i<n-1;i++){
            arr[i]=s.nextInt();
        }
        long res=0;
        for(int i=0;i<n-1;i++){
            res=(res+arr[i])%m;
        }
        long temp1=n;
        long temp=((temp1*(temp1+1))/2)%m;
        long temp2=(temp-res+m)%m;
        System.out.println(temp2);
    }}