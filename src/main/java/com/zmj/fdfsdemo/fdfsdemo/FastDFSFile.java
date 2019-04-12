package com.zmj.fdfsdemo.fdfsdemo;

/**
 * @Author: 周美军
 * @Date: 2019/2/28 15:17
 * @Email: 536304123@QQ.COM
 */
public class FastDFSFile {
    /**
     * 文件内容
     */
    private byte[] content;
    /**
     * 文件名
     */
    private String name;
    /**
     * 后缀名
     */
    private String ext;
    /**
     * 长度
     */
    private String length;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public FastDFSFile() {
    }

    public FastDFSFile(byte[] content, String ext) {
        this.content = content;
        this.ext = ext;
    }

    public FastDFSFile(byte[] content, String name, String ext) {
        this.content = content;
        this.name = name;
        this.ext = ext;
    }

    public FastDFSFile(byte[] content, String name, String ext, String length) {
        this.content = content;
        this.name = name;
        this.ext = ext;
        this.length = length;
    }
}
