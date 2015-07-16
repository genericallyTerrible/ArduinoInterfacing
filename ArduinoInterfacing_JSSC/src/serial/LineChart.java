/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author John
 */
public class LineChart extends JPanel {

    private final XYSeries defaultSeries;
    private final XYSeriesCollection defaultDataset;
    private final JFreeChart defaultChart;
    private final ChartPanel graph;
    private int samples = 0;

    /**
     * Creates a jFreeChart lineChart
     *
     * @param graphName Name of the chart
     * @param xAxisLabel X-Axis Label
     * @param yAxisLabel Y-Axis Label
     * @param bufferSize Max number of data points to display on the graph at a
     * time. Bear in mind that larger values (>30,000) can begin to cause
     * problems with lag.
     */
    public LineChart(String graphName, String xAxisLabel, String yAxisLabel, int bufferSize) {
        initComponents();
        defaultSeries = new XYSeries(graphName);
        defaultSeries.setMaximumItemCount(bufferSize);
        defaultDataset = new XYSeriesCollection(defaultSeries);
        defaultChart = ChartFactory.createXYLineChart(graphName, xAxisLabel, yAxisLabel, defaultDataset);
        graph = new ChartPanel(defaultChart);

        this.add(graph, BorderLayout.CENTER);
        graph.setVisible(true);

    }

    /**
     * Adds the y value to the end of the data set
     *
     * @param y The y value of the data point to add
     */
    public void append(int y) {
        defaultSeries.add(samples++, y);
    }
    
    public void updateBufferSize(int newBufferSize) {
        defaultSeries.setMaximumItemCount(newBufferSize);
    }

    /**
     * Clears the series storing all the data and therefore the chart displaying
     * it
     */
    public void clear() {
        defaultSeries.clear();
        samples = 0;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
