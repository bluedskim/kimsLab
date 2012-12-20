package dskim.pdf;
import java.awt.Rectangle;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.PDFTextStripperByArea;

public class PdfBookmarkGenerator {
	String filePath;
	String author;
	
	public PdfBookmarkGenerator(String filePath) {
		super();
		this.filePath = filePath;
	}

	public String generateBookmark() throws Exception {
		String saveFileName = null;

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
		Calendar cal = Calendar.getInstance();
		String currSec = dateFormat.format(cal.getTime());
		
		PDDocument document = null;
		PDDocument songBook = null;
		ArrayList<String> titles = new ArrayList<String>();					// 타이틀 목록 저장용 list
		HashMap<String, PDPage> pages = new HashMap<String, PDPage>();		// 페이지 저장용
		try {
			File sourcePdfFile = new File(filePath);
			System.out.println("파일 읽는 중... " + sourcePdfFile.getCanonicalPath());

			document = PDDocument.load(sourcePdfFile);
			if (document.isEncrypted()) {
				try {
					document.decrypt("");
				} catch (InvalidPasswordException e) {
					System.err.println("Error: Document is encrypted with a password.");
					System.exit(1);
				}
			}
			PDFTextStripperByArea stripper = new PDFTextStripperByArea();
			stripper.setSortByPosition(true);
			Rectangle rect = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);	// 페이지 전체의 텍스트를 가져옴
			stripper.addRegion("wholePage", rect);
			List allPages = document.getDocumentCatalog().getAllPages();
			//System.out.println("Text in the area:" + rect);
			System.out.println("페이지 수 =" + allPages.size());
			for( int i=0; i<allPages.size(); i++ )
			{
				PDPage tempPage = (PDPage) allPages.get(i);
				stripper.extractRegions(tempPage);
				System.out.print("[" + i + "] Page의 제목 : ");
				String tempText = stripper.getTextForRegion("wholePage");
				String[] tempLines = tempText.split("[\\r\\n]+");
				for(int j = 0 ; j < tempLines.length ; j++) {
					tempLines[j] = tempLines[j].trim();
					if(!tempLines[j].equals("")) {
						System.out.println("\t" + tempLines[j]);
//						titles.add(tempLines[j]+"_"+i);				// 제목이 중복될 경우 구분하기 위해 페이지번호를 뒤에 붙힘
//						pages.put(tempLines[j]+"_"+i, tempPage);
						titles.add(tempLines[j]);
						pages.put(tempLines[j], tempPage);							
						break;
					}	
				}
			}
			
			if(titles.size() > 0) {
				Collections.sort(titles);
			}

			System.out.println("=============== 정렬결과 ==================");
			
			if(titles.size() > 0) {
		        songBook = new PDDocument();
		        
				for(int k = 0 ; k < titles.size(); k++) {
					System.out.println("No=" + (k+1) +  ":\t" + titles.get(k));
					songBook.addPage(pages.get(titles.get(k)));
				}	

				System.out.println("=============== 북마크 생성 ==================");
				//first create the document outline and add it to the page
		        PDDocumentOutline outline = new PDDocumentOutline();
		        songBook.getDocumentCatalog().setDocumentOutline( outline );
		        //Create a root element to show in the tree
		        //PDOutlineItem root = new PDOutlineItem();
		        //root.setTitle("Root of Document");
		        //outline.appendChild(root);

		        List<PDPage> tempPages = (List<PDPage>)songBook.getDocumentCatalog().getAllPages();
		        for( int i=0; i<tempPages.size(); i++ )
                {
                    PDPage page = (PDPage)tempPages.get(i);
                    //Create the outline item to refer to the first page.
                    PDOutlineItem pageItem = new PDOutlineItem();
                    pageItem.setTitle(titles.get(i));
                    pageItem.setDestination(page);
                    //root.appendChild(pageItem);	                    
                    outline.appendChild(pageItem);
				}
		        
				saveFileName = sourcePdfFile.getName().substring(0, sourcePdfFile.getName().indexOf(".")) + currSec + ".pdf";
				
				System.out.println("=============== PDDocumentInformation 생성 ==================");
				PDDocumentInformation info = new PDDocumentInformation();
				info.setTitle(sourcePdfFile.getName());
				info.setCreationDate(Calendar.getInstance());
				if(author != null) info.setAuthor(author);				
				songBook.setDocumentInformation(info);
				
	        	songBook.save(sourcePdfFile.getParentFile().getAbsolutePath() + File.separatorChar + saveFileName);
	        	songBook.close();
	        	System.out.println(saveFileName + " 저장 완료");
			}		        
		} finally {
			if (document != null) {
				document.close();
			}
			if (songBook != null) {
				songBook.close();
			}				
		}
		
		return saveFileName;
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			usage();
		} else {
			PdfBookmarkGenerator bookmarkGenerator = new PdfBookmarkGenerator(args[0]);
			if(args.length > 1) bookmarkGenerator.author = args[1];
			
			String saveFileName = bookmarkGenerator.generateBookmark();
			System.out.println("saveFileName=" + saveFileName);
		}
	}

	/**
	 * This will print the usage for this document.
	 */
	private static void usage() {
		System.err.println("Usage: java PdfBookmarkGenerator <input-pdf>");
	}
}