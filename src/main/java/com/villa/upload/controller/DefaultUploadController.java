package com.villa.upload.controller;

import com.villa.dto.ErrCodeDTO;
import com.villa.dto.ResultDTO;
import com.villa.upload.local.util.LocalUploadUtil;
import com.villa.util.FileUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@Controller
public class DefaultUploadController {
	@ResponseBody
	@RequestMapping("/public/upload/on")
	public ResultDTO on(@RequestParam("file") MultipartFile file){
		try {
			String filename = FileUtil.getFileMD5(file.getBytes())+"."+FileUtil.getEndFix(file.getOriginalFilename());
			return ResultDTO.putSuccess(LocalUploadUtil.upload(file, filename));
		} catch (Exception e) {
			e.printStackTrace();
			return ResultDTO.put500(ErrCodeDTO.ox99998);
		}
	}
}