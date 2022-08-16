package com.changgou.controller;


import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSUtil;

import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/upload")
@CrossOrigin
public class FileUploadController {

    /***
     * 文件上传
     */
    @PostMapping
    public Result upload(@RequestParam(value = "file")MultipartFile file) throws Exception{
        //封装文件信息
        FastDFSFile fastDFSFile =new FastDFSFile(
                file.getOriginalFilename(),
                file.getBytes(),
                StringUtils.getFilenameExtension(file.getOriginalFilename())//获取文件拓展名
        );

        //条约FastDFSUtil工具类将文件传入到FastDFS中
        FastDFSUtil.upload(fastDFSFile);
        return new Result(true, StatusCode.OK,"上传成功");
    }
}
