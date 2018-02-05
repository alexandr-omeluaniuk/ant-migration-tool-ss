/*
 * The MIT License
 *
 * Copyright 2018 ss.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.salesforce.report;

import com.salesforce.ant.TestTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;

/**
 * Test coverage report.
 * @author ss
 */
public class HTMLCoverageReport {
    /** CSS file name. */
    private static final String CSS_FILE_NAME = "coverage-report.css";
    /** HTML file name. */
    private static final String HTML_FILE_NAME = "coverage-report.html";
    /** XML report. */
    private final XMLCoverageReport xmlReport;
    /** Ant task. */
    private final TestTask task;
    /**
     * Constructor.
     * @param xmlReport
     * @param task Ant task.
     */
    public HTMLCoverageReport(final XMLCoverageReport xmlReport,
            final TestTask task) {
        this.xmlReport = xmlReport;
        this.task = task;
    }
    public void createReport() throws BuildException {
        try {
            StringBuilder sb = new StringBuilder();
            
            sb.append("<html>").append("<head>");
            sb.append(createStyle());
            sb.append("</head>").append("<body>");
            sb.append(createClassesCoverageTable());
            sb.append(createTestClassesTable());
            sb.append("</body>").append("</html>");
            File folder = new File(TestTask.REPORT_FOLDER_NAME);
            if (!folder.exists()) {
                folder.mkdir();
            }
            File reportFile = new File(folder, HTML_FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(
                    reportFile)) {
                fos.write(sb.toString().getBytes("UTF-8"));
            }
            copyResources(new String[] {CSS_FILE_NAME}, folder);
            task.log("report saved to [" + reportFile.getAbsolutePath() + "]");
        } catch (Exception e) {
            throw new BuildException("create coverage report fail!", e);
        }
    }
    private StringBuilder createStyle() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""
                + CSS_FILE_NAME + "\">");
        return sb;
    }
    private String createClassesCoverageTable() {
        StringBuilder sb = new StringBuilder();
        StringBuilder table = new StringBuilder();
        table.append("<table>");
            table.append("<thead>");
                table.append("<th>").append("Class name").append("</th>");
                table.append("<th>").append("Coverage lines").append("</th>");
                table.append("<th>").append("Coverage percent").append("</th>");
            table.append("</thead>");
            table.append("</tbody>");
            for (CoverageElement el : xmlReport.getClasses()) {
                table.append(el.toHTMLRow(task.getCoveragePercentLimit()));
            }
            table.append("</tbody>");
        table.append("</table>");
        // total table
        StringBuilder totalTable = new StringBuilder();
        totalTable.append("<table class=\"total-table\">");
            totalTable.append("</tbody>");
            totalTable.append("<tr>");
                totalTable.append("<td>").append("<b>Total</b> (fail: ")
                        .append(xmlReport.getFail())
                        .append(", success: ")
                        .append(xmlReport.getSuccess())
                        .append(")").append("</td>");
                totalTable.append("<td class=\"total-col\">")
                        .append(xmlReport.getTotalCoverageLines()).append("/")
                        .append(xmlReport.getTotalLines()).append("</td>");
                totalTable.append("<td class=\"total-col\">")
                        .append(String.format(
                                "%.1f", xmlReport.getTotalPercent()))
                        .append("</td>");
            totalTable.append("</tr>");
            totalTable.append("</tbody>");
        totalTable.append("</table>");
        sb.append(totalTable);
        sb.append(table);
        return sb.toString();
    }
    private String createTestClassesTable() {
        StringBuilder sb = new StringBuilder();
        Map<String, List<TestElement>> testMap = new HashMap();
        xmlReport.getMethods().stream().forEach((te) -> {
            if (!testMap.containsKey(te.getClassName())) {
                testMap.put(te.getClassName(), new ArrayList<>());
            }
            testMap.get(te.getClassName()).add(te);
        });
        Set<String> tests = testMap.keySet();
        Set<String> sortedTests = new TreeSet<>(tests);
        sb.append("<table class=\"tests-table\">");
            sb.append("<thead>");
                sb.append("<th>").append("Method name").append("</th>");
                sb.append("<th>").append("Error").append("</th>");
                sb.append("<th>").append("State").append("</th>");
                sb.append("<th>").append("See all data").append("</th>");
                sb.append("<th>").append("Duration").append("</th>");
            sb.append("</thead>");
            sb.append("</tbody>");
            for (String clazz : sortedTests) {
                double fullDuration = 0;
                boolean isFail = false;
                List<TestElement> elements = testMap.get(clazz);
                Collections.sort(elements);
                StringBuilder rows = new StringBuilder();
                for (TestElement el : elements) {
                    rows.append(el.toHTMLRow());
                    fullDuration += el.getDuration();
                    if (el.isIsFail()) {
                        isFail = true;
                    }
                }
                sb.append("<tr>");
                sb.append("<td colspan=\"5\" class=\"col-class-name\"><b class=\"")
                        .append(isFail ? "error" : "success").append("\">")
                        .append(clazz).append("</b> (duration: ")
                        .append(fullDuration).append(")").append("</td>");
                sb.append("</tr>");
                sb.append(rows);
            }
            sb.append("</tbody>");
        sb.append("</table>");
        return sb.toString();
    }
    private void copyResources(final String[] resources, final File folder)
            throws Exception {
        for (String resource : resources) {
            try (InputStream is = getClass()
                    .getResourceAsStream(resource)) {
                byte[] buffer = new byte[is.available()];
                is.read(buffer, 0, is.available());
                File reportRes = new File(folder, resource);
                try (FileOutputStream fos = new FileOutputStream(reportRes)) {
                    fos.write(buffer);
                }
            }
        }
    }
}
