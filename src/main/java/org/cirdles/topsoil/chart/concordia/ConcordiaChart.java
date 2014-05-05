/*
 * Copyright (C) 2014 John Zeringue
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cirdles.topsoil.chart.concordia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart.Data;
import javafx.util.Duration;
import org.cirdles.topsoil.chart.DataConverter;
import org.cirdles.topsoil.chart.NumberChart;

/**
 * <p>
 * A <code>NumbertChart</code> use to show a Concordia Graph, containing for now only Error Ellipse.</p>
 * <p>
 * It support adding, removing or changing the Ellipses.</p>
 *
 * @author John Zeringue
 * @see NumberChart
 */
public class ConcordiaChart extends NumberChart {

    private final DataConverter<ErrorEllipse> converter;
    
    private final ErrorEllipsePlotter errorEllipsePlotter;
    private final ErrorEllipseFiller errorEllipseFiller;
    private final ConcordiaLinePlotter concordiaLinePlotter;

    ConcordiaLine concordiaLine;

    public ConcordiaChart() {
        this(new DefaultConverter());
    }

    public ConcordiaChart(DataConverter<ErrorEllipse> converter) {
        super();

        getStylesheets().add(ConcordiaChart.class.getResource("ConcordiaChart.css").toExternalForm());
        setAnimated(false);
        getXAxis().setAnimated(false);
        getXAxis().setLabel("\u00B2\u2070\u2077Pb/\u00B2\u00B3\u2075U"); // "207Pb/235U"
        getYAxis().setAnimated(false);
        getYAxis().setLabel("\u00B2\u2070\u2076Pb/\u00B2\u00B3\u2078U"); // "206Pb/238U"

        errorEllipsePlotter = new ErrorEllipsePlotter(this);
        errorEllipseFiller = new ErrorEllipseFiller(this);
        concordiaLinePlotter = new ConcordiaLinePlotter(this);

        this.converter = converter;
    }

    public ConcordiaChart(ObservableList<Series<Number, Number>> data) {
        this();
        setData(data);
    }

    @Override
    protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
//        item.getNode().getStyleClass().add("series" + getData().indexOf(series));

        if (shouldAnimate()) {
            getPlotChildren().add(
                    errorEllipsePlotter.plot(converter.convert(item)));
            getPlotChildren().add(item.getNode());
            // fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), item.getNode());
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            getPlotChildren().add(
                    errorEllipsePlotter.plot(converter.convert(item)));
        }
    }

    @Override
    protected void dataItemRemoved(Data<Number, Number> item, Series<Number, Number> series) {
        final Node ellipse = item.getNode();

        if (shouldAnimate()) {
            // fade out
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), ellipse);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                getPlotChildren().remove(ellipse);
            });
            fadeOut.play();
        } else {
            getPlotChildren().remove(ellipse);
        }
    }

    @Override
    protected void dataItemChanged(Data<Number, Number> data) {

    }

    @Override
    protected void seriesAdded(Series<Number, Number> series, int seriesIndex) {
        // handle any data already in the series
        for (int i = 0; i < series.getData().size(); i++) {
            Data<Number, Number> item = series.getData().get(i);
            dataItemAdded(series, i, item);
        }
    }

    @Override
    protected void seriesRemoved(Series<Number, Number> series) {
        series.getData().stream()
                .forEach((item) -> {
                    dataItemRemoved(item, series);
                });
    }

    @Override
    protected void layoutPlotChildren() {
        getPlotChildren().clear();

        concordiaLine = new ConcordiaLine(xAxis.getLowerBound(), xAxis.getUpperBound(),
                                          yAxis.getLowerBound(), yAxis.getUpperBound());

        getPlotChildren().add(concordiaLinePlotter.plot(concordiaLine));

        // we have nothing to layout if no data is present
        if (getData() == null) {
            return;
        }

        // add ellipse fills
        getData().stream().forEach(series -> {
            series.getData().stream().forEach(item -> {
                getPlotChildren().add(
                        errorEllipseFiller.fill(converter.convert(item)));
            });
        });

        // update ellipse positions
        getData().stream().forEach(series -> {
            series.getData().stream().forEach(item -> {
                getPlotChildren().add(
                        errorEllipsePlotter.plot(converter.convert(item)));
            });
        });
    }

    @Override
    protected void updateAxisRange() {
        final Axis<Number> xAxis = getXAxis();
        final Axis<Number> yAxis = getYAxis();

        final List<Number> xData = new ArrayList<>();
        final List<Number> yData = new ArrayList<>();

        if (xAxis.isAutoRanging() || yAxis.isAutoRanging()) {
            getData().stream().forEach(series -> {
                series.getData().stream().forEach(item -> {
                    ErrorEllipse errorEllipse = converter.convert(item);

                    if (xAxis.isAutoRanging()) {
                        xData.add(errorEllipse.getMinX());
                        xData.add(errorEllipse.getMaxX());
                    }

                    if (yAxis.isAutoRanging()) {
                        yData.add(errorEllipse.getMinY());
                        yData.add(errorEllipse.getMaxY());
                    }
                });
            });

            if (xAxis.isAutoRanging()) {
                xAxis.invalidateRange(xData);
            }

            if (yAxis.isAutoRanging()) {
                yAxis.invalidateRange(yData);
            }
        }
    }

    public void snapConcordiaLineToCorners() {
        setPlotWindow(ConcordiaLine.getX(concordiaLine.getStartT()),
                      ConcordiaLine.getX(concordiaLine.getEndT()),
                      ConcordiaLine.getY(concordiaLine.getStartT()),
                      ConcordiaLine.getY(concordiaLine.getEndT()));
    }

    private static final class DefaultConverter implements DataConverter<ErrorEllipse> {

        @Override
        public ErrorEllipse convert(Data data) {
            if (data == null || !(data.getExtraValue() instanceof Map)) {
                throw new IllegalArgumentException();
            }

            Map extra = (Map) data.getExtraValue();

            double x = getField(extra, "xValue");
            double y = getField(extra, "yValue");
            double sigmaX = getField(extra, "sigmaX");
            double sigmaY = getField(extra, "sigmaY");
            double rho = getField(extra, "rho");

            return new ErrorEllipse(x, y, sigmaX, sigmaY, rho);
        }

        protected double getField(Map map, String field) {
            return ((ObjectProperty<Number>) map.get(field)).get().doubleValue();
        }
    }
}