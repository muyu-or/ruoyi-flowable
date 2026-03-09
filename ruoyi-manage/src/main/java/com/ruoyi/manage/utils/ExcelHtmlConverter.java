package com.ruoyi.manage.utils;

import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Excel 转 HTML 工具类
 * 支持 xlsx/xls，可将嵌入图片以 Base64 形式内嵌到 HTML 中
 */
public class ExcelHtmlConverter {

    /** 单张图片超过此大小（字节）则跳过，防止 HTML 过大 */
    private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024L;

    /**
     * 将 Excel 文件转换为 HTML 字符串
     *
     * @param filePath 磁盘绝对路径
     * @return HTML 字符串
     * @throws IOException 读取失败
     */
    public static String convertToHtml(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 提取图片列表（按锚点行排序）
            List<PictureInfo> pictures = extractPictures(sheet);

            // 收集合并区域，用于 rowspan/colspan
            Map<String, int[]> mergeMap = buildMergeMap(sheet);

            // 计算最大列数（用于图片行的 colspan）
            int maxCols = calcMaxCols(sheet);

            return buildHtml(sheet, pictures, mergeMap, maxCols);
        }
    }

    // ------------------------------------------------------------------ //
    //  图片信息 POJO
    // ------------------------------------------------------------------ //

    private static class PictureInfo {
        final int anchorRow;   // 图片锚点行（row1）
        final int anchorCol;   // 图片锚点列（col1）
        final String imgTag;   // <img ...> 标签

        PictureInfo(int anchorRow, int anchorCol, String imgTag) {
            this.anchorRow = anchorRow;
            this.anchorCol = anchorCol;
            this.imgTag = imgTag;
        }
    }

    // ------------------------------------------------------------------ //
    //  图片提取
    // ------------------------------------------------------------------ //

    private static List<PictureInfo> extractPictures(Sheet sheet) {
        List<PictureInfo> list = new ArrayList<>();
        Drawing<?> drawing = sheet.getDrawingPatriarch();
        if (drawing == null) {
            return list;
        }

        if (drawing instanceof XSSFDrawing) {
            extractXssfPictures((XSSFDrawing) drawing, list);
        } else if (drawing instanceof HSSFPatriarch) {
            extractHssfPictures((HSSFPatriarch) drawing, list);
        }
        // 按锚点行排序，保证图片出现在对应行附近
        list.sort(Comparator.comparingInt(p -> p.anchorRow));
        return list;
    }

    private static void extractXssfPictures(XSSFDrawing drawing, List<PictureInfo> list) {
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture pic = (XSSFPicture) shape;
                org.apache.poi.xssf.usermodel.XSSFClientAnchor anchor =
                        (org.apache.poi.xssf.usermodel.XSSFClientAnchor) pic.getAnchor();
                String imgTag = toImgTag(pic.getPictureData());
                if (imgTag != null) {
                    list.add(new PictureInfo(anchor.getRow1(), anchor.getCol1(), imgTag));
                }
            }
        }
    }

    private static void extractHssfPictures(HSSFPatriarch patriarch, List<PictureInfo> list) {
        for (HSSFShape shape : patriarch.getChildren()) {
            if (shape instanceof HSSFPicture) {
                HSSFPicture pic = (HSSFPicture) shape;
                org.apache.poi.hssf.usermodel.HSSFClientAnchor anchor =
                        (org.apache.poi.hssf.usermodel.HSSFClientAnchor) pic.getAnchor();
                String imgTag = toImgTag(pic.getPictureData());
                if (imgTag != null) {
                    list.add(new PictureInfo(anchor.getRow1(), anchor.getCol1(), imgTag));
                }
            }
        }
    }

    private static String toImgTag(PictureData pd) {
        if (pd == null) {
            return null;
        }
        byte[] data = pd.getData();
        if (data == null || data.length == 0 || data.length > MAX_IMAGE_BYTES) {
            return null;
        }
        String mimeType = pd.getMimeType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "image/png";
        }
        String b64 = java.util.Base64.getEncoder().encodeToString(data);
        return "<img src=\"data:" + mimeType + ";base64," + b64
                + "\" style=\"max-width:600px;max-height:400px;\" />";
    }

    // ------------------------------------------------------------------ //
    //  合并单元格
    // ------------------------------------------------------------------ //

    private static Map<String, int[]> buildMergeMap(Sheet sheet) {
        Map<String, int[]> map = new HashMap<>();
        int numMerged = sheet.getNumMergedRegions();
        for (int i = 0; i < numMerged; i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            int rowspan = region.getLastRow() - region.getFirstRow() + 1;
            int colspan = region.getLastColumn() - region.getFirstColumn() + 1;
            String topLeft = region.getFirstRow() + "_" + region.getFirstColumn();
            map.put(topLeft, new int[]{rowspan, colspan});
            for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
                for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                    if (r == region.getFirstRow() && c == region.getFirstColumn()) {
                        continue;
                    }
                    map.put(r + "_" + c, new int[]{-1, -1});
                }
            }
        }
        return map;
    }

    // ------------------------------------------------------------------ //
    //  计算最大列数
    // ------------------------------------------------------------------ //

    private static int calcMaxCols(Sheet sheet) {
        int max = 1;
        for (int ri = sheet.getFirstRowNum(); ri <= sheet.getLastRowNum(); ri++) {
            Row row = sheet.getRow(ri);
            if (row != null && row.getLastCellNum() > max) {
                max = row.getLastCellNum();
            }
        }
        return max;
    }

    // ------------------------------------------------------------------ //
    //  HTML 构建
    // ------------------------------------------------------------------ //

    private static String buildHtml(Sheet sheet, List<PictureInfo> pictures,
                                    Map<String, int[]> mergeMap, int maxCols) {
        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();

        // 把图片按 anchorRow 分组，方便在对应行后面插入
        Map<Integer, List<PictureInfo>> picsByRow = new LinkedHashMap<>();
        for (PictureInfo p : pictures) {
            picsByRow.computeIfAbsent(p.anchorRow, k -> new ArrayList<>()).add(p);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table style=\"border-collapse:collapse;width:100%;\">");

        for (int ri = firstRow; ri <= lastRow; ri++) {
            Row row = sheet.getRow(ri);
            if (row == null) {
                // 空行：若有图片则插图片行，否则跳过
                appendPictureRows(sb, picsByRow.remove(ri), maxCols);
                continue;
            }

            sb.append("<tr>");
            short firstCol = row.getFirstCellNum();
            short lastCol = row.getLastCellNum();
            if (firstCol < 0) {
                sb.append("</tr>");
                appendPictureRows(sb, picsByRow.remove(ri), maxCols);
                continue;
            }

            for (int ci = firstCol; ci < lastCol; ci++) {
                String cellKey = ri + "_" + ci;

                if (mergeMap.containsKey(cellKey)) {
                    int[] span = mergeMap.get(cellKey);
                    if (span[0] == -1) {
                        continue;
                    }
                }

                Cell cell = row.getCell(ci, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                StringBuilder tdAttrs = new StringBuilder();
                tdAttrs.append(" style=\"border:1px solid #dfe6ec;padding:6px;text-align:center;vertical-align:middle;\"");

                if (mergeMap.containsKey(cellKey)) {
                    int[] span = mergeMap.get(cellKey);
                    if (span[0] > 1) tdAttrs.append(" rowspan=\"").append(span[0]).append("\"");
                    if (span[1] > 1) tdAttrs.append(" colspan=\"").append(span[1]).append("\"");
                }

                sb.append("<td").append(tdAttrs).append(">");

                if (cell != null) {
                    String text = getCellText(cell);
                    if (text != null && !text.isEmpty()) {
                        sb.append(escapeHtml(text));
                    }
                }

                sb.append("</td>");
            }
            sb.append("</tr>");

            // 在这一行之后插入属于该行锚点的图片行
            appendPictureRows(sb, picsByRow.remove(ri), maxCols);
        }

        // 剩余未插入的图片（锚点行超出数据行范围）追加到末尾
        for (List<PictureInfo> remaining : picsByRow.values()) {
            appendPictureRows(sb, remaining, maxCols);
        }

        sb.append("</table>");
        return sb.toString();
    }

    /**
     * 将图片列表输出为独立的 <tr> 行（每张图片一行，colspan=maxCols，居中）
     */
    private static void appendPictureRows(StringBuilder sb, List<PictureInfo> pics, int maxCols) {
        if (pics == null || pics.isEmpty()) {
            return;
        }
        for (PictureInfo p : pics) {
            sb.append("<tr><td colspan=\"").append(maxCols)
              .append("\" style=\"text-align:center;padding:8px;border:1px solid #dfe6ec;\">")
              .append(p.imgTag)
              .append("</td></tr>");
        }
    }

    // ------------------------------------------------------------------ //
    //  单元格文本提取
    // ------------------------------------------------------------------ //

    private static String getCellText(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception ex) {
                        return "";
                    }
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    // ------------------------------------------------------------------ //
    //  HTML 转义
    // ------------------------------------------------------------------ //

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
