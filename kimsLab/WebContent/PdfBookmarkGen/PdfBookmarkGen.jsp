<%@ page language="java" contentType="text/html; charset=utf8"
    pageEncoding="utf8"%>
<%@ page import="dskim.pdf.*" %>
<%@ page import="java.io.*,java.util.*,javax.servlet.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.commons.fileupload.disk.*" %>
<%@ page import="org.apache.commons.fileupload.servlet.*" %>
<%@ page import="org.apache.commons.io.output.*" %>
<%
	ServletContext context = pageContext.getServletContext();
	String filePath = context.getRealPath(request.getContextPath())+ File.separatorChar;
	File file;
	int maxFileSize = 5000 * 1024;
	int maxMemSize = maxFileSize;
	String contentType = request.getContentType();

	if (contentType != null
			&& (contentType.indexOf("multipart/form-data") >= 0)) {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(maxMemSize);
		factory.setRepository(new File("/tmp"));
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(maxFileSize);
		try {
			List fileItems = upload.parseRequest(request);
			Iterator i = fileItems.iterator();
			while (i.hasNext()) {
				FileItem fi = (FileItem) i.next();
				if (!fi.isFormField()) {
					String fieldName = fi.getFieldName();
					String fileName = fi.getName();
					if (fileName == "")
						out.println("파일을 다시 올려주세요. <a href=''>다시 시도</a>");
					boolean isInMemory = fi.isInMemory();
					long sizeInBytes = fi.getSize();
					if (fileName.lastIndexOf("\\") >= 0) {
						file = new File(filePath
								+ fileName.substring(fileName
										.lastIndexOf("\\")));
					} else {
						file = new File(filePath
								+ fileName.substring(fileName
										.lastIndexOf("\\") + 1));
					}
					fi.write(file);
					out.println("업로드된 파일명: " + filePath
							+ fileName + ", 크기(byte) = "
							+ sizeInBytes + "<br/>");
					
					PdfBookmarkGenerator bookmarkGenerator = new PdfBookmarkGenerator(filePath + File.separatorChar + fileName);					
					String saveFileName = bookmarkGenerator.generateBookmark();
					out.println("saveFileName=" + saveFileName);
					file.delete();

					out.println("<a href=''>다시 업로드</a><br/>");
					out.println("<a href='./download.jsp?fileName=" + saveFileName + "'>변환파일 다운로드</a><br/>");
				}
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
	} else {
%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf8">
<title>PdfBookmarkGen</title>
</head>
<body>
	<h3>변환할 pdf 파일업로드</h3>
	<p>
		<%
			out.println("임시업로드 경로: " + filePath);
		%>
	</p>
	<form enctype="multipart/form-data" action="" method="post">
		<table>
			<tr>
				<td align="left">파일선택:</td>
			</tr>
			<tr>
				<td align="left"><input type="file" name="filename" size="50"></td>
			</tr>
			<tr>
				<td align="left"><input type="submit" name="Submit" value="Upload"> <input type="reset" name="Reset" value="Cancel"></td>
			</tr>
		</table>
	</form>
</body>
</html>
<%
}
%>