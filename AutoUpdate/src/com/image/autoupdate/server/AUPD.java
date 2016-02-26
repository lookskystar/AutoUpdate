/** *//********************************************************************
   * 项目名称                ：rochoc<p>
   * 包名称                  ：com.rochoc.autoupdate<p>
   * 文件名称                ：AutoUpdProtocol.java<p>
   * 编写者                 ：kfzx-luoc<p>
   * 编写日期                ：2008-12-22<p>
   * 程序功能（类）描述    ：<p>
   * 定义自动升级客户端与服务端的通讯协议
   * 程序变更日期            ：
  * 变更作者                ：
  * 变更说明                ：
 ********************************************************************/
 package com.image.autoupdate.server;
 
 /** *//**
  * @author kfzx-luoc
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class AUPD
 {
     /** *//**
      * 无意义操作
      */
     public static final String NONE = "NONE    ";
     
     /** *//**
      * 发送客户端版本信息
      */
     public static final String SEND_CLIENT_VERSION = "SENDCVER";
     
     /** *//**
      * 接收客户端版本信息
      */
     public static final String RECEIVED_CLIENT_VERSION = "RECDCVER";
     
     /** *//**
      * 发送文件全路径
      */
     public static final String SEND_FILE_ABSOULT = "SENDFILE";
     
     /** *//**
      * 接收文件全路径
      */
     public static final String RECEIVED_FILE_ABSOULT = "RECDFILE";
     
     /** *//**
      * 开始文件传输
      */
     public static final String START_TRANSMIT = "STARTTSM";
     
     /** *//**
      * 结束文件传输
      */
     public static final String TERMINATE_TRANSMIT = "TERMTSMT";
     
     /** *//**
      * 更新失败
      */
     public static final String UPDATED_FAILURE = "UPDEFAIL";
     
     /** *//**
      * 更新成功
      */
     public static final String UPDATED_SUCCESSFUL = "UPDESUCC";
    
     /** *//**
      * 无需更新
      */
     public static final String NOTNEED_UPDATED = "NNEEDUPD";
     
     /** *//**
      * 已经准备好接收更新文件
      */
     public static final String READY_TO_UPDATE = "READYTUP";
     
     /** *//**
      * 结束链接
      */
     public static final String BYE = "BYEBYEOK";
     
     /** *//**
      * 数据区OFFSET
      */
     public static final int DATA_OFFSET = 5;    
 
     /** *//**
      * 文件数据块大小
      */
     public static final int DATA_SIZE = 1024;
 
     /** *//**
      * 发送缓冲区大小
      */
     public static final int BUFFER_SIZE = DATA_SIZE + 1 + 4; // [0]位是标志位，区分数据和命令 + 4位长度
 
     /** *//**
      * 数据段标识
     */
    public static final int MARK_DATA_SECT = 0;
    /** *//**
     * 命令段标识
     */
    public static final int CMD_DATA_SECT = 1;

    /** *//**
     * 数据段结束标识
     */
    public static final int MARK_DATA_END = 127;
}
