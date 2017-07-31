package com.bsoft.servlet;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class ExcelExportServlet
 */
public class ExcelExportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ExcelExportServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/x-msdownload");
        String excelName = "Excel.xls";  
        //转码防止乱码  
        response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(excelName,"UTF-8")); 
		List<JSONObject> contentjson = new ArrayList<JSONObject>();
		request.setCharacterEncoding("UTF-8");//中文乱码
		String title = request.getParameter("columns");
		String content = request.getParameter("content");
		String titlestr = title.substring(1, title.length()-1);
		String contentstr = content.substring(1, content.length()-1);
		String[] titlearray = titlestr.split(",");
		String[] contentarray = contentstr.split("},");
		for(int i=0; i<contentarray.length; i++){
			if(contentarray.length-1!=i){
				contentarray[i]=contentarray[i]+"}";
			}
			JSONObject json = JSONObject.fromObject(contentarray[i]);
			contentjson.add(json);
		}
        try {  
        	OutputStream out = response.getOutputStream();
            exportExcel(out,titlearray,contentjson);  
            System.out.println("excel导出成功！");  
        } catch (FileNotFoundException e) {  
                e.printStackTrace();  
        } catch (IOException e) {  
                e.printStackTrace();  
        }  
	}
	
	public void exportExcel(OutputStream out,String[] title, List<JSONObject> content) throws IOException{
		//创建webbook，对应Excel文件
		HSSFWorkbook wb = new HSSFWorkbook();
		//webbook中添加一个sheet，对应Excel文件中的sheet
		HSSFSheet sheet = wb.createSheet("Table");
		
		//设置单元格表头样式
		HSSFCellStyle headstyle = wb.createCellStyle();
		headstyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);  
		headstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
		headstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
		headstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
		headstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);  
		headstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);  
		headstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		//表头字体
		HSSFFont headfont = wb.createFont();
		headfont.setColor(HSSFColor.VIOLET.index);  
		headfont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); 
		headstyle.setFont(headfont);
		
		//设置内容样式
        HSSFCellStyle contentstyle = wb.createCellStyle();  
        contentstyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);  
        contentstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
        contentstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
        contentstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
        contentstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);  
        contentstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        contentstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        contentstyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER); 
        //设置内容字体样式
        HSSFFont contentfont = wb.createFont();  
        contentfont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        contentstyle.setFont(contentfont);
        
        //产生标题行
        HSSFRow row = sheet.createRow(0);  
        for(int i = 0; i<title.length-1;i++){  
            HSSFCell cell = row.createCell(i);  
            cell.setCellStyle(headstyle);  
            HSSFRichTextString text = new HSSFRichTextString(title[i+1]);  
            cell.setCellValue(text);  
        } 
        
        //产生内容行
        for (int i=0;i<content.size();i++) {  
        	JSONObject json = content.get(i);  
            row = sheet.createRow(i+1);
            for(int j = 0; j<title.length-1;j++){  
                HSSFCell cell = row.createCell(j);  
                cell.setCellStyle(contentstyle);
                String key = title[j+1];
                String keystr = key.substring(1, key.length()-1);
                String value = json.getString(keystr);
                cell.setCellValue(value);  
            } 
        }  
        
        //FileOutputStream out = new FileOutputStream("D:/Table.xls");  	
		wb.write(out);
		out.close();
	}
}
