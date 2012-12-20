<%@ page import="java.io.*, java.text.*, java.lang.*, java.util.*,java.net.*"%><%
ServletContext context = pageContext.getServletContext();
String filePath = context.getRealPath(request.getContextPath())+ File.separatorChar;

request.setCharacterEncoding("euc-kr");

String fileName = request.getParameter("fileName");

if (fileName == null) {
 return;
}
String mime = getServletContext().getMimeType(fileName);

if (mime == null) {
 mime = "application/octet-stream;";
}

// 자신에게 맞게 수정할 것.
File file = new File(filePath, fileName);

byte b[] = new byte[2048];

//response.setContentType(mime + "; charset=MS949");
response.setHeader("Content-Transfer-Encoding", "7bit");

if (request.getHeader("User-Agent").indexOf("MSIE 5.5") > -1) {
 response.setHeader("Content-Disposition",
   "filename=" +java.net.URLEncoder.encode(fileName, "euc-kr") + ";");
} else {
 response.setHeader("Content-Disposition",
   "attachment; filename=" + fileName + ";");
}

response.setHeader("Content-Length", "" + file.length() );

if (file.isFile())
{
	try {
		BufferedInputStream fin = new BufferedInputStream(new
		FileInputStream(file));
		BufferedOutputStream outs = new
		BufferedOutputStream(response.getOutputStream());
		int read = 0;
		
		while ((read = fin.read(b)) != -1){
			outs.write(b,0,read);
		}

		outs.close();
		fin.close();
		
		boolean isFileDeleted = file.delete();
		System.out.println("isFileDeleted=" + isFileDeleted);
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	finally {
		System.out.println("download complete / file.getAbsolutePath()=" + file.getAbsolutePath());
	}
}
%>