 package com.image.autoupdate.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
import java.net.Socket;
 
 /**
  * �Զ�����--�����������
  */
 public class AutoUpdateServer extends Thread
 {
     private int port = 0;//����˿ں�
     private Config config = Config.getInstance();//�����ļ�����
     private ServerSocket srvSocket = null;
     public AutoUpdateServer()
     {
         port = Integer.parseInt(config.getServerPort());
         try
         {
             srvSocket = new ServerSocket(port);
             this.start();
             Config.print("�Զ����·������ڶ˿�'"+port+"'����");
         } catch (IOException e)
         {
             e.printStackTrace();
         }
     }
     void setTimeout(int millis) throws IOException
     {
         if (srvSocket != null)
         {
             srvSocket.setSoTimeout(millis);
         }
     }
     void close() throws IOException
     {
         if (srvSocket != null)
         {
             srvSocket.close();
         }
    }
     /**
      * ִ�м�����������пͻ����������������ж��Ƿ���Ҫ���£�
      * �����Ҫ���£�����ͻ��˴������°汾�ļ�
      */
	public void run()
     {
         try
        {
             while (true) 
             {
                 Socket clSocket = null;
                 try
                 {
                     clSocket = srvSocket.accept();
                     Config.print("�ͻ��ˡ�"+clSocket.getInetAddress()+"�����ӳɹ�");
                     //���д���
                     AUpdSrvProc srvP = new AUpdSrvProc(clSocket);
                     srvP.start();
                 } catch (IOException ioe)
                 {
                     try
                     {
                    	 if(clSocket!=null){
                    		 clSocket.close();
                    	 }
                     } catch (IOException e1)
                     {
                     }
                     Config.print("AutoUpdateServer proc client:"+clSocket.getInetAddress().getHostAddress()+" error,"+ioe);
                     ioe.printStackTrace();
                 }
             }
         }catch(Exception e)
         {
             Config.print("AutoUpdateServer running error,"+e);
             e.printStackTrace();
         } finally
         {
             try
             {
                 srvSocket.close();
             } catch (IOException e)
            {
            }
        }
    }    
    //����������
    public static void main(String args[])
    {
        AutoUpdateServer server = new AutoUpdateServer();
        server.run();
    }
}

