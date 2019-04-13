package com.zmj.fdfsdemo.fdfsdemo;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by: meijun
 * Date: 2019/04/12 16:28
 */
@RestController
@RequestMapping("/fdfs")
public class TestController {

    @ResponseBody
    @PostMapping("/upload")
    public String upload(MultipartFile file) {
        String upload = FileManager.upload(file);
        return upload;
    }

    @ResponseBody
    @PostMapping("/delete")
    public int delete(String fileUrl) {
        //M00/00/00/rBFNjlywXBSAWZ5BAADqhhMDb5E163.png
        return FileManager.deletefile(fileUrl);
    }

    @ResponseBody
    @GetMapping("/dowond")
    public void dowond(HttpServletRequest request, HttpServletResponse responseString) {
        String fileUrl = request.getParameter("fileUrl");
        String fileName = FileManager.getFileName(fileUrl);
        byte[] bytes = FileManager.download(fileUrl);
        // 将数据写入输出流
        try {
            IOUtils.write(bytes, new FileOutputStream("E:\\" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("say")
    public String say() {
        return "hello , fdfs";
    }
}
