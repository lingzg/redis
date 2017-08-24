package com.cecsys.secmax.redis.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;

@Controller
@RequestMapping("/file")
public class CommonFileController{
	
	@RequestMapping(value="/index")
	public String index(HttpServletRequest request,HttpServletResponse response) {
		return "uploadify";
	}
	
	
	@RequestMapping(value="/uploadFile")
	public void uploadFile(HttpServletRequest request,HttpServletResponse response) throws Exception{
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		result.put("success", true);
        MultipartHttpServletRequest mRequest=(MultipartHttpServletRequest)request;
        Iterator<String> fns=mRequest.getFileNames();//获取上传的文件列表
       
        String path="D:\\gongdi\\redis";
		long maxSize=51200000L;
        File dir = new File(path);
        if(!dir.exists()){
        	dir.mkdirs();
        }
        while(fns.hasNext()){
        	Map<String, Object> map = new HashMap<String, Object>();
            MultipartFile mFile = mRequest.getFile(fns.next());
			//判断文件是否为空
			if(null==mFile||mFile.isEmpty()){
				result.put("success", false);
			    result.put("message", "文件为空");
				break;
			}
			if(mFile.getSize()>maxSize&& maxSize>0){
				result.put("success", false);
				result.put("message","此文件过大,无法上传");
				data.add(result);
				break;
			}
			String fileName = mFile.getOriginalFilename();
			String fileType = fileName.substring(fileName.lastIndexOf(".")+1);
			String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + fileType;
			String filePath = path + newFileName;
			File file = new File(filePath);
			mFile.transferTo(file);
			map.put("fileName", fileName);
			map.put("fileType", fileType);
			map.put("filePath", filePath);
			data.add(map);
        }
        result.put("data", data);
        response.setContentType("text/html;charset=utf-8");
	    response.getWriter().write(JSON.toJSONString(result));
	}

	@RequestMapping(value = "downloadFile")
	public ResponseEntity<byte[]> downloadFile(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String fileName = request.getParameter("fileName");
		String filePath = request.getParameter("filePath");
		File file = new File(filePath);
		if(file.exists()){
			HttpHeaders headers = new HttpHeaders();    
	        fileName=new String(fileName.getBytes("UTF-8"),"iso-8859-1");//为了解决中文名称乱码问题  
	        headers.setContentDispositionFormData("attachment", fileName);
	        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	        ResponseEntity<byte[]> result=new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers, HttpStatus.CREATED); 
	        return result;
		}else{
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write("文件不存在");
			return null;
		}
	}

}
