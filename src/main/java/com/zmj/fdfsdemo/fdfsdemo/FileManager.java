package com.zmj.fdfsdemo.fdfsdemo;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * https://github.com/happyfish100/fastdfs-client-java
 * @Author: 周美军
 * @Date: 2019/2/28 15:17
 * @Email: 536304123@QQ.COM
 */
@Component
public class FileManager {
    private static TrackerClient trackerClient;
    private static TrackerServer trackerServer;
    private static StorageServer storageServer;
    private static StorageClient1 storageClient;

    private static final String GROUP_NAME = "group1";

    private static final String IMG_SERVER = "http://img.zmjmall.com";

    static {
        try {
            ClientGlobal.init("config/fdfs_client.conf");
          /*  trackerClient = new TrackerClient(ClientGlobal.g_tracker_group);
            trackerServer = trackerClient.getConnection();
            storageServer= trackerClient.getStoreStorage(trackerServer);
            storageClient = new StorageClient(trackerServer, storageServer);*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 并发问题下
     * trackerServer每次用完都会关闭,storageClient获取不到链接,导致多图上传出现bug  -- 2019.03.28增加方法
     *
     * @param group_name
     * @return
     * @throws IOException
     * @throws MyException
     */
    protected static boolean newWritableStorageConnection(String group_name) throws IOException, MyException {
        if (storageClient != null) {
            return false;
        } else {
            TrackerClient tracker = new TrackerClient(ClientGlobal.g_tracker_group);
            trackerServer = tracker.getConnection();
            if(StringUtils.isEmpty(group_name)) {
                storageServer = tracker.getStoreStorage(trackerServer);
            }else {
                storageServer = tracker.getStoreStorage(trackerServer, group_name);
            }
            storageClient = new StorageClient1(trackerServer, storageServer);
            if (storageServer == null) {
                throw new MyException("getStoreStorage fail, errno code: " + tracker.getErrorCode());
            }
            return true;
        }
    }
    protected static boolean newWritableStorageConnection() throws IOException, MyException {
       return newWritableStorageConnection(GROUP_NAME);
    }


    /**
     * multipartFile 上传
     * @param groupName
     * @param multipartFile
     * @return
     */
    public static String upload(String groupName,MultipartFile multipartFile) {
        FastDFSFile fastDFSFile = multipartFile2FastDFSFile(multipartFile);
        String[] uploadResults = null;
        try {
            newWritableStorageConnection(groupName);
            uploadResults = storageClient.upload_file(groupName,fastDFSFile.getContent(),fastDFSFile.getExt(),setNameValuePair(multipartFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groupFilePath(uploadResults,false);
    }

    /**
     * File 上传
     * @param file
     * @param group
     * @return
     */
    public static String upload(String group,File file) {
        FastDFSFile fastDFSFile = file2FastDFSFile(file);
        String[] uploadResults = null;
        try {
            newWritableStorageConnection(group);
            uploadResults = storageClient.upload_file(group,fastDFSFile.getContent(),fastDFSFile.getExt(),setNameValuePair(fastDFSFile));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return groupFilePath(uploadResults,false);
    }


    /**
     * 删除文件
     * @param fileUrl
     */
    public static int deletefile(String groupName,String fileUrl){
        try {
            newWritableStorageConnection(groupName);
            FileInfo file_info1 = storageClient.get_file_info1(groupName + "/" + fileUrl);
            System.out.println(file_info1.toString());
            return storageClient.delete_file(groupName, fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String upload(MultipartFile multipartFile) {

        return upload( GROUP_NAME,multipartFile);
    }

    public static String upload(File file) {
        return upload( GROUP_NAME,file);
    }


    public static int deletefile(String fileUrl){
        return deletefile(GROUP_NAME,fileUrl);
    }

    /**
     * 文件下载
     * @param groupName
     * @param fileUrl
     * @return
     */
    public static byte[] download(String groupName,String fileUrl){
        byte[] group1s = null;
        try {
            newWritableStorageConnection(groupName);

            group1s = storageClient.download_file(groupName, fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return group1s;
    }

    public static byte[] download(String fileUrl){
        return  download(GROUP_NAME,fileUrl);
    }

    /**
     * 获取文件元数据
     * @param fileId 文件ID
     * @return
     */
    public Map<String,String> getFileMetadata(String groupName, String fileId) {
        try {
            newWritableStorageConnection(groupName);

            NameValuePair[] metaList = storageClient.get_metadata(groupName,fileId);
            if (metaList != null) {
                HashMap<String,String> map = new HashMap<String, String>();
                for (NameValuePair metaItem : metaList) {
                    map.put(metaItem.getName(),metaItem.getValue());
                }
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置元数据
     * @param multipartFile
     * @return
     */
    private static NameValuePair[] setNameValuePair(MultipartFile multipartFile) {
        String ext = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")+1);
        return setNameValuePair(multipartFile.getOriginalFilename(),String.valueOf(multipartFile.getSize()),ext);
    }
    /**
     * 设置元数据
     * @param fastDFSFile
     * @return
     */
    private static NameValuePair[] setNameValuePair(FastDFSFile fastDFSFile) {
        return  setNameValuePair(fastDFSFile.getName(),fastDFSFile.getLength(),fastDFSFile.getExt());
    }

    private static NameValuePair[] setNameValuePair(String name,String len,String ext) {
        NameValuePair[] meta_list = new NameValuePair[3];
        meta_list[0] = new NameValuePair("fileName",name);
        meta_list[1] = new NameValuePair("fileLength", len);
        meta_list[2] = new NameValuePair("fileExt", ext);
        return meta_list;
    }

    /**
     * multipartFile 转 File
     * @param multipartFile
     * @return
     */
    public static File multipartFile2File(MultipartFile multipartFile) {
       /* CommonsMultipartFile cf = (CommonsMultipartFile)multipartFile;
        DiskFileItem fi = (DiskFileItem) cf.getFileItem();
        File saveFile = fi.getStoreLocation();
        return saveFile;*/
        File toFile = null;

        try {
            if(multipartFile.equals("")||multipartFile.getSize()<=0){
                multipartFile = null;
            }else {
                InputStream ins = null;
                ins = multipartFile.getInputStream();
                toFile = new File(multipartFile.getOriginalFilename());
                inputStreamToFile(ins, toFile);
                ins.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return toFile;
    }

    public static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  multipartFile 转 FastDFSFile
     * @param multipartFile
     * @return
     */
    private static FastDFSFile multipartFile2FastDFSFile(MultipartFile multipartFile) {
        String ext = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")+1);
        FastDFSFile file = null;
        try {
            file = new FastDFSFile(multipartFile.getBytes(),ext);
            return file;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * file 转 FastDFSFile
     * @param file
     * @return
     */
    private static FastDFSFile file2FastDFSFile(File file) {
        String ext = imageSuffix(file.getName());
        FastDFSFile fastDFSFile = null;
        try {
            fastDFSFile = new FastDFSFile(file2byte(file),ext);
            return fastDFSFile;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 将 File 转为 byte
     * @param file
     * @return
     */
    private static byte[] file2byte(File file)
    {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
    /**
     * 获取文件名后缀
     * @param fileName
     * @return
     */
    private static String imageSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 获取文件名
     * @param fileName
     * @return
     */
    public static String getFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("/") + 1);
    }

    /**
     * 返回图片上传路径
     * @param uploadResults  返回的路径数组
     * @param group 是否带组名 true带组名 false不带组名
     * @return
     */
    private static String groupFilePath(String[] uploadResults,boolean group) {
        if(uploadResults != null && uploadResults.length > 0) {
            if(!group) {
                return IMG_SERVER + "/"+ uploadResults[0] + "/" + uploadResults[1];
            }
                return  IMG_SERVER + "/" + uploadResults[1];
        }
        return null;
    }

}