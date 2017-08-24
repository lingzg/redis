<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"></c:set>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>uploadify文件上传</title>
		<link rel="stylesheet" href="${contextPath}/static/uploadify.css" />
		<script type="text/javascript" src="${contextPath}/static/jquery-1.11.0.js" ></script>
		<script type="text/javascript" src="${contextPath}/static/jquery.uploadify.js" ></script>
	</head>
	<body>
		<input type="file" name="file_upload" id="file_upload" />
		<script type="text/javascript">
	    $(function() {
	    	var contextPath ="/"+window.location.pathname.split("/")[1];
	        $('#file_upload').uploadify({
	            'swf'      : contextPath+'/static/uploadify.swf',
	            'uploader' : contextPath+'/file/uploadFile',
	            'onUploadSuccess': function (file, data, response) {
	            	console.log(file);
	            	console.log(data);
	            }
	        });
	    });
    </script>
	</body>
</html>
