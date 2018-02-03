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
import com.sforce.soap.apex.CodeCoverageResult;
import com.sforce.soap.apex.RunTestFailure;
import com.sforce.soap.apex.RunTestSuccess;
import com.sforce.soap.apex.RunTestsResult;
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
import org.apache.tools.ant.types.LogLevel;

/**
 * Test coverage report.
 * @author ss
 */
public class CoverageReport {
    /** CSS file name. */
    private static final String CSS_FILE_NAME = "coverage-report.css";
    /** Tests result. */
    private final RunTestsResult result;
    /** Project src directory. */
    private final File srcDir;
    /** Ant task. */
    private final TestTask task;
    /**
     * Constructor.
     * @param result result.
     * @param srcDir project source directory.
     * @param task ant task.
     */
    public CoverageReport(final RunTestsResult result, final File srcDir,
            final TestTask task) {
        this.result = result;
        this.srcDir = srcDir;
        this.task = task;
    }
    public void createReport() throws BuildException {
        try {
            StringBuilder sb = new StringBuilder();
            Set<String> classes = task.getProjectClassesAndTriggers();
            sb.append("<html>").append("<head>");
            sb.append(createStyle());
            sb.append("</head>").append("<body>");
            sb.append(createClassesCoverageTable(classes));
            sb.append(createTestClassesTable());
            sb.append("</body>").append("</html>");
            File reportFile = new File("coverage-report.html");
            try (FileOutputStream fos = new FileOutputStream(
                    reportFile)) {
                fos.write(sb.toString().getBytes("UTF-8"));
            }
            copyResources(new String[] {CSS_FILE_NAME, "google-font1.css",
                "google-font2.css"});
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
    private String createClassesCoverageTable(final Set<String> classes) {
        StringBuilder sb = new StringBuilder();
        CodeCoverageResult[] coverageResult = result.getCodeCoverage();
        List<CoverageElement> elements = new ArrayList<>();
        for (CodeCoverageResult ccr : coverageResult) {
            task.log("coverage name [" + ccr.getName() + "]",
                    LogLevel.DEBUG.getLevel());
            if (!classes.contains(ccr.getName())) {
                continue;
            }
            CoverageElement element = new CoverageElement(ccr);
            elements.add(element);
        }
        Collections.sort(elements, (CoverageElement o1, CoverageElement o2) -> {
            return o1.getClassName().compareTo(o2.getClassName());
        });
        int totalLines = 0;
        int totalCoveregeLines = 0;
        StringBuilder table = new StringBuilder();
        table.append("<table>");
            table.append("<thead>");
                table.append("<th>").append("Class name").append("</th>");
                table.append("<th>").append("Coverage lines").append("</th>");
                table.append("<th>").append("Coverage percent").append("</th>");
            table.append("</thead>");
            table.append("</tbody>");
            for (CoverageElement el : elements) {
                table.append(el.toHTMLRow());
                totalLines += el.getTotalLines();
                totalCoveregeLines += el.getCoverageLines();
            }
            table.append("</tbody>");
        table.append("</table>");
        // total table
        float percent = (((float) totalCoveregeLines)
                    / ((float) totalLines)) * 100;
        StringBuilder totalTable = new StringBuilder();
        totalTable.append("<table class=\"total-table\">");
            totalTable.append("</tbody>");
            totalTable.append("<tr>");
                totalTable.append("<td>").append("<b>Total</b> (fail: ")
                        .append(result.getFailures().length)
                        .append(", success: ")
                        .append(result.getSuccesses().length)
                        .append(")").append("</td>");
                totalTable.append("<td class=\"total-col\">")
                        .append(totalCoveregeLines).append("/")
                        .append(totalLines).append("</td>");
                totalTable.append("<td class=\"total-col\">")
                        .append(String.format("%.1f", percent))
                        .append("</td>");
            totalTable.append("</tr>");
            totalTable.append("</tbody>");
        totalTable.append("</table>");
        sb.append(totalTable);
        sb.append(table);
        return sb.toString();
    }
    private String createTestClassesTable() {
        RunTestFailure[] failTests = result.getFailures();
        Map<String, List<TestElement>> testMap = new HashMap();
        for (RunTestFailure fail : failTests) {
            TestElement el = new TestElement(fail);
            if (!testMap.containsKey(fail.getName())) {
                testMap.put(fail.getName(), new ArrayList<>());
            }
            testMap.get(fail.getName()).add(el);
        }
        RunTestSuccess[] successTests = result.getSuccesses();
        for (RunTestSuccess success : successTests) {
            TestElement el = new TestElement(success);
            if (!testMap.containsKey(success.getName())) {
                testMap.put(success.getName(), new ArrayList<>());
            }
            testMap.get(success.getName()).add(el);
        }
        StringBuilder sb = new StringBuilder();
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
    private void copyResources(final String[] resources) throws Exception {
        for (String resource : resources) {
            try (InputStream is = getClass()
                    .getResourceAsStream(resource)) {
                byte[] buffer = new byte[is.available()];
                is.read(buffer, 0, is.available());
                File reportRes = new File(resource);
                try (FileOutputStream fos = new FileOutputStream(reportRes)) {
                    fos.write(buffer);
                }
            }
        }
    }
}
