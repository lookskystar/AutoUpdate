 package com.image.autoupdate.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
import java.net.Socket;
 
 /**
  * 自动升级--服务端主程序
  */
 public class AutoUpdateServer extends Thread
 {
     private int port = 0;//服务端口号
     private Config config = Config.getInstance();//配置文件对像
     private ServerSocket srvSocket = null;
     public AutoUpdateServer()
     {
         port = Integer.parseInt(config.getServerPort());
         try
         {
             srvSocket = new ServerSocket(port);
             this.start();
             Config.print("自动更新服务器在端口'"+port+"'监听");
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
      * 执行监听处理，如果有客户端连接上来，则判断是否需要更新，
      * 如果需要更新，则给客户端传送最新版本文件
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
                     Config.print("客户端‘"+clSocket.getInetAddress()+"’连接成功");
                     //进行处理
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
    //测试主函数
    public static void main(String args[])
    {
        AutoUpdateServer server = new AutoUpdateServer();
        server.run();
    }
}

