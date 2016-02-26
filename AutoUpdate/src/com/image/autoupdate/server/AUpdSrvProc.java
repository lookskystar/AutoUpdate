 package com.image.autoupdate.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
 
 /**
  * �Զ����·���˴������
  */
 public class AUpdSrvProc extends Thread
 {
     private Socket socket = null;
     private InputStream socketIn;
     private OutputStream socketOut;    
     private Config config = Config.getInstance();//�����ļ�����
     private ClientVerParser cvPaser = null;
     private Map<String, UpdFile> cFiles = new HashMap<String,UpdFile>();
     
     byte bFlag [] = new byte[1];//��ʶλ
     byte bCmd[] = new byte[8];//����
     
     public AUpdSrvProc(Socket socket)
     {
         this.socket = socket;
     }    
    /**
      * ���տͻ��˵��������󣬲����д���
      */
     public void run()
     {
         try
         {
             config.refresh();//���¸���������Ϣ
             socketIn = socket.getInputStream();
             socketOut = socket.getOutputStream();
             while(true)
             {
                 //��ȡ��־λ
                 int len = socketIn.read(bFlag,0,1);
                 if(len!=1)
                 {
                     Config.print(socket.getInetAddress()+":��ȡ��ʶλʧ��");
                     socketOut.write(Config.getCmd(AUPD.BYE));//����
                     break;
                 }
                 if(bFlag[0]==AUPD.CMD_DATA_SECT)//������
                 {
                     len = socketIn.read(bCmd,0,8);
                     if(len!=8)
                     {
                         Config.print(socket.getInetAddress()+":��ȡ����ʧ��,CMD="+bCmd);
                         socketOut.write(Config.getCmd(AUPD.BYE));//����
                         break;
                     }
                     if(Config.parseCmd(bCmd).equals(AUPD.READY_TO_UPDATE))//�ͻ����Ѿ�׼���ø�����
                     {
                         Config.print(socket.getInetAddress()+":�ͻ����Ѿ�׼���ý��ո����ļ�");
                         int ret = sendUpdateFile();
                         switch(ret)
                         {
                             case 0:
                                 socketOut.write(Config.getCmd(AUPD.UPDATED_FAILURE));//ʧ��
                             break;
                             case 1:
                                 socketOut.write(Config.getCmd(AUPD.UPDATED_SUCCESSFUL));//�ɹ�
                             break;
                             default:
                                 socketOut.write(Config.getCmd(AUPD.NOTNEED_UPDATED));//�������
                             break;
                         }
                     }else if(Config.parseCmd(bCmd).equals(AUPD.BYE))//��������
                     {
                         socketOut.write(Config.getCmd(AUPD.BYE));//����
                         break;
                     }
                 }else if(bFlag[0]==AUPD.MARK_DATA_SECT || bFlag[0]==AUPD.MARK_DATA_END)//��������
                {
                    if(Config.parseCmd(bCmd).equals(AUPD.SEND_CLIENT_VERSION))//���а汾��Ϣ���մ���
                   {
                        receiveClientVer(bFlag[0]);
                   }else
                    {
                        Config.print("���ַ���������,"+new String(bCmd));
                        socketOut.write(Config.getCmd(AUPD.BYE));//����
                        break;
                    }
                }else
                {
                    Config.print(socket.getInetAddress()+":��������ʶλ,"+bFlag[0]);
                    socketOut.write(Config.getCmd(AUPD.BYE));//����
                    break;
                }
            }//END while(ture)
            //�ر���Դ
            socketIn.close();
            socketOut.close();
            socket.close();
        } catch (IOException e){
        	e.printStackTrace();
            Config.print("����ͻ�����������ʧ��,"+socket.getInetAddress()+","+e);
        }        
    }
    /**
     * ���͸����ļ�
     * @return 0.����ʧ�� 1.���³ɹ� 2.�������
     */
    private int sendUpdateFile()
    {
        try
        {
            //���������Ϳͻ��˰汾���Ƿ�һ�£����һ�´ǣ�����������
            if (config.getVerstion().equals(cvPaser.getVerstion()))
            {           
                Config.print(socket.getInetAddress()+":�汾һ�£��������");                
                return 2;
            }
            //��ʼ���д���
            UpdFile srvFiles [] = config.getFiles();
            boolean isSuccess = true;
            for(int i=0;i<srvFiles.length;i++)
            {
                UpdFile cf = (UpdFile)cFiles.get(srvFiles[i].getName());
                //�ļ������ڻ�汾�Ų�һ������Ҫ���¸��ļ�
                if(cf==null || !cf.getVersion().equals(srvFiles[i].getVersion()))
                {
                    if(!sendFile(srvFiles[i]))
                    {
                        isSuccess = false;
                    }                    
                }
            }//END for
            //���Ͱ汾��Ϣ�ļ������͸�����Ϣ�ļ�
            if(isSuccess)
            {
                UpdFile verFile = new UpdFile("autoupdate.xml");
                verFile.setPath("." + File.separator + "config");
                verFile.setType(0);
               verFile.setVersion(config.getVerstion());
                if(!sendFile(verFile))
                {
                    Config.print(socket.getInetAddress()+":���Ͱ汾�ļ�ʧ��");
                    return 0;
                }
                //���͸�����Ϣ
                UpdFile infFile = new UpdFile("history.htm");
                infFile.setPath("." + File.separator + "config");
                infFile.setType(0);
                infFile.setVersion(config.getVerstion());
                if(!sendFile(infFile))
                {
                    Config.print(socket.getInetAddress()+":����������Ϣʧ��");
                }
                return 1;
            }else
            {
               return 0;
            }
        }catch(Exception e)
        {
            Config.print("������Ҫ�����ļ�ʧ��,"+e);
            e.printStackTrace();
            return 0;
        }
    }
    /**
     * ���͸����ļ�·��
     * @param path
     * @return 0.ʧ�� 1.�ɹ�
     */
    private boolean sendFileAbsPath(String path)
    {
        try
        {
            byte buffer[] = new byte[AUPD.BUFFER_SIZE];
            int len = 0;
            //��ʶΪ���ݶ�
            buffer[0] = AUPD.MARK_DATA_SECT;
            Config.copyArray(buffer, Config.getLen(path.getBytes().length), 1, 0, 4);//4λ����
            //������ݰ�
            for (int i = 0; i < path.getBytes().length; i++)
                buffer[i + 5] = path.getBytes()[i];
            socketOut.write(buffer, 0, path.getBytes().length + 5);//ǰ��λΪͷ��1λ��ʶ+4λ����
            //��ʶΪ���ݶ��ѽ�������������������
            buffer[0] = AUPD.MARK_DATA_END;
            socketOut.write(buffer, 0, 1);
            socketOut.flush();
            //���ͻ����Ƿ��յ�            
            len = socketIn.read(bFlag,0,1);
            if(len!=1)
            {
                Config.print(socket.getInetAddress()+":��ȡ��ʶλʧ��");
                socketOut.write(Config.getCmd(AUPD.BYE));//����
                return false;
            }
            //��ȡ����
           len = socketIn.read(bCmd,0,8);
            if(len!=8)
            {
                Config.print(socket.getInetAddress()+":��ȡ����ʧ��,CMD="+bCmd);
                socketOut.write(Config.getCmd(AUPD.BYE));//����
                return false;
            }
            if(Config.parseCmd(bCmd).equals(AUPD.RECEIVED_FILE_ABSOULT))//�ɹ�
            {
                Config.print(socket.getInetAddress()+":�����ļ�·���ɹ�,"+path);
                return true;
            }else if(Config.parseCmd(bCmd).equals(AUPD.BYE))//ʧ��
            {
                Config.print(socket.getInetAddress()+":�����ļ�·��ʧ�ܣ�"+path);
                return false;
            }else//�쳣
            {
                return false;
            }            
        }catch(Exception e)
        {
            Config.print(socket.getInetAddress()+":�����ļ�·��ʧ��,"+path);
            e.printStackTrace();
            return false;
        }
    }
    /**
     * ���͸����ļ�
     * @param file
     * @return false.ʧ�� true.�ɹ�
     */
    private boolean sendFile(UpdFile file)
    {
       try
       {
           File f = new File(Config.formatPath(file.getPath())+file.getName());
           if(!f.exists()||!f.isFile())
           {
               Config.print(file+",�����ڣ��޷�����");
               return false;
           }
           Config.print(socket.getInetAddress()+":��ʼ�����ļ�>>"+file);
           socketOut.write(Config.getCmd(AUPD.SEND_FILE_ABSOULT));//�����ļ�ȫ·��
           String fileAbsPath = Config.formatPath(file.getPath())+file.getName();
           if(!sendFileAbsPath(fileAbsPath))
          {
               return false;
           }
           socketOut.write(Config.getCmd(AUPD.START_TRANSMIT));//��ʼ����
           FileInputStream fin = new FileInputStream(f);
           //�ļ����ݻ�����
           byte[] data = new byte[AUPD.DATA_SIZE];
           // �������ݻ�����
           byte[] buffer = new byte[AUPD.BUFFER_SIZE];
           int len = -1;
           while ((len=fin.read(data)) != -1)
            {
                // ��ʶΪ���ݶ�
                buffer[0] = AUPD.MARK_DATA_SECT;
                Config.copyArray(buffer,Config.getLen(len),1,0,4);//��ų���
                // ������ݰ�
                for (int i=0; i<len; i++)
                    buffer[i+5] = data[i];
                socketOut.write(buffer,0,len+5);
            }
           // ��ʶΪ���ݶ��ѽ�������������������
            buffer[0] = AUPD.MARK_DATA_END;            
          socketOut.write(buffer,0,1);
            socketOut.flush();
            fin.close();
            //�жϿͻ����Ƿ��յ�
            len = socketIn.read(bFlag,0,1);
            if(len!=1)
            {
                Config.print(socket.getInetAddress()+":��ȡ��ʶλʧ��");
                socketOut.write(Config.getCmd(AUPD.BYE));//����
                return false;
            }
            //��ȡ����
            len = socketIn.read(bCmd,0,8);
            if(len!=8)
            {
                Config.print(socket.getInetAddress()+":��ȡ����ʧ��,CMD="+new String(bCmd));
                socketOut.write(Config.getCmd(AUPD.BYE));//����
               return false;
           }
            if(Config.parseCmd(bCmd).equals(AUPD.TERMINATE_TRANSMIT))//�ɹ�
            {
                Config.print(socket.getInetAddress()+":�����ļ�'"+file+"'�ɹ�");
                return true;
            }else if(Config.parseCmd(bCmd).equals(AUPD.BYE))//ʧ��
            {
                Config.print(socket.getInetAddress()+":�����ļ�ʧ�ܣ�"+file);
                return false;
            }else//�쳣
            {
                Config.print(socket.getInetAddress()+":�����ļ��쳣��"+file+","+new String(bCmd));
                return false;
           } 
       }catch(Exception e)
       {
           Config.print("�����ļ�'"+file+"'ʧ��,"+e);
           e.printStackTrace();
           return false;
       }
    }
    
    /**
     * ���տͻ��˰汾
     * @param flag
     */
    private void receiveClientVer(byte flag)//��һλ��ʾ���������ݻ��ǽ�������
    {
        try
        {
            //�������ݻ�����
            byte flagb[] = new byte[1];//��־
            byte lenb [] = new byte[4];//����
            //���հ汾����Ϣ
            StringBuffer strBuf = new StringBuffer();//���ڽ�����Ϣ
            int len = -1;
           boolean isFirst = true;
           boolean isOk = false;        
            flagb[0] = flag;
            while(true)
            {
                //��һ��
                if(isFirst)
                {
                    isFirst = false;        
                }else
                {
                    len = socketIn.read(flagb,0,1);//��ȡ��ʶλ
                    if(len != 1)
                    {
                        Config.print(socket.getInetAddress() + ":��ȡ���ݱ�ʶλʧ��");
                        break;
                    }
                }
                //��ȡ���ݳ���
                if(flagb[0]==AUPD.MARK_DATA_SECT)
               {
                    len = socketIn.read(lenb, 0, 4);
                    if (len != 4)
                    {
                        Config.print(socket.getInetAddress() + ":��ȡ����ͷ��ʧ��");
                        break;
                    }
                }
                if (flagb[0] == AUPD.MARK_DATA_SECT)//��������
                {
                   int cLen = Integer.parseInt(new String(lenb, 0, 4));//�������ݳ���
                    byte data[] = new byte[cLen];
                    len = socketIn.read(data, 0, cLen);
                   int totLen = len;
                    while (totLen < cLen)//����λҪ���ض�ȡ
                    {
                        strBuf.append(new String(data, 0, len));
                       len = socketIn.read(data, 0, cLen - totLen);
                        totLen = totLen + len;
                    }
                    strBuf.append(new String(data, 0, len));
                }else if(flagb[0]==AUPD.MARK_DATA_END)//���ݽ���
                {
                    isOk = true;
                    break;
                }else
                {
                    Config.print(socket.getInetAddress()+":�յ�����������,"+new String(flagb,0,1)+"<<");
                    break;
                }
            }//END while(true)
            if(isOk)//�ɹ�
            {
                socketOut.write(Config.getCmd(AUPD.RECEIVED_CLIENT_VERSION));//��ʱ����
                Config.print("���տͻ���" + socket.getInetAddress() + " �汾��Ϣ�ɹ�");
                cvPaser = new ClientVerParser(new StringReader(strBuf
                        .toString()));
                UpdFile[] files = cvPaser.getFiles();
                for (int i = 0; i < files.length; i++)
                {
                    cFiles.put(files[i].getName(), files[i]);
                }
            }else//ʧ��
            {
                socketOut.write(Config.getCmd(AUPD.BYE));//����
            }
        }catch(Exception e)
        {
            Config.print("���տͻ���"+socket.getInetAddress()+" �汾����Ϣ����ʧ��,"+e);
            e.printStackTrace();
        }
    }    
}