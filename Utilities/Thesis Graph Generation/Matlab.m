b=0.0;
m=0.5;
bM=(b/2+1)*m;
b2M=(b+1)*m;
sumM3 =m*3;
sumM2=m*2;
sumB2=((b/2+1)*m)*2;
Q=[-(sumM3),m,m,m,0,0,0,0;
   m,-(m+sumB2),0,0,bM,bM,0,0;
   m,0,-(m+sumB2),0,bM,0,bM,0;
   m,0,0,-(m+sumB2),0,bM,bM,0;
   0,m,m,0,-(sumM2+b2M),0,0,b2M;
   0,m,0,m,0,-(sumM2+b2M),0,b2M;
   0,0,m,m,0,0,-(sumM2+b2M),b2M;
   0,0,0,0,m,m,m,-(sumM3)];

expm(Q*1000)
