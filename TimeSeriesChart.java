import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.StandardChartTheme;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class TimeSeriesChart extends ApplicationFrame {

    private static final long serialVersionUID = 1L;

    static {
        // Установка темы графика
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
    }

    public TimeSeriesChart(String title) {
        super(title);
        ChartPanel chartPanel = (ChartPanel) createDemoPanel();
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 500));
        setContentPane(chartPanel);
    }

    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Сравнение ArrayList и LinkedList",  // Заголовок графика
                "Количество элементов",             // Метка оси X
                "Время (нс)",                       // Метка оси Y
                dataset,                            // Данные
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true,                               // Легенда
                true,                               // Подсказки
                false                               // URL
        );

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        // Настройка отображения данных
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        plot.setRenderer(renderer);

        return chart;
    }

    private XYDataset createDataset() {
        // Путь к папке с логами
        String folderPath = "C:/Users/Семен/Documents/Lab 5/src/logs";
        Map<String, Map<String, Long>> data = processLogs(folderPath);

        // Создаем коллекции серий для ArrayList и LinkedList
        XYSeries avgAddSeriesArrayList = new XYSeries("ArrayList: Среднее время добавления");
        XYSeries avgRemoveSeriesArrayList = new XYSeries("ArrayList: Среднее время удаления");
        XYSeries totalAddSeriesArrayList = new XYSeries("ArrayList: Общее время добавления");
        XYSeries totalRemoveSeriesArrayList = new XYSeries("ArrayList: Общее время удаления");

        XYSeries avgAddSeriesLinkedList = new XYSeries("LinkedList: Среднее время добавления");
        XYSeries avgRemoveSeriesLinkedList = new XYSeries("LinkedList: Среднее время удаления");
        XYSeries totalAddSeriesLinkedList = new XYSeries("LinkedList: Общее время добавления");
        XYSeries totalRemoveSeriesLinkedList = new XYSeries("LinkedList: Общее время удаления");

        // Преобразуем данные в отсортированный вид
        TreeMap<Integer, Map<String, Long>> arrayListData = new TreeMap<>();
        TreeMap<Integer, Map<String, Long>> linkedListData = new TreeMap<>();

        for (String fileName : data.keySet()) {
            int elementCount = Integer.parseInt(fileName.replaceAll("\\D", ""));
            if (fileName.toLowerCase().contains("arraylist")) {
                arrayListData.put(elementCount, data.get(fileName));
            } else if (fileName.toLowerCase().contains("linkedlist")) {
                linkedListData.put(elementCount, data.get(fileName));
            }
        }

        // Заполняем серии для ArrayList
        for (Map.Entry<Integer, Map<String, Long>> entry : arrayListData.entrySet()) {
            int elementCount = entry.getKey();
            Map<String, Long> stats = entry.getValue();

            long addTotalTime = stats.getOrDefault("addTotalTime", 0L);
            long addTotalCount = stats.getOrDefault("addTotalCount", 1L);
            long removeTotalTime = stats.getOrDefault("removeTotalTime", 0L);
            long removeTotalCount = stats.getOrDefault("removeTotalCount", 1L);

            double avgAddTime = (double) addTotalTime / addTotalCount;
            double avgRemoveTime = (double) removeTotalTime / removeTotalCount;

            avgAddSeriesArrayList.add(elementCount, avgAddTime);
            avgRemoveSeriesArrayList.add(elementCount, avgRemoveTime);
            totalAddSeriesArrayList.add(elementCount, addTotalTime);
            totalRemoveSeriesArrayList.add(elementCount, removeTotalTime);
        }

        // Заполняем серии для LinkedList
        for (Map.Entry<Integer, Map<String, Long>> entry : linkedListData.entrySet()) {
            int elementCount = entry.getKey();
            Map<String, Long> stats = entry.getValue();

            long addTotalTime = stats.getOrDefault("addTotalTime", 0L);
            long addTotalCount = stats.getOrDefault("addTotalCount", 1L);
            long removeTotalTime = stats.getOrDefault("removeTotalTime", 0L);
            long removeTotalCount = stats.getOrDefault("removeTotalCount", 1L);

            double avgAddTime = (double) addTotalTime / addTotalCount;
            double avgRemoveTime = (double) removeTotalTime / removeTotalCount;

            avgAddSeriesLinkedList.add(elementCount, avgAddTime);
            avgRemoveSeriesLinkedList.add(elementCount, avgRemoveTime);
            totalAddSeriesLinkedList.add(elementCount, addTotalTime);
            totalRemoveSeriesLinkedList.add(elementCount, removeTotalTime);
        }

        // Создаем коллекцию серий
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(avgAddSeriesArrayList);
        dataset.addSeries(avgRemoveSeriesArrayList);
        dataset.addSeries(totalAddSeriesArrayList);
        dataset.addSeries(totalRemoveSeriesArrayList);
        dataset.addSeries(avgAddSeriesLinkedList);
        dataset.addSeries(avgRemoveSeriesLinkedList);
        dataset.addSeries(totalAddSeriesLinkedList);
        dataset.addSeries(totalRemoveSeriesLinkedList);

        return dataset;
    }



    private Map<String, Map<String, Long>> processLogs(String folderPath) {
        Map<String, Map<String, Long>> results = new TreeMap<>(); // Используем TreeMap для сортировки

        try {
            Path folder = Paths.get(folderPath);

            // Проверка существования папки
            if (!Files.exists(folder)) {
                throw new IOException("Папка не найдена: " + folderPath);
            }

            // Регулярные выражения для извлечения данных
            Pattern addPattern = Pattern.compile("addTotalTime = (\\d+)");
            Pattern removePattern = Pattern.compile("removeTotalTime = (\\d+)");
            Pattern addCountPattern = Pattern.compile("addTotalCount = (\\d+)");
            Pattern removeCountPattern = Pattern.compile("removeTotalCount = (\\d+)");

            // Чтение всех файлов
            Files.list(folder)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .forEach(path -> {
                        try {
                            Map<String, Long> stats = new HashMap<>();
                            List<String> lines = Files.readAllLines(path);
                            for (String line : lines) {
                                Matcher addMatcher = addPattern.matcher(line);
                                Matcher removeMatcher = removePattern.matcher(line);
                                Matcher addCountMatcher = addCountPattern.matcher(line);
                                Matcher removeCountMatcher = removeCountPattern.matcher(line);
                                if (addMatcher.find()) {
                                    stats.put("addTotalTime", Long.parseLong(addMatcher.group(1)));
                                }
                                if (removeMatcher.find()) {
                                    stats.put("removeTotalTime", Long.parseLong(removeMatcher.group(1)));
                                }
                                if (addCountMatcher.find()) {
                                    stats.put("addTotalCount", Long.parseLong(addCountMatcher.group(1)));
                                }
                                if (removeCountMatcher.find()) {
                                    stats.put("removeTotalCount", Long.parseLong(removeCountMatcher.group(1)));
                                }
                            }
                            results.put(path.getFileName().toString(), stats);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(results);
        return results;

    }


    public JPanel createDemoPanel() {
        JFreeChart chart = createChart(createDataset());
        chart.setPadding(new RectangleInsets(4, 8, 2, 2));
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(1000, 500));
        return panel;
    }

    public static void main(String[] args) {
        TimeSeriesChart demo = new TimeSeriesChart("Сравнение");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
}
