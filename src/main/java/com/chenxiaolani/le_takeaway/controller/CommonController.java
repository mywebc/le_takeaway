package com.chenxiaolani.le_takeaway.controller;

import com.chenxiaolani.le_takeaway.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${le_takeaway.path}")
    private String basePath;

    /**
     * 上传文件, 注意file是前端传过来的文件，所以这里的参数名要和前端的参数名一致，就是前端的name属性
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // file就是前端传过来的文件,这是临时文件，需要自己保存（转存）
        log.info("上传文件：{}", file.toString());

        // 获取文件名(并不建议这样做，因为文件名有可能重复，我们可以自己生成一个文件名)
        // 使用UUID生成文件名
        String originalFilename = UUID.randomUUID().toString() + file.getOriginalFilename();

        // 创建一个目录
        File dir = new File(basePath);
        if (!dir.exists()) {
            // 如果目录不存在，创建目录
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basePath + originalFilename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(originalFilename);
    }

    /**
     * 下载文件
     *
     * @param name
     * @param httpServletResponse
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse httpServletResponse) {
        // 输入流，读取文件
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            // 输出流，写出文件
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();

            // 设置返回格式
            httpServletResponse.setContentType("image/jpeg");

            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fileInputStream.read(buffer)) != -1) {
                servletOutputStream.write(buffer, 0, len);
            }
            fileInputStream.close();
            servletOutputStream.close();
            servletOutputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
