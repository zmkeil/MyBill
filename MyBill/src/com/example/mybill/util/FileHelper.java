package com.example.mybill.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

public class FileHelper {

    private static final String TAG = "FileHelper";
    private Context mContext;
 
    public FileHelper(Context _mContext) {
        mContext = _mContext;
    }
    
 // 在手机本地硬盘中保存信息
    public void save(String fileName, String content) {
 
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = mContext.openFileOutput(fileName,
                    Context.MODE_PRIVATE);	// 覆写
            fileOutputStream.write(content.getBytes());
 
        } catch (FileNotFoundException e) {
        	Log.i(TAG, "" + e);
            e.printStackTrace();
        } catch (IOException e) {
        	Log.i(TAG, "" + e);
            e.printStackTrace();
        } finally {
            try {
 
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
            	Log.i(TAG, "" + e);
                e.printStackTrace();
            }
        }
    }
 
    // 在手机本地硬盘中保存信息
    public void append(String fileName, String content) {
 
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = mContext.openFileOutput(fileName,
                    Context.MODE_APPEND);	// Context.MODE_APPEND
            fileOutputStream.write(content.getBytes());
 
        } catch (FileNotFoundException e) {
        	Log.i(TAG, "" + e);
            e.printStackTrace();
        } catch (IOException e) {
        	Log.i(TAG, "" + e);
            e.printStackTrace();
        } finally {
            try {
 
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
            	Log.i(TAG, "" + e);
                e.printStackTrace();
            }
        }
    }
 
    // 读取手机硬盘中保存的文件
    public String read(String fileName) {
        FileInputStream fileInputStream = null;
        String content = null;
        try {
            fileInputStream = mContext.openFileInput(fileName);
            int len = 0;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
            while ((len = fileInputStream.read(buffer)) != -1) {
                byteArrayInputStream.write(buffer, 0, len);
            }
            content = new String(byteArrayInputStream.toByteArray());
            //Log.i(TAG, string);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
 
        return content;
    }
    
    public String read_ex(String filename) {
    	File file = new File(filename);
		FileInputStream finStream;
		String content = null;
		try {
			finStream = new FileInputStream(file);
			byte[] bt = new byte[finStream.available()];
			finStream.read(bt);
			content = new String(bt);
			finStream.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
    }
    
    // 判断文件是否存在
    public boolean fileIsExists(String strFile) {  
        try {  
            File f=new File("/data/data/com.example.mybill/files/" + strFile);  
            if(!f.exists()) { 
            	Log.i(TAG, "file not exists");
            	return false;  
            } 
        } catch (Exception e) { 
        	Log.i(TAG, "file construct exception");
            return false;  
        }    
        
        return true;  
    }  
    
    public boolean deleteFile(String file) {
    	try {  
            File f=new File("/data/data/com.example.mybill/files/" + file);  
            if(!f.exists()) { 
            	Log.i(TAG, "file not exists");
            	return true;  
            } 
            f.delete();
        } catch (Exception e) { 
        	Log.i(TAG, "file construct exception");
            return false;  
        }    

        return true;  
    }
    
}
